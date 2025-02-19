package org.chomookun.fintics.core.broker.client.alpaca;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.chomookun.arch4j.core.common.support.RestTemplateBuilder;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.broker.model.Balance;
import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientDefinition;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.profit.model.DividendProfit;
import org.chomookun.fintics.core.profit.model.RealizedProfit;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class AlpacaBrokerClient extends BrokerClient {

    private final boolean live;

    private final String apiKey;

    private final String apiSecret;

    private final boolean insecure;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    /**
     * constructor
     * @param definition definition
     * @param properties properties
     */
    public AlpacaBrokerClient(BrokerClientDefinition definition, Properties properties) {
        super(definition, properties);
        this.live = Boolean.parseBoolean(properties.getProperty("live"));
        this.apiKey = properties.getProperty("apiKey");
        this.apiSecret = properties.getProperty("apiSecret");
        this.insecure = Optional.ofNullable(properties.getProperty("insecure"))
                .map(Boolean::parseBoolean)
                .orElse(Boolean.FALSE);

        // rest template
        this.restTemplate = createRestTemplate();

        // object mapper
        this.objectMapper = new ObjectMapper();
    }

    /**
     * creates rest template
     * @return rest template
     */
    RestTemplate createRestTemplate() {
        return RestTemplateBuilder.create()
                .httpRequestRetryStrategy(new DefaultHttpRequestRetryStrategy())
                .insecure(insecure)
                .build();
    }

    HttpHeaders createHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("APCA-API-KEY-ID", apiKey);
        httpHeaders.add("APCA-API-SECRET-KEY", apiSecret);
        return httpHeaders;
    }

    @Override
    public boolean isOpened(LocalDateTime datetime) throws InterruptedException {
        if (live) {
            // TODO
            return true;
        } else {
            return true;
        }
    }

    /**
     * gets ohlcvs
     * @param symbol symbol
     * @param timeframe time frame
     * @param start start date time
     * @return ohlcvs
     * @see [https://docs.alpaca.markets/reference/stockbars-1]
     */
    List<Map<String,String>> getOhlcvs(String symbol, String timeframe, LocalDateTime start) {
        RequestEntity<Void> requestEntity = RequestEntity
                .get("https://data.sandbox.alpaca.markets/v2/stocks/bars/latest?feed=sip")
                .headers(createHttpHeaders())
                .build();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return objectMapper.convertValue(rootNode.path("bars").path(symbol), new TypeReference<>(){});
    }

    @Override
    public List<Ohlcv> getMinuteOhlcvs(Asset asset) throws InterruptedException {
        RequestEntity<Void> requestEntity = RequestEntity
                .get("https://data.sandbox.alpaca.markets/v2/stocks/bars/latest?feed=sip")
                .headers(createHttpHeaders())
                .build();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        return null;
    }

    @Override
    public List<Ohlcv> getDailyOhlcvs(Asset asset) throws InterruptedException {
        return null;
    }

    @Override
    public OrderBook getOrderBook(Asset asset) throws InterruptedException {
        return null;
    }

    @Override
    public Balance getBalance() throws InterruptedException {
        return null;
    }

    @Override
    public Order submitOrder(Asset asset, Order order) throws InterruptedException {
        return null;
    }

    @Override
    public List<Order> getWaitingOrders() throws InterruptedException {
        return null;
    }

    @Override
    public Order amendOrder(Asset asset, Order order) throws InterruptedException {
        return null;
    }

    @Override
    public List<RealizedProfit> getRealizedProfits(LocalDate dateFrom, LocalDate dateTo) throws InterruptedException {
        return null;
    }

    @Override
    public List<DividendProfit> getDividendProfits(LocalDate dateFrom, LocalDate dateTo) throws InterruptedException {
        return null;
    }

}
