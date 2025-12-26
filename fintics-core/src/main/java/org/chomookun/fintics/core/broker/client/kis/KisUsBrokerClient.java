package org.chomookun.fintics.core.broker.client.kis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.support.RestTemplateBuilder;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.balance.model.Balance;
import org.chomookun.fintics.core.balance.model.BalanceAsset;
import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientDefinition;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.balance.model.DividendProfit;
import org.chomookun.fintics.core.balance.model.RealizedProfit;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
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
 * 한국투자증권 해외 주식 broker client
 */
@Slf4j
public class KisUsBrokerClient extends BrokerClient {

    private final boolean production;

    private final String apiUrl;

    private final String appKey;

    private final String appSecret;

    private final String accountNo;

    private final boolean insecure;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    /**
     * Constructor
     * @param definition client definition
     * @param properties client properties
     */
    public KisUsBrokerClient(BrokerClientDefinition definition, Properties properties) {
        super(definition, properties);
        this.production = Boolean.parseBoolean(properties.getProperty("production"));
        this.apiUrl = properties.getProperty("apiUrl");
        this.appKey = properties.getProperty("appKey");
        this.appSecret = properties.getProperty("appSecret");
        this.accountNo = properties.getProperty("accountNo");
        this.insecure = Optional.ofNullable(properties.getProperty("insecure"))
                .map(Boolean::parseBoolean)
                .orElse(Boolean.FALSE);
        this.restTemplate = createRestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Creates rest template
     * @return rest template
     */
    RestTemplate createRestTemplate() {
        return RestTemplateBuilder.create()
                .httpRequestRetryStrategy(new KisHttpRequestRetryStrategy())
                .insecure(insecure)
                .build();
    }

    /**
     * Creates rest api header
     * @return http headers
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
     * Force to sleep
     */
    private void sleep() throws InterruptedException {
        long sleepMillis = production ? 200 : 2_000;    // 메뉴얼상은 초당 20건 제약으로 하나 10건만 보내도 응답거부가 많음으로 초당 5건으로 처리
        KisAccessThrottler.sleep(appKey, sleepMillis);
    }

    /**
     * Checks if market is open
     * 해외 휴장일 일정은 rest api 로 제공 되지 않음
     * @param datetime date time
     * @return whether is opened
     */
    @Override
    public boolean isOpened(LocalDateTime datetime) throws InterruptedException {
        // check weekend
        DayOfWeek dayOfWeek = datetime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        // check holiday
        Set<LocalDate> fixedHolidays = new HashSet<>();
        int year = datetime.getYear();
        fixedHolidays.add(LocalDate.of(year, Month.JANUARY, 1));    // New Year's Day
        fixedHolidays.add(LocalDate.of(year, Month.JUNE, 19));      // Juneteenth National Independence Day
        fixedHolidays.add(LocalDate.of(year, Month.JULY, 4));       // Independence Day
        fixedHolidays.add(LocalDate.of(year, Month.DECEMBER, 25));  // Christmas Day
        if (fixedHolidays.contains(datetime.toLocalDate())) {
            return false;
        }
        // TODO checks ics file for holidays
        // default
        return true;
    }

    @Override
    public boolean isOhlcvTrusted() throws InterruptedException {
        return production;
    }

    /**
     * Converts MIC code to kis exchange code (3 length)
     * 한국투자증권 rest api 상 미국거래소 코드가 2종류가 존재함. 해당 method 는 3자리 미국거래소 로 변환
     * @param asset asset
     * @return kis exchange code (3 length)
     */
    private String getExcd(Asset asset) {
        return switch (asset.getExchange()) {
            case "XNAS" -> "NAS";
            case "XNYS" -> "NYS";
            case "XASE", "BATS" -> "AMS";
            default -> null;
        };
    }

    /**
     * Converts MIC code to kis exchange code (4 length)
     * 한국투자증권 rest api 상 미국거래소 코드가 2종류가 존재함. 해당 method 는 4자리 미국거래소 로 변환
     * @param asset asset
     * @return kis exchange code (4 length)
     */
    private String getOvrsExcgCd(Asset asset) {
        return switch (asset.getExchange()) {
            case "XNAS" -> "NASD";
            case "XNYS" -> "NYSE";
            case "XASE", "BATS" -> "AMEX";
            default -> null;
        };
    }

    /**
     * Gets minute ohlcvs
     * @param asset asset
     * @return minute ohlcvs
     * @see <a href="https://apiportal.koreainvestment.com/apiservice/apiservice-oversea-stock-quotations#L_852d7e45-4f34-418b-b6a1-a4552bbcdf90">
     *     해외주식분봉조회[v1_해외주식-030]
     *     </a>
     */
    @Override
    public List<Ohlcv> getMinuteOhlcvs(Asset asset) throws InterruptedException {
        String url = apiUrl + "/uapi/overseas-price/v1/quotations/inquire-time-itemchartprice";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "HHDFS76950200");
        headers.add("custtype", "P");
        String excd = getExcd(asset);
        String symb = asset.getSymbol();
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("AUTH", "")
                .queryParam("EXCD", excd)
                .queryParam("SYMB", symb)
                .queryParam("NMIN", "1")
                .queryParam("PINC", "1")
                .queryParam("NEXT", "")
                .queryParam("NREC", "120")
                .queryParam("FILL", "")
                .queryParam("KEYB", "")
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
                            row.get("xymd") + row.get("xhms"),
                                    DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                            )
                            .truncatedTo(ChronoUnit.MINUTES);
                    ZoneId timezone = getDefinition().getTimezone();
                    BigDecimal open = new BigDecimal(row.get("open"));
                    BigDecimal high = new BigDecimal(row.get("high"));
                    BigDecimal low = new BigDecimal(row.get("low"));
                    BigDecimal close = new BigDecimal(row.get("last"));
                    BigDecimal volume = new BigDecimal(row.get("evol"));
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
     * Gets daily ohlcvs
     * @param asset asset
     * @return daily ohlcvs
     * @see <a href="https://apiportal.koreainvestment.com/apiservice/apiservice-oversea-stock-quotations#L_0e9fb2ba-bbac-4735-925a-a35e08c9a790">
     *     해외주식 기간별시세[v1_해외주식-010]
     *     </a>
     */
    @Override
    public List<Ohlcv> getDailyOhlcvs(Asset asset) throws InterruptedException {
        String url = apiUrl + "/uapi/overseas-price/v1/quotations/dailyprice";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "HHDFS76240000");
        String excd = getExcd(asset);
        String symb = asset.getSymbol();
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("AUTH", "")
                .queryParam("EXCD", excd)
                .queryParam("SYMB", symb)
                .queryParam("GUBN", "0")
                .queryParam("BYMD", "")
                .queryParam("MODP", "0")    // Using raw price (not using adjusting price in trading)
                .queryParam("KEYB", "")
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
                    LocalDateTime datetime = LocalDate.parse(row.get("xymd"), DateTimeFormatter.ofPattern("yyyyMMdd"))
                            .atTime(LocalTime.MIN)
                            .truncatedTo(ChronoUnit.DAYS);
                    ZoneId timezone = getDefinition().getTimezone();
                    BigDecimal open = new BigDecimal(row.get("open"));
                    BigDecimal high = new BigDecimal(row.get("high"));
                    BigDecimal low = new BigDecimal(row.get("low"));
                    BigDecimal close = new BigDecimal(row.get("clos"));
                    BigDecimal volume = new BigDecimal(row.get("tvol"));
                    return Ohlcv.builder()
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
     * Returns order book
     * @param asset asset
     * @return order book
     * @see <a href="https://apiportal.koreainvestment.com/apiservice/apiservice-oversea-stock-quotations#L_ed60877a-6183-433a-9a8c-ef56ed9bc679">
     *     해외주식 현재가 10호가 [해외주식-033]
     *     </a>
     */
    @Override
    public OrderBook getOrderBook(Asset asset) throws InterruptedException{
        String url = apiUrl + "/uapi/overseas-price/v1/quotations/inquire-asking-price";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "HHDFS76200100");
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("AUTH", "")
                .queryParam("EXCD", getExcd(asset))
                .queryParam("SYMB", asset.getSymbol())
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
        JsonNode output1Node = rootNode.path("output1");
        JsonNode output2Node = rootNode.path("output2");
        Map<String, String> output1 = objectMapper.convertValue(output1Node, new TypeReference<>() {});
        Map<String, String> output2 = objectMapper.convertValue(output2Node, new TypeReference<>() {});
        BigDecimal price = new BigDecimal(output1.get("last"));
        BigDecimal tickPrice = getTickPrice(asset, price);
        BigDecimal askPrice = new BigDecimal(output2.get("pask1"));
        BigDecimal bidPrice = new BigDecimal(output2.get("pbid1"));
        return OrderBook.builder()
                .price(price)
                .tickPrice(tickPrice)
                .askPrice(askPrice)
                .bidPrice(bidPrice)
                .build();
    }

    /**
     * Gets tick price
     * @param asset asset
     * @param price current asset price
     * @return tick price
     * @see <a href="https://apiportal.koreainvestment.com/apiservice/apiservice-oversea-stock-quotations#L_abc66a03-8103-4f6d-8ba8-450c2b935e14">
     *     해외주식 현재가상세[v1_해외주식-029]
     *     </a>
     */
    BigDecimal getTickPrice(Asset asset, BigDecimal price) throws InterruptedException {
        String url = apiUrl + "/uapi/overseas-price/v1/quotations/price-detail";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "HHDFS76200200");
        String excd = getExcd(asset);
        String symb = asset.getSymbol();
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("AUTH", "")
                .queryParam("EXCD", excd)
                .queryParam("SYMB", symb)
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
        Map<String, String> output = objectMapper.convertValue(outputNode, new TypeReference<>() {});
        String tickPrice = output.get("e_hogau");   // 호가 단위
        return new BigDecimal(tickPrice);
    }

    /**
     * Gets account balance
     * @return balance
     * @see <a href="https://apiportal.koreainvestment.com/apiservice/apiservice-oversea-stock-order#L_0482dfb1-154c-476c-8a3b-6fc1da498dbf">
     *     해외주식 잔고[v1_해외주식-006]
     *     </a>
     */
    @Override
    public Balance getBalance() throws InterruptedException {
        Balance balance = new Balance();
        List<BalanceAsset> balanceAssets = new ArrayList<>();
        // pagination key
        String trCont = "";
        String ctxAreaFk200 = "";
        String ctxAreaNk200 = "";
        // loop
        for (int i = 0; i < 10; i ++) {
            String url = apiUrl + "/uapi/overseas-stock/v1/trading/inquire-balance";
            HttpHeaders headers = createHeaders();
            String trId = production ? "TTTS3012R" : "VTTS3012R";
            headers.add("tr_id", trId);
            headers.add("tr_cont", trCont);
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("CANO", accountNo.split("-")[0])
                    .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                    .queryParam("OVRS_EXCG_CD", "NASD")
                    .queryParam("TR_CRCY_CD", "USD")
                    .queryParam("CTX_AREA_FK200", ctxAreaFk200)
                    .queryParam("CTX_AREA_NK200", ctxAreaNk200)
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
            Map<String, String> output2 = objectMapper.convertValue(output2Node, new TypeReference<>() {
            });
            // balance
            if (i == 0) {
                balance = Balance.builder()
                        .accountNo(accountNo)
                        .purchaseAmount(new BigDecimal(output2.get("frcr_pchs_amt1")).setScale(2, RoundingMode.HALF_UP))
                        .valuationAmount(new BigDecimal(output2.get("tot_evlu_pfls_amt")).setScale(2, RoundingMode.HALF_UP))
                        .realizedProfitAmount(new BigDecimal(output2.get("ovrs_rlzt_pfls_amt")).setScale(2, RoundingMode.HALF_UP))
                        .profitAmount(new BigDecimal(output2.get("ovrs_tot_pfls")).setScale(2, RoundingMode.HALF_UP))
                        .build();
            }
            // balance asset
            List<BalanceAsset> pageBalanceAssets = output1.stream()
                    .map(row -> BalanceAsset.builder()
                            .accountNo(accountNo)
                            .assetId(toAssetId(row.get("ovrs_pdno")))
                            .name(row.get("ovrs_item_name"))
                            .market(getDefinition().getMarket())
                            .currency(getDefinition().getCurrency())
                            .price(new BigDecimal(row.get("now_pric2")))
                            .quantity(new BigDecimal(row.get("ovrs_cblc_qty")))
                            .orderableQuantity(new BigDecimal(row.get("ord_psbl_qty")))
                            .purchasePrice(new BigDecimal(row.get("pchs_avg_pric")).setScale(2, RoundingMode.HALF_UP))
                            .build())
                    .filter(balanceAsset -> balanceAsset.getQuantity().intValue() > 0)
                    .collect(Collectors.toList());
            balanceAssets.addAll(pageBalanceAssets);
            // detects next page
            trCont = responseEntity.getHeaders().getFirst("tr_cont");
            ctxAreaFk200 = objectMapper.convertValue(rootNode.path("ctx_area_fk200"), String.class);
            ctxAreaNk200 = objectMapper.convertValue(rootNode.path("ctx_area_nk200"), String.class);
            if ((Objects.equals(trCont,"D") || Objects.equals(trCont, "E"))
                    || pageBalanceAssets.isEmpty()) {
                break;
            }
            trCont = "N";
        }
        // set balance assets
        balance.setBalanceAssets(balanceAssets);
        // cash amount, total amount
        BigDecimal cashAmount = getBalanceCashAmount();
        BigDecimal totalAmount = balance.getValuationAmount().add(cashAmount);
        balance.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        balance.setCashAmount(cashAmount.setScale(2, RoundingMode.HALF_UP));
        // return
        return balance;
    }

    /**
     * Gets balance cash amount
     * 잔고 조회 에서 매도 재사용 가능 금액을 알수 없음 으로 해외 주식 매수 가능 금액 조회 (Apple 로 조회)
     * @return cash amount
     * @see <a href="https://apiportal.koreainvestment.com/apiservice/apiservice-oversea-stock-order#L_2a155fee-882f-4d80-8183-559f2f6983e9">
     *     해외주식 매수가능금액조회[v1_해외주식-014]
     *     </a>
     */
    private BigDecimal getBalanceCashAmount() throws InterruptedException {
        String url = apiUrl + "/uapi/overseas-stock/v1/trading/inquire-psamount";
        HttpHeaders headers = createHeaders();
        String trId = production ? "TTTS3007R" : "VTTS3007R";
        headers.add("tr_id", trId);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("OVRS_EXCG_CD", "NASD")
                .queryParam("OVRS_ORD_UNPR", "")
                .queryParam("ITEM_CD", "AAPL")
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
        Map<String, String> output = objectMapper.convertValue(outputNode, new TypeReference<>(){});
        return new BigDecimal(output.get("ovrs_ord_psbl_amt"));
    }

    /**
     * submit order
     * @param asset asset
     * @param order order
     * @return submitted order
     * @see <a href="https://apiportal.koreainvestment.com/apiservice/apiservice-oversea-stock-order#L_e4a7e5fd-eed5-4a85-93f0-f46b804dae5f">
     *     해외주식 주문[v1_해외주식-001]
     *     </a>
     */
    @Override
    public Order submitOrder(Asset asset, Order order) throws InterruptedException {
        // quantity
        BigDecimal quantity = order.getQuantity()
                .setScale(0, RoundingMode.FLOOR);
        order.setQuantity(quantity);
        // price
        BigDecimal price = order.getPrice()
                .setScale(2, RoundingMode.FLOOR);
        order.setPrice(price);
        // rest template
        String url = apiUrl + "/uapi/overseas-stock/v1/trading/order";
        HttpHeaders headers = createHeaders();
        // order type
        String trId = null;
        switch(order.getType()) {
            case BUY -> trId = production ? "TTTT1002U" : "VTTT1002U";
            case SELL -> trId = production ? "TTTT1006U" : "VTTT1001U";
            default -> throw new RuntimeException("invalid order kind");
        }
        headers.add("tr_id", trId);
        // ovrsExcgCd
        String ovrsExcgCd = getOvrsExcgCd(asset);
        // sllType
        String sllType = null;
        if (order.getType() == Order.Type.SELL) {
            sllType = "00";
        }
        // request
        Map<String, String> payloadMap = new LinkedHashMap<>();
        payloadMap.put("CANO", accountNo.split("-")[0]);
        payloadMap.put("ACNT_PRDT_CD", accountNo.split("-")[1]);
        payloadMap.put("OVRS_EXCG_CD", ovrsExcgCd);
        payloadMap.put("PDNO", order.getSymbol());
        payloadMap.put("ORD_QTY", String.valueOf(quantity.intValue()));
        payloadMap.put("OVRS_ORD_UNPR", String.valueOf(price.doubleValue()));
        payloadMap.put("CTAC_TLNO", "");
        payloadMap.put("MGCO_APTM_ODNO", "");
        payloadMap.put("SLL_TYPE", sllType);
        payloadMap.put("ORD_SVR_DVSN_CD", "0");
        payloadMap.put("ORD_DVSN", "00");
        RequestEntity<Map<String, String>> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);
        // exchange
        sleep();
        ResponseEntity<Map<String, Object>> responseEntity = createRestTemplate().exchange(requestEntity, new ParameterizedTypeReference<>() {});
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
     * @see <a href="https://apiportal.koreainvestment.com/apiservice/apiservice-oversea-stock-order#L_60cae69d-c121-4dd9-902c-1112567fd88e">
     *     해외주식 미체결내역[v1_해외주식-005]
     *     </a>
     */
    @Override
    public List<Order> getWaitingOrders() throws InterruptedException {
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", production ? "TTTS3018R" : "VTTS3018R");
        String url = apiUrl + "/uapi/overseas-stock/v1/trading/inquire-nccs";
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("OVRS_EXCG_CD", "NASD")     // NASD includes all us exchange
                .queryParam("SORT_SQN", "DS")
                .queryParam("CTX_AREA_FK200","")
                .queryParam("CTX_AREA_NK200", "")
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
                    Order.Kind orderKind = Order.Kind.LIMIT;
                    String symbol = row.get("pdno");
                    BigDecimal quantity = new BigDecimal(row.get("ft_ord_qty"));
                    BigDecimal price = new BigDecimal(row.get("ft_ord_unpr3"));
                    String brokerOrderId = row.get("odno");
                    return Order.builder()
                            .type(orderType)
                            .assetId(toAssetId(symbol))
                            .kind(orderKind)
                            .quantity(quantity)
                            .price(price)
                            .brokerOrderId(brokerOrderId)
                            .build();

                })
                .collect(Collectors.toList());
    }

    /**
     * amends order
     * @param asset asset
     * @param order order
     * @return amended order
     * @see <a href="https://apiportal.koreainvestment.com/apiservice/apiservice-oversea-stock-order#L_4812f155-bdb5-47ac-a35b-a70d3d8f14c9">
     *     해외주식 정정취소주문[v1_해외주식-003]
     *     </a>
     */
    @Override
    public Order amendOrder(Asset asset, Order order) throws InterruptedException {
        String url = apiUrl + "/uapi/overseas-stock/v1/trading/order-rvsecncl";
        String trId = (production ? "TTTT1004U" : "VTTT1004U");
        BigDecimal quantity = order.getQuantity();
        BigDecimal price = order.getPrice().setScale(2, RoundingMode.FLOOR);
        // request
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", trId);
        // ovrsExcgCd
        String ovrsExcgCd = getOvrsExcgCd(asset);
        // payload
        Map<String, String> payloadMap = new LinkedHashMap<>();
        payloadMap.put("CANO", accountNo.split("-")[0]);
        payloadMap.put("ACNT_PRDT_CD", accountNo.split("-")[1]);
        payloadMap.put("OVRS_EXCG_CD", ovrsExcgCd);
        payloadMap.put("PDNO", order.getSymbol());
        payloadMap.put("ORGN_ODNO", order.getBrokerOrderId());
        payloadMap.put("RVSE_CNCL_DVSN_CD", "01");
        payloadMap.put("ORD_QTY", quantity.toString());
        payloadMap.put("OVRS_ORD_UNPR", price.toString());
        payloadMap.put("ORD_SVR_DVSN_CD", "0");
        RequestEntity<Map<String, String>> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);

        // exchange
        sleep();
        ResponseEntity<Map<String, Object>> responseEntity = createRestTemplate().exchange(requestEntity, new ParameterizedTypeReference<>(){});
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
     * gets realized profit
     * 미국 실현 손익 조회
     * @return realized profits
     * @see <a href="https://apiportal.koreainvestment.com/apiservice/apiservice-oversea-stock-order#L_147d1d34-3001-4958-b970-106935a19fe7">
     *     해외주식 기간손익[v1_해외주식-032]
     *     </a>
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
        headers.add("tr_id", "TTTS3039R");

        // pagination key
        String ctxAreaFk200 = "";
        String ctxAreaNk200 = "";

        // loop
        for (int i = 0; i < 100; i ++) {
            String url = apiUrl + "/uapi/overseas-stock/v1/trading/inquire-period-profit";
            String inqrStrtDt = dateFrom.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String inqrEndDt = dateTo.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("CANO", accountNo.split("-")[0])
                    .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                    .queryParam("OVRS_EXCG_CD", "NASD")     // NASD includes all us exchange
                    .queryParam("NATN_CD", "")
                    .queryParam("CRCY_CD", "USD")
                    .queryParam("PDNO", "")
                    .queryParam("INQR_STRT_DT", inqrStrtDt)
                    .queryParam("INQR_END_DT", inqrEndDt)
                    .queryParam("WCRC_FRCR_DVSN_CD", "01")
                    .queryParam("CTX_AREA_FK200", ctxAreaFk200)
                    .queryParam("CTX_AREA_NK200", ctxAreaFk200)
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

            // updates pagination key
            ctxAreaFk200 = objectMapper.convertValue(rootNode.path("ctx_area_fk200"), String.class);
            ctxAreaNk200 = objectMapper.convertValue(rootNode.path("ctx_area_nk200"), String.class);

            // temp list
            List<Map<String, String>> output1 = objectMapper.convertValue(rootNode.path("output1"), new TypeReference<>() {});
            List<RealizedProfit> tempRealizedProfits = output1.stream()
                    .map(row -> {
                        return RealizedProfit.builder()
                                .date(LocalDate.parse(row.get("trad_day"), DateTimeFormatter.BASIC_ISO_DATE))
                                .symbol(row.get("ovrs_pdno"))
                                .name(row.get("ovrs_item_name"))
                                .quantity(new BigDecimal(row.get("slcl_qty")))
                                .purchasePrice(new BigDecimal(row.get("pchs_avg_pric")))
                                .purchaseAmount(new BigDecimal(row.get("frcr_pchs_amt1")))
                                .disposePrice(new BigDecimal(row.get("avg_sll_unpr")))
                                .disposeAmount(new BigDecimal(row.get("frcr_sll_amt_smtl1")))
                                .feeAmount(new BigDecimal(row.get("stck_sll_tlex")))
                                .profitAmount(new BigDecimal(row.get("ovrs_rlzt_pfls_amt"))
                                        .setScale(2, RoundingMode.DOWN))
                                .profitPercentage(new BigDecimal(row.get("pftrt")).setScale(2, RoundingMode.HALF_UP))
                                .build();
                    })
                    .collect(Collectors.toList());

            // adds final list
            realizedProfits.addAll(tempRealizedProfits);

            // detects pagination
            if (tempRealizedProfits.isEmpty()) {
                break;
            }
            headers.set("tr_cont", "N");
            ctxAreaFk200 = ctxAreaNk200;
        }

        // return
        return realizedProfits;
    }

    /**
     * TODO 현재 권리 내역 조회 API 없음
     * @param dateFrom date from
     * @param dateTo date to
     * @return 배당 이력
     */
    @Override
    public List<DividendProfit> getDividendProfits(LocalDate dateFrom, LocalDate dateTo) throws InterruptedException {
        List<DividendProfit> dividendProfits = new ArrayList<>();

        Set<String> symbols = new HashSet<>();
        // 현재 잔고 종목
        symbols.addAll(this.getBalance().getBalanceAssets().stream()
                .map(BalanceAsset::getSymbol)
                .toList());
        // 기간 체결 이력 종목
        symbols.addAll(getPeriodOrderedSymbols(dateFrom, dateTo));

        // 종목 별 배당 내역 조회
        for (String symobl : symbols) {
            // 권리 내역
            List<Map<String, String>> periodRights = getPeriodRights(symobl, dateFrom, dateTo);
            for (Map<String,String> periodRight : periodRights) {
                LocalDate recordDate = LocalDate.parse(periodRight.get("bass_dt"), DateTimeFormatter.BASIC_ISO_DATE);
                String symbol = periodRight.get("pdno");
                String name = periodRight.get("prdt_name");
                BigDecimal dividendPerUnit = new BigDecimal(periodRight.get("alct_frcr_unpr"));

                // 체결 기준 보유 잔고
                Map<String,String> paymentBalanceAsset = getPaymentBalanceAsset(recordDate, symbol);
                if (paymentBalanceAsset != null) {
                    BigDecimal holdingQuantity = new BigDecimal(paymentBalanceAsset.get("cblc_qty13"));
                    BigDecimal dividendAmount = dividendPerUnit
                            .multiply(holdingQuantity)
                            .setScale(2, RoundingMode.DOWN);

                    // payment date 는 record date 로 부터 3 영업일 후
                    LocalDate paymentDate = recordDate.with(temporal -> {
                        LocalDate date = LocalDate.from(temporal);
                        int addedDays = 0;
                        while (addedDays < 3) {
                            date = date.plus(1, ChronoUnit.DAYS);
                            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                                addedDays++;
                            }
                        }
                        return date;
                    });

                    // dividend history
                    DividendProfit dividendHistory = DividendProfit.builder()
                            .date(recordDate)
                            .symbol(symbol)
                            .name(name)
                            .holdingQuantity(holdingQuantity)
                            .dividendAmount(dividendAmount)
                            .paymentDate(paymentDate)
                            .build();
                    dividendProfits.add(dividendHistory);
                }
            }
        }
        // sort
        dividendProfits.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));

        // returns
        return dividendProfits;
    }

    /**
     * 기간 체결 내역 종목 조회
     * @param dateFrom date from
     * @param dateTo date to
     * @return distinct symbols
     * @see <a href="https://apiportal.koreainvestment.com/apiservice/apiservice-oversea-stock-order#L_6d715b38-566f-4045-a08c-4a594d3a3314">
     *     해외주식 주문체결내역[v1_해외주식-007]
     *     </a>
     */
    private Set<String> getPeriodOrderedSymbols(LocalDate dateFrom, LocalDate dateTo) throws InterruptedException {
        // pagination key
        String trCont = "";
        String ctxAreaFk200 = "";
        String ctxAreaNk200 = "";
        // loop
        Set<String> periodOrderedSymbols = new HashSet<>();
        for (int i = 0; i < 100; i ++) {
            String url = apiUrl + "/uapi/overseas-stock/v1/trading/inquire-ccnl";
            HttpHeaders headers = createHeaders();
            String trId = production ? "TTTS3035R" : "VTTS3035R";
            headers.add("tr_id", trId);
            headers.add("tr_cont", trCont);
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("CANO", accountNo.split("-")[0])
                    .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                    .queryParam("PDNO", "%")
                    .queryParam("ORD_STRT_DT", dateFrom.format(DateTimeFormatter.BASIC_ISO_DATE))
                    .queryParam("ORD_END_DT", dateTo.format(DateTimeFormatter.BASIC_ISO_DATE))
                    .queryParam("SLL_BUY_DVSN", "02")   // 매수만
                    .queryParam("CCLD_NCCS_DVSN", "00") // 체결만
                    .queryParam("OVRS_EXCG_CD", "NASD")
                    .queryParam("SORT_SQN", "DS")
                    .queryParam("ORD_DT", "")
                    .queryParam("ORD_GNO_BRNO", "")
                    .queryParam("ODNO", "")
                    .queryParam("CTX_AREA_NK200", ctxAreaNk200)
                    .queryParam("CTX_AREA_FK200", ctxAreaFk200)
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
            JsonNode outputNode = rootNode.path("output");
            List<Map<String, String>> output = objectMapper.convertValue(outputNode, new TypeReference<>() {
            });
            List<String> distinctSymbols = output.stream()
                    .map(it -> it.get("pdno"))
                    .toList()
                    .stream()
                    .distinct()
                    .toList();
            periodOrderedSymbols.addAll(distinctSymbols);
            // detects next page
            trCont = responseEntity.getHeaders().getFirst("tr_cont");
            ctxAreaFk200 = objectMapper.convertValue(rootNode.path("ctx_area_fk200"), String.class);
            ctxAreaNk200 = objectMapper.convertValue(rootNode.path("ctx_area_nk200"), String.class);
            if ((Objects.equals(trCont,"D") || Objects.equals(trCont, "E"))
                    || output.isEmpty()) {
                break;
            }
            trCont = "N";
        }
        // return
        return periodOrderedSymbols;
    }

    /**
     * 해당 종목 권리 내역 조회
     * @param symbol symbol
     * @param dateFrom date from
     * @param dateTo date to
     * @return list of period rights historios
     * @see <a href="https://apiportal.koreainvestment.com/apiservice/apiservice-oversea-stock-Manalysis#L_2151d14c-0fae-44a5-be38-c3f5ab8354bb">
     *     해외주식 기간별권리조회 [해외주식-052]
     *     </a>
     */
    private List<Map<String,String>> getPeriodRights(String symbol, LocalDate dateFrom, LocalDate dateTo) throws InterruptedException {
        String url = apiUrl + "/uapi/overseas-price/v1/quotations/period-rights";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "CTRGT011R");
        headers.add("tr_cont", "");
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("RGHT_TYPE_CD", "03")
                .queryParam("INQR_DVSN_CD", "02")
                .queryParam("INQR_STRT_DT", dateFrom.format(DateTimeFormatter.BASIC_ISO_DATE))
                .queryParam("INQR_END_DT", dateTo.format(DateTimeFormatter.BASIC_ISO_DATE))
                .queryParam("PDNO", symbol)
                .queryParam("PRDT_TYPE_CD", "")
                .queryParam("CTX_AREA_NK50", "")
                .queryParam("CTX_AREA_FK50", "")
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

        JsonNode outputNode = rootNode.path("output");
        List<Map<String, String>> output = objectMapper.convertValue(outputNode, new TypeReference<>() {});

        List<Map<String, String>> periodRights = output.stream()
                .filter(it -> Objects.equals(it.get("pdno"), symbol))       // like 검색으로 반환됨으로 해당 심볼만 필터링
                .toList();
        // returns
        return periodRights;
    }

    /**
     * 기준 일자 시점 결제 잔고
     * @param date date
     * @return payment balance assets
     * @see <a href="https://apiportal.koreainvestment.com/apiservice/apiservice-oversea-stock-order#L_8e78ed2f-8c3d-424e-b400-82fc94ca4a6b">
     *     해외주식 결제기준잔고 [해외주식-064]
     *     </a>
     */
    Map<String, String> getPaymentBalanceAsset(LocalDate date, String symbol) throws InterruptedException {
        Map<String,String> paymentBalanceAsset = new HashMap<>();
        String url = apiUrl + "/uapi/overseas-stock/v1/trading/inquire-paymt-stdr-balance";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "CTRP6010R");
        headers.add("tr_cont", "");
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("BASS_DT", date.format(DateTimeFormatter.BASIC_ISO_DATE))
                .queryParam("WCRC_FRCR_DVSN_CD", "02")
                .queryParam("INQR_DVSN_CD", "00")
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
        List<Map<String, String>> output1 = objectMapper.convertValue(output1Node, new TypeReference<>() {});

        paymentBalanceAsset = output1.stream()
                .filter(it -> Objects.equals(it.get("pdno"), symbol))
                .findFirst()
                .orElse(null);
        // returns
        return paymentBalanceAsset;
    }

}
