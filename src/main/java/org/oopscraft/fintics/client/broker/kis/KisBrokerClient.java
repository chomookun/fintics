package org.oopscraft.fintics.client.broker.kis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.oopscraft.arch4j.core.common.support.RestTemplateBuilder;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.oopscraft.fintics.model.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 한국투자증권 국내 주식 broker client
 */
@Slf4j
public class KisBrokerClient extends BrokerClient {

    private final static Object LOCK_OBJECT = new Object();

    private final boolean production;

    private final String apiUrl;

    private final String appKey;

    private final String appSecret;

    private final String accountNo;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    /**
     * constructor
     * @param definition broker definition
     * @param properties broker properties
     */
    public KisBrokerClient(BrokerClientDefinition definition, Properties properties) {
        super(definition, properties);
        this.production = Boolean.parseBoolean(properties.getProperty("production"));
        this.apiUrl = properties.getProperty("apiUrl");
        this.appKey = properties.getProperty("appKey");
        this.appSecret = properties.getProperty("appSecret");
        this.accountNo = properties.getProperty("accountNo");

        // rest template
        this.restTemplate = RestTemplateBuilder.create()
                .retryCount(3)
                .build();

        // object mapper
        this.objectMapper = new ObjectMapper();
    }

    /**
     * creates headers
     * @return headers
     */
    HttpHeaders createHeaders() throws InterruptedException {
        KisAccessToken accessToken = KisAccessTokenRegistry.getAccessToken(apiUrl, appKey, appSecret);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        httpHeaders.add("authorization", "Bearer " + accessToken.getAccessToken());
        httpHeaders.add("appkey", appKey);
        httpHeaders.add("appsecret", appSecret);
        return httpHeaders;
    }

    /**
     * force sleep
     */
    private synchronized void sleep() throws InterruptedException {
        synchronized (LOCK_OBJECT) {
            long sleepMillis = production ? 200 : 1_000;
            KisAccessThrottler.sleep(appKey, sleepMillis);
        }
    }

    @Override
    public boolean isOpened(LocalDateTime datetime) throws InterruptedException {
        // check weekend
        DayOfWeek dayOfWeek = datetime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        // check holiday
        return !isHoliday(datetime);
    }

    /**
     * checks holiday
     * @param dateTime date time
     * @return whether is holiday
     * @see [국내휴장일조회[국내주식-040]](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-index-quotations#L_5c488ab2-59fd-486e-bf74-b68e813e35c0)
     */
    boolean isHoliday(LocalDateTime dateTime) throws InterruptedException {
        // 모의 투자는 휴장일 조회 API 제공 하지 않음
        if(!production) {
            return false;
        }
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/chk-holiday";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "CTCA0903R");

        // convert date time
        String baseDt = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("BASS_DT", baseDt)
                .queryParam("CTX_AREA_NK","")
                .queryParam("CTX_AREA_FK","")
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        List<Map<String, String>> output = objectMapper.convertValue(rootNode.path("output"), new TypeReference<>(){});
        Map<String, String> matchedRow = output.stream()
                .filter(row -> {
                    LocalDate localDate = LocalDate.parse(row.get("bass_dt"), DateTimeFormatter.ofPattern("yyyyMMdd"));
                    return localDate.isEqual(dateTime.toLocalDate());
                })
                .findFirst()
                .orElse(null);

        // define holiday
        boolean holiday = false;
        if(matchedRow != null) {
            String openYn = matchedRow.get("opnd_yn");
            if(openYn.equals("N")) {
                holiday = true;
            }
        }
        return holiday;
    }

