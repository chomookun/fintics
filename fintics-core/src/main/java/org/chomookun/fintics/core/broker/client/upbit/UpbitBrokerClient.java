package org.chomookun.fintics.core.broker.client.upbit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.arch4j.core.common.support.RestTemplateBuilder;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.broker.model.Balance;
import org.chomookun.fintics.core.broker.model.BalanceAsset;
import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientDefinition;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.broker.model.DividendProfit;
import org.chomookun.fintics.core.broker.model.RealizedProfit;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class UpbitBrokerClient extends BrokerClient {

    private static final String API_URL = "https://api.upbit.com";

    private static final String QUERY_HASH_ALGORITHM = "SHA-512";

    private final String accessKey;

    private final String secretKey;

    private final boolean insecure;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public UpbitBrokerClient(BrokerClientDefinition definition, Properties properties) {
        super(definition, properties);
        this.accessKey = properties.getProperty("accessKey");
        this.secretKey = properties.getProperty("secretKey");
        this.insecure = Optional.ofNullable(properties.getProperty("insecure"))
                .map(Boolean::parseBoolean)
                .orElse(Boolean.FALSE);
        this.restTemplate = createRestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    RestTemplate createRestTemplate() {
        return RestTemplateBuilder.create()
                .httpRequestRetryStrategy(new DefaultHttpRequestRetryStrategy())
                .insecure(insecure)
                .build();
    }

    @Override
    public boolean isOpened(LocalDateTime datetime) throws InterruptedException {
        return true;
    }

    private synchronized static void sleep() throws InterruptedException {
        Thread.sleep(300);
    }

    HttpHeaders createHeaders(String queryString) {
        // check null
        if(queryString == null) {
            queryString = "";
        }
        // query hash
        String queryHash;
        try {
            MessageDigest md = MessageDigest.getInstance(QUERY_HASH_ALGORITHM);
            md.update(queryString.getBytes(StandardCharsets.UTF_8));
            queryHash = String.format("%0128x", new BigInteger(1, md.digest()));
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
        // jwt token
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);
        // http header
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", String.format("Bearer %s", jwtToken));
        return httpHeaders;
    }

    @Override
    public OrderBook getOrderBook(Asset asset) throws InterruptedException {
        String url = API_URL + "/v1/orderbook";
        String queryString = "markets=" + asset.getSymbol();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url + "?" + queryString)
                .headers(createHeaders(queryString))
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Map<String, String>> orderBookUnits = objectMapper.convertValue(rootNode.get(0).path("orderbook_units"), new TypeReference<>() {});
        Map<String, String> orderBookUnit = orderBookUnits.get(0);
        return OrderBook.builder()
                .price(new BigDecimal(orderBookUnit.get("bid_price")))
                .tickPrice(BigDecimal.ZERO)
                .bidPrice(new BigDecimal(orderBookUnit.get("bid_price")))
                .askPrice(new BigDecimal(orderBookUnit.get("ask_price")))
                .build();
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(Asset asset) throws InterruptedException {
        return getOhlcvs(asset, Ohlcv.Type.MINUTE);
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(Asset asset) throws InterruptedException {
        return getOhlcvs(asset, Ohlcv.Type.DAILY);
    }

    private List<Ohlcv> getOhlcvs(Asset asset, Ohlcv.Type ohlcvType) throws InterruptedException {
        String url = API_URL + "/v1/candles/";
        switch(ohlcvType) {
            case MINUTE -> url += "minutes/1";
            case DAILY -> url += "days";
            default -> throw new RuntimeException("invalid OhlcvType");
        }
        String queryString = "market=" + asset.getSymbol() + "&count=200";
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url + "?" + queryString)
                .headers(createHeaders(queryString))
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        List<Map<String, String>> rows;
        try {
            rows = objectMapper.readValue(responseEntity.getBody(), new TypeReference<>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rows.stream()
                .map(row -> {
                    LocalDateTime dateTime = LocalDateTime.parse(row.get("candle_date_time_kst"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            .truncatedTo(ChronoUnit.MINUTES);
                    if (ohlcvType == Ohlcv.Type.DAILY) {
                        dateTime = dateTime.truncatedTo(ChronoUnit.DAYS);
                    }
                    ZoneId timezone = getDefinition().getTimezone();
                    return Ohlcv.builder()
                            .assetId(asset.getAssetId())
                            .type(ohlcvType)
                            .dateTime(dateTime)
                            .timeZone(timezone)
                            .open(new BigDecimal(row.get("opening_price")).setScale(2, RoundingMode.HALF_UP))
                            .high(new BigDecimal(row.get("high_price")).setScale(2, RoundingMode.HALF_UP))
                            .low(new BigDecimal(row.get("low_price")).setScale(2, RoundingMode.HALF_UP))
                            .close(new BigDecimal(row.get("trade_price")).setScale(2, RoundingMode.HALF_UP))
                            .volume(new BigDecimal(row.get("candle_acc_trade_volume")).setScale(2, RoundingMode.HALF_UP))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Balance getBalance() throws InterruptedException {
        RequestEntity<Void> requestEntity = RequestEntity
                .get(API_URL + "/v1/accounts")
                .headers(createHeaders(null))
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        List<Map<String, String>> rows;
        try {
            rows = objectMapper.readValue(responseEntity.getBody(), new TypeReference<>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal cacheAmount = BigDecimal.ZERO;
        BigDecimal purchaseAmount = BigDecimal.ZERO;
        BigDecimal valuationAmount = BigDecimal.ZERO;
        BigDecimal profitAmount = BigDecimal.ZERO;
        BigDecimal realizedProfitAmount = BigDecimal.ZERO;
        List<BalanceAsset> balanceAssets = new ArrayList<>();
        for(Map<String, String> row : rows) {
            String currency = row.get("currency");
            String unitCurrency = row.get("unit_currency");
            String symbol = String.format("%s-%s", unitCurrency, currency);
            BigDecimal assetBalance = new BigDecimal(row.get("balance"));
            BigDecimal assetAverageBuyPrice = new BigDecimal(row.get("avg_buy_price"));
            if("KRW".equals(currency) && "KRW".equals(unitCurrency)) {
                totalAmount = totalAmount.add(assetBalance);
                cacheAmount = cacheAmount.add(assetBalance);
            }else{
                BigDecimal assetPurchaseAmount = assetAverageBuyPrice.multiply(assetBalance)
                        .setScale(0, RoundingMode.CEILING);
                purchaseAmount = purchaseAmount.add(assetPurchaseAmount);
                // upbit 의 경우 평가 금액 확인 불가로 order book 재조회 후 산출
                Asset asset = Asset.builder()
                        .assetId(toAssetId(symbol))
                        .name(symbol)
                        .build();
                OrderBook orderBook = getOrderBook(asset);
                BigDecimal assetValuationAmount = orderBook.getPrice().multiply(assetBalance)
                        .setScale(2, RoundingMode.CEILING);
                valuationAmount = valuationAmount.add(assetValuationAmount);
                totalAmount = totalAmount.add(assetValuationAmount);
                // profit amount
                BigDecimal assetProfitAmount = assetValuationAmount.subtract(assetPurchaseAmount)
                        .setScale(2, RoundingMode.CEILING);
                profitAmount = profitAmount.add(assetProfitAmount);
                // add
                BalanceAsset balanceAsset = BalanceAsset.builder()
                        .assetId(toAssetId(symbol))
                        .name(symbol)
                        .quantity(assetBalance)
                        .orderableQuantity(assetBalance)
                        .purchaseAmount(assetPurchaseAmount)
                        .valuationAmount(assetValuationAmount)
                        .profitAmount(assetProfitAmount)
                        .build();
                balanceAssets.add(balanceAsset);
            }
        }
        return Balance.builder()
                .totalAmount(totalAmount.setScale(0, RoundingMode.CEILING))
                .cashAmount(cacheAmount.setScale(0, RoundingMode.CEILING))
                .purchaseAmount(purchaseAmount.setScale(0, RoundingMode.CEILING))
                .valuationAmount(valuationAmount.setScale(0, RoundingMode.CEILING))
                .profitAmount(profitAmount.setScale(0,RoundingMode.CEILING))
                .realizedProfitAmount(realizedProfitAmount.setScale(0, RoundingMode.CEILING))
                .balanceAssets(balanceAssets)
                .build();
    }

    @Override
    public Order submitOrder(Asset asset, Order order) throws InterruptedException {
        // define parameters
        String market = order.getSymbol();
        String side;
        String ordType;
        BigDecimal price;
        BigDecimal volume;
        switch(order.getType()) {
            case BUY -> {
                side = "bid";
                switch(order.getKind()) {
                    case LIMIT -> {
                        ordType = "limit";
                        price = order.getPrice();
                        volume = order.getQuantity();
                    }
                    case MARKET -> {
                        ordType = "price";
                        price = order.getPrice().multiply(order.getQuantity())
                                .setScale(2, RoundingMode.HALF_UP);
                        volume = null;
                    }
                    default -> throw new RuntimeException("Invalid order type");
                }
            }
            case SELL -> {
                side = "ask";
                switch(order.getKind()) {
                    case LIMIT -> {
                        ordType = "limit";
                        price = order.getPrice();
                        volume = order.getQuantity();
                    }
                    case MARKET -> {
                        ordType = "market";
                        price = null;
                        volume = order.getQuantity();
                    }
                    default -> throw new RuntimeException("Invalid order type");
                }
            }
            default -> throw new RuntimeException("Invalid order kind");
        }
        // url payload
        String url = API_URL + "/v1/orders";
        HashMap<String,String> payloadMap = new HashMap<>();
        payloadMap.put("market", market);
        payloadMap.put("side", side);
        if(price != null) {
            payloadMap.put("price", price.toPlainString());
        }
        if(volume != null) {
            payloadMap.put("volume", volume.toPlainString());
        }
        payloadMap.put("ord_type", ordType);
        payloadMap.put("identifier", IdGenerator.uuid());
        // query string
        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : payloadMap.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }
        String queryString = String.join("&", queryElements.toArray(new String[0]));
        // payload
        String payload;
        try {
            payload = objectMapper.writeValueAsString(payloadMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // request
        RequestEntity<String> requestEntity = RequestEntity
                .post(url)
                .headers(createHeaders(queryString))
                .header("Content-Type", "application/json")
                .body(payload);

        sleep();
        ResponseEntity<Map<String, String>> responseEntity = createRestTemplate().exchange(requestEntity, new ParameterizedTypeReference<>() {});
        Map<String, String> responseMap = responseEntity.getBody();
        log.info("{}", responseMap);
        if(responseMap != null) {
            order.setBrokerOrderId(responseMap.get("uuid"));
        }
        // return
        return order;
    }

    @Override
    public List<Order> getWaitingOrders() throws InterruptedException {
        String url = API_URL + "/v1/orders/";
        String queryString = "state=wait&page=1&limit=100";
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url + "?" + queryString)
                .headers(createHeaders(queryString))
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        List<Map<String, String>> rows;
        try {
            rows = objectMapper.readValue(responseEntity.getBody(), new TypeReference<>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rows.stream()
                .map(row -> {
                    Order.Type orderKind;
                    switch(row.get("side")) {
                        case "bid" -> orderKind = Order.Type.BUY;
                        case "ask" -> orderKind = Order.Type.SELL;
                        default -> throw new RuntimeException("invalid side");
                    }
                    Order.Kind orderType;
                    switch(row.get("ord_type")) {
                        case "limit" -> orderType = Order.Kind.LIMIT;
                        case "market","price" -> orderType = Order.Kind.MARKET;
                        default -> orderType = null;
                    }
                    String symbol = row.get("market");
                    BigDecimal quantity = new BigDecimal(row.get("remaining_volume"));
                    BigDecimal price = new BigDecimal(row.get("price"));
                    String clientOrderId = row.get("uuid");
                    // order
                    return Order.builder()
                            .type(orderKind)
                            .assetId(toAssetId(symbol))
                            .kind(orderType)
                            .quantity(quantity)
                            .price(price)
                            .brokerOrderId(clientOrderId)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Order amendOrder(Asset asset, Order order) throws InterruptedException {
        // cancel
        String url = API_URL + "/v1/order";
        String queryString = "uuid=" + order.getBrokerOrderId();
        RequestEntity<Void> requestEntity = RequestEntity
                .delete(url + "?" + queryString)
                .headers(createHeaders(queryString))
                .build();
        sleep();
        createRestTemplate().exchange(requestEntity, Void.class);
        // submit order
        return submitOrder(asset, order);
    }

    @Override
    public boolean isAvailablePriceAndQuantity(BigDecimal price, BigDecimal quantity) {
        return quantity.multiply(price).compareTo(BigDecimal.valueOf(5_000)) >= 0;
    }

    @Override
    public int getQuantityScale() {
        return 8;
    }

    @Override
    public List<RealizedProfit> getRealizedProfits(LocalDate dateFrom, LocalDate dateTo) throws InterruptedException {
        return new ArrayList<>();
    }

    @Override
    public List<DividendProfit> getDividendProfits(LocalDate dateFrom, LocalDate dateTo) throws InterruptedException {
        return new ArrayList<>();
    }

}
