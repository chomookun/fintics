package org.chomookun.fintics.client.ohlcv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.chomookun.arch4j.core.common.support.RestTemplateBuilder;
import org.chomookun.fintics.client.support.YahooClientSupport;
import org.chomookun.fintics.model.Asset;
import org.chomookun.fintics.model.Ohlcv;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * simple ohlcv client
 */
@Component
@ConditionalOnProperty(prefix = "fintics", name = "ohlcv-client.class-name", havingValue="org.chomookun.fintics.client.ohlcv.SimpleOhlcvClient")
@Slf4j
public class SimpleOhlcvClient extends OhlcvClient implements YahooClientSupport {

    private final static Object LOCK_OBJECT = new Object();

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    /**
     * constructor
     * @param ohlcvClientProperties client properties
     * @param objectMapper object mapper
     */
    public SimpleOhlcvClient(OhlcvClientProperties ohlcvClientProperties, ObjectMapper objectMapper) {
        super(ohlcvClientProperties);

        // rest template
        this.restTemplate = RestTemplateBuilder.create()
                .httpRequestRetryStrategy(new DefaultHttpRequestRetryStrategy())
                .build();

        // object mapper
        this.objectMapper = objectMapper;
    }

    /**
     * force sleep
     */
    private static synchronized void sleep() {
        synchronized (LOCK_OBJECT) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.warn(e.getMessage());
            }
        }
    }

    /**
     * checks supported asset
     * @param asset asset
     * @return whether is supported
     */
    @Override
    public boolean isSupported(Asset asset) {
        String yahooSymbol = convertToYahooSymbol(asset);
        return isSupported(yahooSymbol);
    }

    /**
     * checks yahoo symbol
     * @param yahooSymbol yahoo symbol
     * @return whether yahoo symbol is valid
     */
    boolean isSupported(String yahooSymbol) {
        String url = String.format("https://query1.finance.yahoo.com/v1/finance/quoteType/?symbol=%s", yahooSymbol);
        RequestEntity<Void> requestEntity = RequestEntity
                .get(url)
                .build();
        sleep();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode resultNode = rootNode.path("quoteType").path("result");
        for (JsonNode result : resultNode) {
            if (yahooSymbol.contentEquals(result.get("symbol").asText())) {
                return true;
            }
        }
        return false;
    }

    /**
     * get asset time zone
     * @param asset asset
     * @return time zone
     */
    ZoneId getTimezone(Asset asset) {
        String market = asset.getAssetId().split("\\.")[0];
        return switch(market) {
            case "US" -> ZoneId.of("America/New_York");
            case "KR" -> ZoneId.of("Asia/Seoul");
            default -> ZoneId.of("UTC");
        };
    }

    /**
     * convert asset to yahoo symbol
     * @param asset asset
     * @return yahoo symbol
     */
    String convertToYahooSymbol(Asset asset) {
        String yahooSymbol;
        String exchange = Optional.ofNullable(asset.getExchange()).orElseThrow(() -> new RuntimeException("exchange is null"));
        switch(exchange) {
            case "XKRX" -> yahooSymbol = String.format("%s.KS", asset.getSymbol());
            case "XKOS" -> yahooSymbol = String.format("%s.KQ", asset.getSymbol());
            default -> yahooSymbol = asset.getSymbol();
        }
        return yahooSymbol;
    }

    /**
     * gets ohlcvs
     * @param asset asset
     * @param type type
     * @param dateTimeFrom date time from
     * @param dateTimeTo date time to
     * @param pageable pageable
     * @return list of ohlcvs
     */
    @Override
    public List<Ohlcv> getOhlcvs(Asset asset, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        HttpHeaders headers = createYahooHeader();

        // yahoo symbol
        String yahooSymbol = convertToYahooSymbol(asset);

        // url
        String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s", yahooSymbol);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("symbol", yahooSymbol)
                .queryParam("interval", "-")
                .queryParam("period1", dateTimeFrom.atOffset(ZoneOffset.UTC).toEpochSecond())
                .queryParam("period2", dateTimeTo.atOffset(ZoneOffset.UTC).toEpochSecond())
                .queryParam("corsDomain", "finance.yahoo.com")
                .build()
                .toUriString();

        // intervals
        List<String> intervals = null;
        switch(type) {
            case MINUTE -> intervals = List.of("1m","2m","5m","15m","30m","60m","90m");
            case DAILY -> intervals = List.of("1d","5d");
        }

        // try request
        String interval;
        long period1 = dateTimeFrom.atOffset(ZoneOffset.UTC).toEpochSecond();
        ResponseEntity<String> responseEntity;
        Map<LocalDateTime,Ohlcv> totalOhlcvMap = new HashMap<>();
        for(int i = intervals.size()-1; i >= 0; i --) {
            interval = intervals.get(i);
            if (type == Ohlcv.Type.MINUTE) {
                // 1m 기긴의 경우 일주일 이전 만 제공
                if ("1m".equals(interval)) {
                    period1 = Math.max(
                            Instant.now().minus(24*7, ChronoUnit.HOURS).atOffset(ZoneOffset.UTC).toEpochSecond(),
                            period1
                    );
                }
            }
            try {
                url = UriComponentsBuilder.fromUriString(url)
                        .replaceQueryParam("interval", interval)
                        .replaceQueryParam("period1", period1)
                        .build()
                        .toUriString();
                RequestEntity<Void> requestEntity = RequestEntity
                        .get(url)
                        .headers(headers)
                        .build();
                sleep();
                responseEntity = restTemplate.exchange(requestEntity, String.class);

                JsonNode rootNode;
                try {
                    rootNode = objectMapper.readTree(responseEntity.getBody());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                // add to total ohlcv map
                Map<LocalDateTime,Ohlcv> ohlcvMap = convertRootNodeToOhlcv(asset, type, interval, rootNode);
                totalOhlcvMap.putAll(ohlcvMap);
            } catch(Throwable ignore) {
                log.debug(ignore.getMessage());
            }
        }

        // check date time is in range
        List<Ohlcv> ohlcvs = totalOhlcvMap.values().stream()
                .filter(ohlcv -> {
                    LocalDateTime dateTime = ohlcv.getDateTime();
                    return (dateTime.isAfter(dateTimeFrom) || dateTime.isEqual(dateTimeFrom))
                            && (dateTime.isBefore(dateTimeTo) || dateTime.isEqual(dateTimeTo));
                }).collect(Collectors.toList());

        // sort by dateTime(sometimes response is not ordered)
        ohlcvs.sort(Comparator
                .comparing(Ohlcv::getDateTime)
                .reversed());

        // apply pageable (yahoo chart not support offset,limit)
        if (pageable.isPaged()) {
            long startIndex = pageable.getOffset();
            long endIndex = Math.min(ohlcvs.size(), startIndex + pageable.getPageSize());
            ohlcvs = ohlcvs.subList(Math.toIntExact(startIndex), Math.toIntExact(endIndex));
        }

        // return
        return ohlcvs;
    }

    /**
     * converts root node to ohlcv map
     * @param asset asset
     * @param type type
     * @param interval interval
     * @param rootNode root node
     * @return map of ohlcv
     */
    Map<LocalDateTime, Ohlcv> convertRootNodeToOhlcv(Asset asset, Ohlcv.Type type, String interval, JsonNode rootNode) {
        JsonNode resultNode = rootNode.path("chart").path("result").get(0);
        List<Long> timestamps = objectMapper.convertValue(resultNode.path("timestamp"), new TypeReference<>(){});
        JsonNode quoteNode = resultNode.path("indicators").path("quote").get(0);
        List<BigDecimal> opens = objectMapper.convertValue(quoteNode.path("open"), new TypeReference<>(){});
        List<BigDecimal> highs = objectMapper.convertValue(quoteNode.path("high"), new TypeReference<>(){});
        List<BigDecimal> lows = objectMapper.convertValue(quoteNode.path("low"), new TypeReference<>(){});
        List<BigDecimal> closes = objectMapper.convertValue(quoteNode.path("close"), new TypeReference<>(){});
        List<BigDecimal> volumes = objectMapper.convertValue(quoteNode.path("volume"), new TypeReference<>(){});

        // duplicated data retrieved.
        Map<LocalDateTime, Ohlcv> ohlcvMap = new TreeMap<>();
        if(timestamps != null) {        // if data not found, timestamps element is null.
            for(int i = 0; i < timestamps.size(); i ++) {
                // truncates dateTime
                Instant instant = Instant.ofEpochSecond(timestamps.get(i));
                ZoneId timeZone = getTimezone(asset);
                LocalDateTime dateTime = switch(type) {
                    case MINUTE -> instant
                            .atZone(timeZone)
                            .toLocalDateTime()
                            .truncatedTo(ChronoUnit.MINUTES);
                    case DAILY -> instant
                            .atZone(timeZone)
                            .toLocalDateTime()
                            .truncatedTo(ChronoUnit.DAYS);
                };
                // ohlcv value
                BigDecimal open = opens.get(i);
                if(open == null) {     // sometimes open price is null (data error)
                    continue;
                }
                BigDecimal high = Optional.ofNullable(highs.get(i)).orElse(open);
                BigDecimal low = Optional.ofNullable(lows.get(i)).orElse(open);
                BigDecimal close = Optional.ofNullable(closes.get(i)).orElse(open);
                BigDecimal volume = Optional.ofNullable(volumes.get(i)).orElse(BigDecimal.ZERO);

                // interpolate flag
                boolean interpolated = switch(type) {
                    case MINUTE -> !interval.equals("1m");
                    case DAILY -> !interval.equals("1d");
                    default -> false;
                };

                Ohlcv ohlcv = Ohlcv.builder()
                        .assetId(asset.getAssetId())
                        .dateTime(dateTime)
                        .timeZone(timeZone)
                        .type(type)
                        .open(open.setScale(2, RoundingMode.HALF_UP))
                        .high(high.setScale(2, RoundingMode.HALF_UP))
                        .low(low.setScale(2, RoundingMode.HALF_UP))
                        .close(close.setScale(2, RoundingMode.HALF_UP))
                        .volume(volume.setScale(2, RoundingMode.HALF_UP))
                        .interpolated(interpolated)
                        .build();
                ohlcvMap.put(dateTime, ohlcv);
            }
        }
        return ohlcvMap;
    }

}
