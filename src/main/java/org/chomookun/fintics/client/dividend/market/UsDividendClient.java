package org.chomookun.fintics.client.dividend.market;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.chomookun.arch4j.core.common.support.RestTemplateBuilder;
import org.chomookun.fintics.client.dividend.DividendClient;
import org.chomookun.fintics.client.dividend.DividendClientProperties;
import org.chomookun.fintics.client.support.YahooClientSupport;
import org.chomookun.fintics.model.Asset;
import org.chomookun.fintics.model.Dividend;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

public class UsDividendClient extends DividendClient implements YahooClientSupport {

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    /**
     * constructor
     * @param dividendClientProperties dividend client properties
     */
    public UsDividendClient(DividendClientProperties dividendClientProperties, ObjectMapper objectMapper) {
        super(dividendClientProperties);

        // rest template
        this.restTemplate = RestTemplateBuilder.create()
                .httpRequestRetryStrategy(new DefaultHttpRequestRetryStrategy())
                .build();

        // object mapper
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isSupport(Asset asset) {
        return Objects.equals(asset.getMarket(), "US");
    }

    @Override
    public List<Dividend> getDividends(Asset asset, LocalDate dateFrom, LocalDate dateTo) {
        HttpHeaders headers = createYahooHeader();
        String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s", asset.getSymbol());
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("events", "events=capitalGain|div|split")
                .queryParam("interval", "1d")
                .queryParam("period1", dateFrom.atStartOfDay().atOffset(ZoneOffset.UTC).toEpochSecond())
                .queryParam("period2", dateTo.atStartOfDay().atOffset(ZoneOffset.UTC).toEpochSecond())
                .build()
                .toUriString();
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .headers(headers)
                .build();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode resultNode = rootNode.path("chart").path("result").get(0);
        JsonNode eventsNode = resultNode.path("events").path("dividends");
        Map<String, Map<String,Double>> dividendsMap = objectMapper.convertValue(eventsNode, new TypeReference<>(){});
        dividendsMap = Optional.ofNullable(dividendsMap).orElse(Collections.emptyMap());
        List<Dividend> dividends = new ArrayList<>();
        for (Map.Entry<String, Map<String,Double>> entry : dividendsMap.entrySet()) {
            Map<String,Double> value = entry.getValue();
            LocalDate date = Instant.ofEpochSecond(value.get("date").longValue())
                    .atOffset(ZoneOffset.UTC)
                    .toLocalDate();
            BigDecimal dividendPerShare = BigDecimal.valueOf(value.get("amount"));
            Dividend dividend = Dividend.builder()
                    .assetId(asset.getAssetId())
                    .date(date)
                    .dividendPerShare(dividendPerShare)
                    .build();
            dividends.add(dividend);
        }
        // sort
        dividends.sort(Comparator.comparing(Dividend::getDate).reversed());
        // returns
        return dividends;
    }

}
