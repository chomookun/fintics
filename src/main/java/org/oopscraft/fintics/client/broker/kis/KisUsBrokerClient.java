package org.oopscraft.fintics.client.broker.kis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.oopscraft.fintics.client.broker.UsBrokerClient;
import org.oopscraft.fintics.model.*;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KisUsBrokerClient extends UsBrokerClient {

    private final boolean production;

    private final String apiUrl;

    private final String appKey;

    private final String appSecret;

    private final String accountNo;

    private final ObjectMapper objectMapper;

    public KisUsBrokerClient(BrokerClientDefinition definition, Properties properties) {
        super(definition, properties);
        this.production = Boolean.parseBoolean(properties.getProperty("production"));
        this.apiUrl = properties.getProperty("apiUrl");
        this.appKey = properties.getProperty("appKey");
        this.appSecret = properties.getProperty("appSecret");
        this.accountNo = properties.getProperty("accountNo");
        this.objectMapper = new ObjectMapper();
    }

    HttpHeaders createHeaders() throws InterruptedException {
        KisAccessToken accessToken = KisAccessTokenRegistry.getAccessToken(apiUrl, appKey, appSecret);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        httpHeaders.add("authorization", "Bearer " + accessToken.getAccessToken());
        httpHeaders.add("appkey", appKey);
        httpHeaders.add("appsecret", appSecret);
        return httpHeaders;
    }

    private synchronized static void sleep() throws InterruptedException {
        Thread.sleep(300);
    }

    @Override
    public boolean isOpened(LocalDateTime dateTime) throws InterruptedException {
        // check us super (weekend and fixed holiday)
        if (!super.isOpened(dateTime)) {
            return false;
        }

        // broker agent is not providing api
        // default
        return true;
    }

    private String toExcd3(String exchange) {
        String excd = null;
        switch(exchange) {
            case "XNAS" -> excd = "NAS";
            case "XNYS" -> excd = "NYS";
            case "XASE" -> excd = "AMS";
        }
        return excd;
    }

    private String toExcd4(String exchange) {
        String excd = null;
        switch(exchange) {
            case "XNAS" -> excd = "NASD";
            case "XNYS" -> excd = "NYSE";
            case "XASE" -> excd = "AMEX";
        }
        return excd;
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/overseas-price/v1/quotations/inquire-time-itemchartprice";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "HHDFS76950200");
        headers.add("custtype", "P");
        String excd = toExcd3(asset.getExchange());
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
                    LocalDateTime ohlcvDateTime = LocalDateTime.parse(
                            row.get("kymd") + row.get("khms"),
                            DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    );
                    BigDecimal openPrice = new BigDecimal(row.get("open"));
                    BigDecimal highPrice = new BigDecimal(row.get("high"));
                    BigDecimal lowPrice = new BigDecimal(row.get("low"));
                    BigDecimal closePrice = new BigDecimal(row.get("last"));
                    BigDecimal volume = new BigDecimal(row.get("evol"));
                    return Ohlcv.builder()
                            .type(Ohlcv.Type.MINUTE)
                            .dateTime(ohlcvDateTime)
                            .openPrice(openPrice)
                            .highPrice(highPrice)
                            .lowPrice(lowPrice)
                            .closePrice(closePrice)
                            .volume(volume)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(Asset asset, LocalDateTime dateTime) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/overseas-price/v1/quotations/dailyprice";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "HHDFS76240000");
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("AUTH", "")
                .queryParam("EXCD", toExcd3(asset.getExchange()))
                .queryParam("SYMB", asset.getSymbol())
                .queryParam("GUBN", "0")
                .queryParam("BYMD", "")
                .queryParam("MODP", "1")
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
                    LocalDateTime ohlcvDateTime = LocalDateTime.parse(row.get("xymd") + "000000", DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    BigDecimal openPrice = new BigDecimal(row.get("open"));
                    BigDecimal highPrice = new BigDecimal(row.get("high"));
                    BigDecimal lowPrice = new BigDecimal(row.get("low"));
                    BigDecimal closePrice = new BigDecimal(row.get("clos"));
                    BigDecimal volume = new BigDecimal(row.get("tvol"));
                    return Ohlcv.builder()
                            .type(Ohlcv.Type.DAILY)
                            .dateTime(ohlcvDateTime)
                            .openPrice(openPrice)
                            .highPrice(highPrice)
                            .lowPrice(lowPrice)
                            .closePrice(closePrice)
                            .volume(volume)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderBook getOrderBook(Asset asset) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/overseas-price/v1/quotations/price-detail";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "HHDFS76200200");
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("AUTH", "")
                .queryParam("EXCD", toExcd3(asset.getExchange()))
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
        JsonNode outputNode = rootNode.path("output");
        Map<String, String> output = objectMapper.convertValue(outputNode, new TypeReference<>() {});
        BigDecimal price = new BigDecimal(output.get("last"));

        return OrderBook.builder()
                .price(price)
                .bidPrice(price)
                .askPrice(price)
                .build();
    }

    @Override
    public BigDecimal getTickPrice(Asset asset, BigDecimal price) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/overseas-price/v1/quotations/price-detail";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "HHDFS76200200");
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("AUTH", "")
                .queryParam("EXCD", toExcd3(asset.getExchange()))
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
        JsonNode outputNode = rootNode.path("output");
        Map<String, String> output = objectMapper.convertValue(outputNode, new TypeReference<>() {});
        String tickPrice = output.get("e_hogau");
        return new BigDecimal(tickPrice);
    }

    @Override
    public Balance getBalance() throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/overseas-stock/v1/trading/inquire-balance";
        HttpHeaders headers = createHeaders();
        String trId = production ? "TTTS3012R" : "VTTS3012R";
        headers.add("tr_id", trId);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("OVRS_EXCG_CD", "NASD")
                .queryParam("TR_CRCY_CD", "USD")
                .queryParam("CTX_AREA_FK200", "")
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

        JsonNode output1Node = rootNode.path("output1");
        List<Map<String, String>> output1 = objectMapper.convertValue(output1Node, new TypeReference<>(){});
        JsonNode output2Node = rootNode.path("output2");
        Map<String, String> output2 = objectMapper.convertValue(output2Node, new TypeReference<>(){});

        // balance
        Balance balance = Balance.builder()
                .accountNo(accountNo)
                .purchaseAmount(new BigDecimal(output2.get("frcr_pchs_amt1")))
                .valuationAmount(new BigDecimal(output2.get("tot_evlu_pfls_amt")))
                .realizedProfitAmount(new BigDecimal(output2.get("ovrs_rlzt_pfls_amt")))
                .profitAmount(new BigDecimal(output2.get("ovrs_tot_pfls")))
                .build();

        // cash amount, total amount
        BigDecimal cashAmount = getBalanceCashAmount();
        BigDecimal totalAmount = balance.getValuationAmount().add(cashAmount);
        balance.setTotalAmount(totalAmount);
        balance.setCashAmount(cashAmount);

        // balance asset
        List<BalanceAsset> balanceAssets = output1.stream()
                .map(row -> BalanceAsset.builder()
                        .accountNo(accountNo)
                        .assetId(toAssetId(row.get("ovrs_pdno")))
                        .assetName(row.get("ovrs_item_name"))
                        .quantity(new BigDecimal(row.get("ovrs_cblc_qty")))
                        .orderableQuantity(new BigDecimal(row.get("ord_psbl_qty")))
                        .purchasePrice(new BigDecimal(row.get("pchs_avg_pric")))
                        .purchaseAmount(new BigDecimal(row.get("frcr_pchs_amt1")))
                        .valuationAmount(new BigDecimal(row.get("ovrs_stck_evlu_amt")))
                        .profitAmount(new BigDecimal(row.get("frcr_evlu_pfls_amt")))
                        .build())
                .filter(balanceAsset -> balanceAsset.getQuantity().intValue() > 0)
                .collect(Collectors.toList());
        balance.setBalanceAssets(balanceAssets);

        // return
        return balance;
    }

    private BigDecimal getBalanceCashAmount() throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/overseas-stock/v1/trading/inquire-present-balance";
        HttpHeaders headers = createHeaders();
        String trId = production ? "CTRP6504R" : "VTRP6504R";
        headers.add("tr_id", trId);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("WCRC_FRCR_DVSN_CD", "02")
                .queryParam("NATN_CD", "840")
                .queryParam("TR_MKET_CD", "00")
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
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }
        JsonNode output2Node = rootNode.path("output2");
        List<Map<String, String>> output2 = objectMapper.convertValue(output2Node, new TypeReference<>(){});
        return new BigDecimal(output2.get(0).get("frcr_dncl_amt_2"));
    }

    @Override
    public Order submitOrder(Order order) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
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

        // quantity
        int quantity = Math.max(order.getQuantity().intValue(),1);

        // price
        double price = order.getPrice()
                .setScale(2, RoundingMode.FLOOR)
                .doubleValue();

        // sllType
        String sllType = null;
        if (order.getType() == Order.Type.SELL) {
            sllType = "00";
        }

        // request
        Map<String, String> payloadMap = new LinkedHashMap<>();
        payloadMap.put("CANO", accountNo.split("-")[0]);
        payloadMap.put("ACNT_PRDT_CD", accountNo.split("-")[1]);
        payloadMap.put("OVRS_EXCG_CD", "NASD");
        payloadMap.put("PDNO", order.getSymbol());
        payloadMap.put("ORD_QTY", String.valueOf(quantity));
        payloadMap.put("OVRS_ORD_UNPR", String.valueOf(price));
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
        ResponseEntity<Map<String, String>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>() {});
        Map<String, String> responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();

        // response
        String rtCd = responseMap.get("rt_cd");
        String msg1 = responseMap.get("msg1");
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        // return
        return order;
    }

    @Override
    public List<Order> getWaitingOrders() throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();

        HttpHeaders headers = createHeaders();
        headers.add("tr_id", production ? "TTTS3018R" : "VTTS3018R");
        String url = apiUrl + "/uapi/overseas-stock/v1/trading/inquire-nccs";
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("OVRS_EXCG_CD", "NASD")
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

    @Override
    public Order amendOrder(Order order) throws InterruptedException {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        String url = apiUrl + "/uapi/overseas-stock/v1/trading/order-rvsecncl";
        String trId = (production ? "TTTT1004U" : "VTTT1004U");
        BigDecimal quantity = order.getQuantity();
        BigDecimal price = order.getPrice().setScale(2, RoundingMode.FLOOR);

        // request
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", trId);
        Map<String, String> payloadMap = new LinkedHashMap<>();
        payloadMap.put("CANO", accountNo.split("-")[0]);
        payloadMap.put("ACNT_PRDT_CD", accountNo.split("-")[1]);
        payloadMap.put("OVRS_EXCG_CD", "NASD");
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
        ResponseEntity<Map<String, String>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>(){});
        Map<String, String> responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();

        // response
        String rtCd = responseMap.get("rt_cd");
        String msg1 = responseMap.get("msg1");
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        // return
        return order;
    }

}