    /**
     * returns minute ohlcvs
     * @param asset asset
     * @return minute ohlcvs
     * @see [주식당일분봉조회[v1_국내주식-022]](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations2#L_eddbb36a-1d55-461a-b242-3067ba1e5640)
     */
    @Override
    public List<Ohlcv> getMinuteOhlcvs(Asset asset) throws InterruptedException {
        String fidEtcClsCode = "";
        String fidCondMrktDivCode = "J";
        String fidInputIscd = asset.getSymbol();
        LocalTime time = ZonedDateTime.now(getDefinition().getTimezone()).toLocalTime();
        LocalTime closeTime = LocalTime.of(15,30);
        LocalTime fidInputHour1Time = (time.isAfter(closeTime) ? closeTime : time);

        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "FHKST03010200");
        headers.add("custtype", "P");
        String fidInputHour1 = fidInputHour1Time.format(DateTimeFormatter.ofPattern("HHmmss"));
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("FID_ETC_CLS_CODE", fidEtcClsCode)
                .queryParam("FID_COND_MRKT_DIV_CODE", fidCondMrktDivCode)
                .queryParam("FID_INPUT_ISCD", fidInputIscd)
                .queryParam("FID_INPUT_HOUR_1", fidInputHour1)
                .queryParam("FID_PW_DATA_INCU_YN","N")
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        List<Map<String, String>> output2 = objectMapper.convertValue(rootNode.path("output2"), new TypeReference<>(){});
        return output2.stream()
                .map(row -> {
                    LocalDateTime datetime = LocalDateTime.parse(
                            row.get("stck_bsop_date") + row.get("stck_cntg_hour"),
                                    DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                            .truncatedTo(ChronoUnit.MINUTES);
                    ZoneId timezone = getDefinition().getTimezone();
                    BigDecimal open = new BigDecimal(row.get("stck_oprc"));
                    BigDecimal high = new BigDecimal(row.get("stck_hgpr"));
                    BigDecimal low = new BigDecimal(row.get("stck_lwpr"));
                    BigDecimal close = new BigDecimal(row.get("stck_prpr"));
                    BigDecimal volume = new BigDecimal(row.get("cntg_vol"));
                    return Ohlcv.builder()
                            .assetId(asset.getAssetId())
                            .type(Ohlcv.Type.MINUTE)
                            .dateTime(datetime)
                            .timeZone(timezone)
                            .open(open)
                            .high(high)
                            .low(low)
                            .close(close)
                            .volume(volume)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * return daily ohlcvs
     * @param asset asset
     * @return daily ohlcvs
     * @see [국내주식기간별시세 - 일,주,월,년[v1_국내주식-016]](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations2#L_a08c3421-e50f-4f24-b1fe-64c12f723c77)
     */
    @Override
    public List<Ohlcv> getDailyOhlcvs(Asset asset) throws InterruptedException {
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "FHKST03010100");
        String fidCondMrktDivCode = "J";
        String fidInputIscd = asset.getSymbol();
        String fidInputDate1 = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now().minusMonths(1));
        String fidInputDate2 = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
        String fidPeriodDivCode = "D";  // 일봉
        String fidOrgAdjPrc = "0";      // 수정주가
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("FID_COND_MRKT_DIV_CODE", fidCondMrktDivCode)
                .queryParam("FID_INPUT_ISCD", fidInputIscd)
                .queryParam("FID_INPUT_DATE_1", fidInputDate1)
                .queryParam("FID_INPUT_DATE_2", fidInputDate2)
                .queryParam("FID_PERIOD_DIV_CODE", fidPeriodDivCode)
                .queryParam("FID_ORG_ADJ_PRC", fidOrgAdjPrc)
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        List<Map<String, String>> output2 = objectMapper.convertValue(rootNode.path("output2"), new TypeReference<>(){});

        return output2.stream()
                .filter(row -> !row.isEmpty())  // 신규 종목의 경우 값 없이 {}로 반환 되는 경우가 있음
                .map(row -> {
                    LocalDateTime datetime = LocalDate.parse(row.get("stck_bsop_date"), DateTimeFormatter.ofPattern("yyyyMMdd"))
                            .atTime(LocalTime.MIN)
                            .truncatedTo(ChronoUnit.DAYS);
                    ZoneId timezone = getDefinition().getTimezone();
                    BigDecimal open = new BigDecimal(row.get("stck_oprc"));
                    BigDecimal high = new BigDecimal(row.get("stck_hgpr"));
                    BigDecimal low = new BigDecimal(row.get("stck_lwpr"));
                    BigDecimal close = new BigDecimal(row.get("stck_clpr"));
                    BigDecimal volume = new BigDecimal(row.get("acml_vol"));
                    return Ohlcv.builder()
                            .assetId(asset.getAssetId())
                            .type(Ohlcv.Type.DAILY)
                            .dateTime(datetime)
                            .timeZone(timezone)
                            .open(open)
                            .high(high)
                            .low(low)
                            .close(close)
                            .volume(volume)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * return order book
     * @param asset asset
     * @return order book
     * @see [주식현재가 호가/예상체결[v1_국내주식-011]](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations2#L_af3d3794-92c0-4f3b-8041-4ca4ddcda5de)
     */
    @Override
    public OrderBook getOrderBook(Asset asset) throws InterruptedException {
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "FHKST01010200");
        String fidCondMrktDivCode = "J";
        String fidInputIscd = asset.getSymbol();
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("FID_COND_MRKT_DIV_CODE", fidCondMrktDivCode)
                .queryParam("FID_INPUT_ISCD", fidInputIscd)
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        Map<String, String> output1 = objectMapper.convertValue(rootNode.path("output1"), new TypeReference<>() {});
        Map<String, String> output2 = objectMapper.convertValue(rootNode.path("output2"), new TypeReference<>() {});

        BigDecimal price = new BigDecimal(output2.get("stck_prpr"));
        BigDecimal bidPrice = new BigDecimal(output1.get("bidp1"));
        BigDecimal askPrice = new BigDecimal(output1.get("askp1"));

        return OrderBook.builder()
                .price(price)
                .bidPrice(bidPrice)
                .askPrice(askPrice)
                .build();
    }

    /**
     * 호가 단위 반환 (정책은 아래 주소를 참조)
     * https://securities.koreainvestment.com/main/customer/notice/Notice.jsp?&cmd=TF04ga000002&currentPage=1&num=39930
     */
    @Override
    public BigDecimal getTickPrice(Asset asset, BigDecimal price) throws InterruptedException {
        // etf, etn, elw
        if(Arrays.asList("ETF","ETN","ELW").contains(asset.getType())) {
            return BigDecimal.valueOf(5);
        }
        // default fallback (stock)
        BigDecimal priceTick = null;
        if (price.compareTo(BigDecimal.valueOf(2_000)) <= 0) {
            priceTick = BigDecimal.valueOf(1);
        } else if (price.compareTo(BigDecimal.valueOf(5_000)) <= 0) {
            priceTick = BigDecimal.valueOf(5);
        } else if (price.compareTo(BigDecimal.valueOf(20_000)) <= 0) {
            priceTick = BigDecimal.valueOf(10);
        } else if (price.compareTo(BigDecimal.valueOf(50_000)) <= 0) {
            priceTick = BigDecimal.valueOf(50);
        } else if (price.compareTo(BigDecimal.valueOf(200_000)) <= 0) {
            priceTick = BigDecimal.valueOf(100);
        } else if (price.compareTo(BigDecimal.valueOf(500_000)) <= 0) {
            priceTick = BigDecimal.valueOf(500);
        } else {
            priceTick = BigDecimal.valueOf(1_000);
        }
        return priceTick;
    }

    /**
     * check minimum order amount
     * @param quantity quantity
     * @param price price
     * @return whether is over minimum
     */
    @Override
    public boolean isOverMinimumOrderAmount(BigDecimal quantity, BigDecimal price) throws InterruptedException {
        return quantity.compareTo(BigDecimal.ONE) >= 0;
    }

    /**
     * return account balance
     * @return balance
     * @see [주식잔고조회[v1_국내주식-006]](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-order#L_66c61080-674f-4c91-a0cc-db5e64e9a5e6)
     */
    @Override
    public Balance getBalance() throws InterruptedException {
        Balance balance = new Balance();
        List<BalanceAsset> balanceAssets = new ArrayList<>();

        // pagination key
        String trCont = "";
        String ctxAreaFk100 = "";
        String ctxAreaNk100 = "";

        for (int i = 0; i < 10; i ++) {
            String url = apiUrl + "/uapi/domestic-stock/v1/trading/inquire-balance";
            HttpHeaders headers = createHeaders();
            String trId = production ? "TTTC8434R" : "VTTC8434R";
            headers.add("tr_id", trId);
            headers.add("tr_cont", trCont);
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("CANO", accountNo.split("-")[0])
                    .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                    .queryParam("AFHR_FLPR_YN", "N")
                    .queryParam("OFL_YN", "")
                    .queryParam("INQR_DVSN", "02")
                    .queryParam("UNPR_DVSN", "01")
                    .queryParam("FUND_STTL_ICLD_YN", "N")
                    .queryParam("FNCG_AMT_AUTO_RDPT_YN", "N")
                    .queryParam("PRCS_DVSN", "00")
                    .queryParam("CTX_AREA_FK100", ctxAreaFk100)
                    .queryParam("CTX_AREA_NK100", ctxAreaNk100)
                    .build()
                    .toUriString();
            RequestEntity<Void> requestEntity = RequestEntity
                    .get(url)
                    .headers(headers)
                    .build();
            sleep();
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            JsonNode rootNode;
            try {
                rootNode = objectMapper.readTree(responseEntity.getBody());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
            String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
            if (!"0".equals(rtCd)) {
                throw new RuntimeException(msg1);
            }

            JsonNode output1Node = rootNode.path("output1");
            List<Map<String, String>> output1 = objectMapper.convertValue(output1Node, new TypeReference<>() {
            });

            JsonNode output2Node = rootNode.path("output2");
            List<Map<String, String>> output2 = objectMapper.convertValue(output2Node, new TypeReference<>() {
            });

            // balance
            if (i == 0) {
                balance = Balance.builder()
                        .accountNo(accountNo)
                        .totalAmount(new BigDecimal(output2.get(0).get("tot_evlu_amt")))
                        .cashAmount(new BigDecimal(output2.get(0).get("prvs_rcdl_excc_amt")))
                        .purchaseAmount(new BigDecimal(output2.get(0).get("pchs_amt_smtl_amt")))
                        .valuationAmount(new BigDecimal(output2.get(0).get("evlu_amt_smtl_amt")))
                        .build();
            }

            // page balance assets
            List<BalanceAsset> pageBalanceAssets = output1.stream()
                    .map(row -> {
                        try {
                            return BalanceAsset.builder()
                                    .accountNo(accountNo)
                                    .assetId(toAssetId(row.get("pdno")))
                                    .name(row.get("prdt_name"))
                                    .market(getDefinition().getMarket())
                                    .quantity(new BigDecimal(row.get("hldg_qty")))
                                    .orderableQuantity(new BigDecimal(row.get("ord_psbl_qty")))
                                    .purchasePrice(new BigDecimal(row.get("pchs_avg_pric")))
                                    .purchaseAmount(new BigDecimal(row.get("pchs_amt")))
                                    .valuationPrice(new BigDecimal(row.get("prpr")))
                                    .valuationAmount(new BigDecimal(row.get("evlu_amt")))
                                    .profitAmount(new BigDecimal(row.get("evlu_pfls_amt")))
                                    .build();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(balanceAsset -> balanceAsset.getQuantity().intValue() > 0)
                    .collect(Collectors.toList());
            balanceAssets.addAll(pageBalanceAssets);

            // detects next page
            trCont = responseEntity.getHeaders().getFirst("tr_cont");
            ctxAreaFk100 = objectMapper.convertValue(rootNode.path("ctx_area_fk100"), String.class);
            ctxAreaNk100 = objectMapper.convertValue(rootNode.path("ctx_area_nk100"), String.class);
            if ((Objects.equals(trCont,"D") || Objects.equals(trCont, "E"))
            || pageBalanceAssets.isEmpty()) {
                break;
            }
            trCont = "N";
        }

        // adds balance assets
        balance.setBalanceAssets(balanceAssets);

        // calculates profit amount
        BigDecimal profitAmount = balanceAssets.stream()
                .map(BalanceAsset::getProfitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        balance.setProfitAmount(profitAmount);

        // calculates realized profit amount (모의 투자는 지원 하지 않음)
        if(production) {
            BigDecimal realizedProfitAmount = getBalanceRealizedProfitAmount();
            balance.setRealizedProfitAmount(realizedProfitAmount);
        }

        // return
        return balance;
    }

    /**
     * gets balance realized profit amount
     * @return realized profit amount
     * @see [주식잔고조회_실현손익[v1_국내주식-041]](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-order#L_ff79302e-6014-495e-a188-6dca69fc952e)
     */
    private BigDecimal getBalanceRealizedProfitAmount() throws InterruptedException {
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/inquire-balance-rlz-pl";
        HttpHeaders headers = createHeaders();
        String trId = "TTTC8494R";
        headers.add("tr_id", trId);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("AFHR_FLPR_YN", "N")
                .queryParam("OFL_YN", "")
                .queryParam("INQR_DVSN", "00")
                .queryParam("UNPR_DVSN", "01")
                .queryParam("FUND_STTL_ICLD_YN", "N")
                .queryParam("FNCG_AMT_AUTO_RDPT_YN", "N")
                .queryParam("PRCS_DVSN", "00")
                .queryParam("COST_ICLD_YN", "Y")
                .queryParam("CTX_AREA_FK100", "")
                .queryParam("CTX_AREA_NK100", "")
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        JsonNode output2Node = rootNode.path("output2");
        List<Map<String, String>> output2 = objectMapper.convertValue(output2Node, new TypeReference<>(){});
        return new BigDecimal(output2.get(0).get("rlzt_pfls"));
    }

    /**
     * submit order
     * @param asset asset
     * @param order order
     * @return submitted order
     * @see [주식주문 - 현금[v1_국내주식-001]](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-order#L_aade4c72-5fb7-418a-9ff2-254b4d5f0ceb)
     */
    @Override
    public Order submitOrder(Asset asset, Order order) throws InterruptedException {
        // quantity with check
        BigDecimal quantity = order.getQuantity().setScale(0, RoundingMode.FLOOR);
        order.setQuantity(quantity);

        // price
        BigDecimal price = order.getPrice().setScale(0, RoundingMode.FLOOR);
        order.setPrice(price);

        // api url
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/order-cash";
        HttpHeaders headers = createHeaders();

        // order type
        String trId;
        switch(order.getType()) {
            case BUY -> trId = production ? "TTTC0802U" : "VTTC0802U";
            case SELL -> trId = production ? "TTTC0801U" : "VTTC0801U";
            default -> throw new RuntimeException("invalid order kind");
        }
        headers.add("tr_id", trId);

        // order kind
        String ordDvsn;
        String ordUnpr;
        switch(order.getKind()) {
            case LIMIT -> {
                ordDvsn = "00";
                ordUnpr = String.valueOf(order.getPrice().longValue());
            }
            case MARKET -> {
                ordDvsn = "01";
                ordUnpr = "0";
            }
            default -> throw new RuntimeException("invalid order type");
        }

        // order quantity
        String ordQty = String.valueOf(quantity.intValue());

        // request
        Map<String, String> payloadMap = new LinkedHashMap<>();
        payloadMap.put("CANO", accountNo.split("-")[0]);
        payloadMap.put("ACNT_PRDT_CD", accountNo.split("-")[1]);
        payloadMap.put("PDNO", order.getSymbol());
        payloadMap.put("ORD_DVSN", ordDvsn);
        payloadMap.put("ORD_QTY", ordQty);
        payloadMap.put("ORD_UNPR", ordUnpr);
        RequestEntity<Map<String, String>> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);

        // exchange
        sleep();
        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>(){});
        Map<String, Object> responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();

        // response
        String rtCd = responseMap.getOrDefault("rt_cd", "").toString();
        String msg1 = responseMap.getOrDefault("msg1", "").toString();
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        // return
        return order;
    }

    /**
     * gets waiting orders
     * @return waiting orders
     * @see [주식정정취소가능주문조회[v1_국내주식-004]](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-order#L_d4537e9c-73f7-414c-9fb0-4eae3bc397d0)
     */
    @Override
    public List<Order> getWaitingOrders() throws InterruptedException {
        // supported in only production
        if(!production) {
            return new ArrayList<>();
        }
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/inquire-psbl-rvsecncl";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "TTTC8036R");
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("CTX_AREA_FK100", "")
                .queryParam("CTX_AREA_NK100","")
                .queryParam("INQR_DVSN_1", "1")
                .queryParam("INQR_DVSN_2", "0")
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();

        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        JsonNode outputNode = rootNode.path("output");
        List<Map<String, String>> output = objectMapper.convertValue(outputNode, new TypeReference<>(){});

        // return
        return output.stream()
                .map(row -> {
                    Order.Type orderType;
                    switch (row.get("sll_buy_dvsn_cd")) {
                        case "01" -> orderType = Order.Type.SELL;
                        case "02" -> orderType = Order.Type.BUY;
                        default -> throw new RuntimeException("invalid sll_buy_dvsn_cd");
                    }
                    Order.Kind orderKind;
                    switch (row.get("ord_dvsn_cd")) {
                        case "00" -> orderKind = Order.Kind.LIMIT;
                        case "01" -> orderKind = Order.Kind.MARKET;
                        default -> orderKind = null;
                    }

                    String symbol = row.get("pdno");
                    BigDecimal quantity = new BigDecimal(row.get("psbl_qty"));
                    BigDecimal price = new BigDecimal(row.get("ord_unpr"));
                    String clientOrderId = row.get("odno");
                    return Order.builder()
                            .type(orderType)
                            .assetId(toAssetId(symbol))
                            .kind(orderKind)
                            .quantity(quantity)
                            .price(price)
                            .brokerOrderId(clientOrderId)
                            .build();

                })
                .collect(Collectors.toList());
    }

    /**
     * amends order
     * @param asset asset
     * @param order order
     * @return amended order
     * @see [주식주문 - 정정취소[v1_국내주식-003]](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-order#L_4bfdfb2b-34a7-43f6-935a-e637724f960a)
     */
    @Override
    public Order amendOrder(Asset asset, Order order) throws InterruptedException {
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/order-rvsecncl";
        HttpHeaders headers = createHeaders();

        // trId
        String trId = (production ? "TTTC0803U" : "VTTC0803U");
        headers.add("tr_id", trId);

        // order type
        String ordDvsn;
        switch(order.getKind()) {
            case LIMIT -> {
                ordDvsn = "00";
            }
            case MARKET -> {
                ordDvsn = "01";
            }
            default -> throw new RuntimeException("invalid order type");
        }

        // request
        Map<String, String> payloadMap = new LinkedHashMap<>();
        payloadMap.put("CANO", accountNo.split("-")[0]);
        payloadMap.put("ACNT_PRDT_CD", accountNo.split("-")[1]);
        payloadMap.put("KRX_FWDG_ORD_ORGNO", "");
        payloadMap.put("ORGN_ODNO", order.getBrokerOrderId());
        payloadMap.put("ORD_DVSN", ordDvsn);
        payloadMap.put("RVSE_CNCL_DVSN_CD", "01");
        payloadMap.put("ORD_QTY", "0");
        payloadMap.put("ORD_UNPR", String.valueOf(order.getPrice().longValue()));
        payloadMap.put("QTY_ALL_ORD_YN", "Y");
        RequestEntity<Map<String, String>> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);

        // exchange
        sleep();
        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>(){});
        Map<String, Object> responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();

        // response
        String rtCd = responseMap.getOrDefault("rt_cd", "").toString();
        String msg1 = responseMap.getOrDefault("msg1", "").toString();
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        // return
        return order;
    }

    /**
     * gets realized profits
     * @param dateFrom date from
     * @param dateTo  date to
     * @return realized profits
     * @see [기간별매매손익현황조회[v1_국내주식-060]](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-order#L_4755efc7-31c4-411c-af45-3e6948611f0a)
     */
    @Override
    public List<RealizedProfit> getRealizedProfits(LocalDate dateFrom, LocalDate dateTo) throws InterruptedException {
        // 모의 투자는 미지원
        if (!this.production) {
            throw new UnsupportedOperationException();
        }

        // defines
        List<RealizedProfit> realizedProfits = new ArrayList<>();
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "TTTC8715R");

        // pagination key
        String ctxAreaFk100 = "";
        String ctxAreaNk100 = "";

        // loop for pagination
        for (int i = 0; i < 100; i ++) {
            String url = apiUrl + "/uapi/domestic-stock/v1/trading/inquire-period-trade-profit";
            String inqrStrtDt = dateFrom.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String inqrEndDt = dateTo.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("CANO", accountNo.split("-")[0])
                    .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                    .queryParam("SORT_DVSN", "00")
                    .queryParam("PDNO", "")
                    .queryParam("INQR_STRT_DT", inqrStrtDt)
                    .queryParam("INQR_END_DT", inqrEndDt)
                    .queryParam("CBLC_DVSN", "00")
                    .queryParam("CTX_AREA_FK100", ctxAreaFk100)
                    .queryParam("CTX_AREA_NK100", ctxAreaNk100)
                    .build()
                    .toUriString();
            RequestEntity<Void> requestEntity = RequestEntity
                    .get(url)
                    .headers(headers)
                    .build();

            sleep();
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

            JsonNode rootNode;
            try {
                rootNode = objectMapper.readTree(responseEntity.getBody());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
            String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
            if (!"0".equals(rtCd)) {
                throw new RuntimeException(msg1);
            }

            // temp list
            List<Map<String, String>> output1 = objectMapper.convertValue(rootNode.path("output1"), new TypeReference<>() {});
            List<RealizedProfit> tempRealizedProfits = output1.stream()
                    .filter(row -> new BigDecimal(row.get("sll_qty")).compareTo(BigDecimal.ZERO) > 0)
                    .map(row -> {
                        return RealizedProfit.builder()
                                .date(LocalDate.parse(row.get("trad_dt"), DateTimeFormatter.BASIC_ISO_DATE))
                                .symbol(row.get("pdno"))
                                .name(row.get("prdt_name"))
                                .quantity(new BigDecimal(row.get("sll_qty")))
                                .purchasePrice(new BigDecimal(row.get("pchs_unpr")))
                                .purchaseAmount(new BigDecimal(row.get("buy_amt")))
                                .disposePrice(new BigDecimal(row.get("sll_pric")))
                                .disposeAmount(new BigDecimal(row.get("sll_amt")))
                                .feeAmount(new BigDecimal(row.get("fee")).add(new BigDecimal(row.get("tl_tax"))))
                                .profitAmount(new BigDecimal(row.get("rlzt_pfls")))
                                .profitPercentage(new BigDecimal(row.get("pfls_rt")).setScale(2, RoundingMode.HALF_UP))
                                .build();
                    })
                    .collect(Collectors.toList());

            // adds final list
            realizedProfits.addAll(tempRealizedProfits);

            // detects pagination
            ctxAreaFk100 = objectMapper.convertValue(rootNode.path("ctx_area_fk100"), String.class);
            ctxAreaNk100 = objectMapper.convertValue(rootNode.path("ctx_area_nk100"), String.class);
            if (tempRealizedProfits.isEmpty()) {
                break;
            }
            headers.set("tr_cont", "N");
            ctxAreaFk100 = ctxAreaNk100;
        }

        // return
        return realizedProfits;
    }

    @Override
    public List<DividendHistory> getDividendHistories(LocalDate dateFrom, LocalDate dateTo) throws InterruptedException {
        return new ArrayList<>();
    }

}
