package org.chomookun.fintics.client.asset.market;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.chomookun.arch4j.core.common.support.RestTemplateBuilder;
import org.chomookun.fintics.client.asset.AssetClient;
import org.chomookun.fintics.client.asset.AssetClientProperties;
import org.chomookun.fintics.client.support.NasdaqClientSupport;
import org.chomookun.fintics.client.support.YahooClientSupport;
import org.chomookun.fintics.model.Asset;
import org.chomookun.fintics.model.Dividend;
import org.chomookun.fintics.model.Ohlcv;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class UsAssetClient extends AssetClient implements NasdaqClientSupport, YahooClientSupport {

    private static final String MARKET_US = "US";

    private static final Currency CURRENCY_USD = Currency.getInstance("USD");

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public UsAssetClient(AssetClientProperties assetClientProperties, ObjectMapper objectMapper) {
        super(assetClientProperties);

        // rest template
        this.restTemplate = RestTemplateBuilder.create()
                .httpRequestRetryStrategy(new DefaultHttpRequestRetryStrategy())
                .build();

        // object mapper
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Asset> getAssets() {
        List<Asset> assets = new ArrayList<>();
        assets.addAll(getStockAssets("NASDAQ"));
        assets.addAll(getStockAssets("NYSE"));
        assets.addAll(getStockAssets("AMEX"));
        assets.addAll(getEtfAssets());
        return assets;
    }

    /**
     * gets stock assets
     * @param exchange exchange code
     * @return list of stock asset
     */
    List<Asset> getStockAssets(String exchange) {
        String url = String.format("https://api.nasdaq.com/api/screener/stocks?tableonly=true&download=true&exchange=%s", exchange);
        RequestEntity<Void> requestEntity = RequestEntity.get(url)
                .headers(createNasdaqHeaders())
                .build();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode rowsNode = rootNode.path("data").path("rows");
        List<Map<String, String>> rows = objectMapper.convertValue(rowsNode, new TypeReference<>() {});

        // sort
        rows.sort((o1, o2) -> {
            BigDecimal o1MarketCap = new BigDecimal(StringUtils.defaultIfBlank(o1.get("marketCap"), "0"));
            BigDecimal o2MarketCap = new BigDecimal(StringUtils.defaultIfBlank(o2.get("marketCap"),"0"));
            return o2MarketCap.compareTo(o1MarketCap);
        });

        // return
        return rows.stream()
                .map(row -> {
                    String exchangeMic = null;
                    switch (exchange) {
                        case "NASDAQ" -> exchangeMic = "XNAS";
                        case "NYSE" -> exchangeMic = "XNYS";
                        case "AMEX" -> exchangeMic = "XASE";
                    }
                    String marketCapString = row.get("marketCap");
                    marketCapString = marketCapString == null || marketCapString.isBlank() ? "0" : marketCapString;
                    BigDecimal marketCap = new BigDecimal(marketCapString)
                            .divide(BigDecimal.valueOf(1_000_000), 2, RoundingMode.HALF_UP)
                            .setScale(0, RoundingMode.HALF_UP);
                    return Asset.builder()
                            .assetId(toAssetId(MARKET_US, row.get("symbol")))
                            .name(row.get("name"))
                            .market(MARKET_US)
                            .exchange(exchangeMic)
                            .type("STOCK")
                            .marketCap(marketCap)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * gets list of ETF
     * @see <a href="https://www.nasdaq.com/market-activity/etf/screener">Nasdaq Symbol Screener</a>
     * @return list of etf asset
     */
    protected List<Asset> getEtfAssets() {
        String url = "https://api.nasdaq.com/api/screener/etf?download=true";
        RequestEntity<Void> requestEntity = RequestEntity.get(url)
                .headers(createNasdaqHeaders())
                .build();
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode rowsNode = rootNode.path("data").path("data").path("rows");
        List<Map<String, String>> rows = objectMapper.convertValue(rowsNode, new TypeReference<>() {});

        // sort
        rows.sort((o1, o2) -> {
            try {
                BigDecimal o1LastSalePrice = convertCurrencyToNumber(o1.get("lastSalePrice"), CURRENCY_USD, BigDecimal.ZERO);
                BigDecimal o2LastSalePrice = convertCurrencyToNumber(o2.get("lastSalePrice"), CURRENCY_USD, BigDecimal.ZERO);
                return o2LastSalePrice.compareTo(o1LastSalePrice);
            } catch (Exception e) {
                log.warn(e.getMessage());
                return 0;
            }
        });

        List<Asset> assets = rows.stream()
                .map(row -> Asset.builder()
                        .assetId(toAssetId(MARKET_US, row.get("symbol")))
                        .name(row.get("companyName"))
                        .market(MARKET_US)
                        .type("ETF")
                        .build())
                .collect(Collectors.toList());

        // fill exchange
        List<String> symbols = assets.stream().map(Asset::getSymbol).toList();
        Map<String, String> exchangeMap = getExchangeMap(symbols);
        assets.forEach(asset -> asset.setExchange(exchangeMap.get(asset.getSymbol())));

        // return
        return assets;
    }

    /**
     * gets exchange map
     * @param symbols list of symbols to retrieve
     * @return exchange map
     */
    Map<String, String> getExchangeMap(List<String> symbols) {
        Map<String, String> exchangeMicMap = new LinkedHashMap<>();
        final int BATCH_SIZE = 100;
        try {
            HttpHeaders headers = createYahooHeader();
            for (int i = 0; i < symbols.size(); i += BATCH_SIZE) {
                List<String> batchSymbols = symbols.subList(i, Math.min(i + BATCH_SIZE, symbols.size()));
                String symbolParam = String.join(",", batchSymbols);
                String url = String.format("https://query2.finance.yahoo.com/v1/finance/quoteType/?symbol=%s&lang=en-US&region=US", symbolParam);
                RequestEntity<Void> requestEntity = RequestEntity.get(url)
                        .headers(headers)
                        .build();
                String responseBody = restTemplate.exchange(requestEntity, String.class).getBody();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode resultNode = rootNode.path("quoteType").path("result");
                List<Map<String, String>> results = objectMapper.convertValue(resultNode, new TypeReference<>() {});
                for (Map<String, String> result : results) {
                    String symbol = result.get("symbol");
                    String exchange = result.get("exchange");
                    String exchangeMic = switch (exchange) {
                        case "NGM" -> "XNAS";
                        case "PCX" -> "XASE";
                        // BATS Exchange to BATS (currently Cboe BZX Exchange)
                        case "BTS" -> "BATS";
                        default -> "XNYS";
                    };
                    exchangeMicMap.put(symbol, exchangeMic);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching exchange information", e);
        }
        // return
        return exchangeMicMap;
    }

    @Override
    public boolean isSupport(Asset asset) {
        return asset.getAssetId().startsWith("US.");
    }

    @Override
    public void updateAsset(Asset asset) {
        switch(Optional.ofNullable(asset.getType()).orElse("")) {
            case "STOCK" -> updateStockAsset(asset);
            case "ETF" -> updateEtfAsset(asset);
            default -> throw new RuntimeException("Unsupported asset type");
        }
    }

    void updateStockAsset(Asset asset) {
        BigDecimal price = null;
        BigDecimal volume = null;
        BigDecimal marketCap = null;
        BigDecimal totalEquity = null;
        BigDecimal netIncome = null;
        BigDecimal eps = null;
        BigDecimal roe = null;
        BigDecimal per = null;
        BigDecimal dividendYield = null;
        int dividendFrequency;
        BigDecimal capitalGain;
        BigDecimal totalReturn;

        // calls summary api
        HttpHeaders headers = createNasdaqHeaders();
        String summaryUrl = String.format(
                "https://api.nasdaq.com/api/quote/%s/summary?assetclass=stocks",
                asset.getSymbol()
        );
        RequestEntity<Void> summaryRequestEntity = RequestEntity.get(summaryUrl)
                .headers(headers)
                .build();
        ResponseEntity<String> summaryResponseEntity = restTemplate.exchange(summaryRequestEntity, String.class);
        JsonNode summaryRootNode;
        try {
            summaryRootNode = objectMapper.readTree(summaryResponseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode summaryDataNode = summaryRootNode.path("data").path("summaryData");
        HashMap<String, Map<String,String>> summaryDataMap = objectMapper.convertValue(summaryDataNode, new TypeReference<>() {});

        // price, market cap
        for(String name : summaryDataMap.keySet()) {
            Map<String, String> map = summaryDataMap.get(name);
            String value = map.get("value");
            if (name.equals("PreviousClose")) {
                price = convertCurrencyToNumber(value, CURRENCY_USD, null);
            }
            if (name.equals("ShareVolume")) {
                volume = convertStringToNumber(value, null);
            }
            if (name.equals("MarketCap")) {
                marketCap = convertStringToNumber(value, null)
                        .divide(BigDecimal.valueOf(1_000_000), 2, RoundingMode.HALF_UP)
                        .setScale(0, RoundingMode.HALF_UP);
            }
            if (name.equals("EarningsPerShare")) {
                eps = convertCurrencyToNumber(value, CURRENCY_USD, null);
            }
            if (name.equals("PERatio")) {
                per = convertStringToNumber(value, null);
            }
            if(name.equals("Yield")) {
                dividendYield = convertPercentageToNumber(value, null);
            }
        }

        // calls financial api
        String financialUrl = String.format(
                "https://api.nasdaq.com/api/company/%s/financials?frequency=1", // frequency 2 is quarterly
                asset.getSymbol()
        );
        RequestEntity<Void> financialRequestEntity = RequestEntity.get(financialUrl)
                .headers(headers)
                .build();
        ResponseEntity<String> financialResponseEntity = restTemplate.exchange(financialRequestEntity, String.class);
        JsonNode financialRootNode;
        try {
            financialRootNode = objectMapper.readTree(financialResponseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode balanceSheetTableRowsNode = financialRootNode.path("data").path("balanceSheetTable").path("rows");
        List<Map<String,String>> balanceSheetTableRows = objectMapper.convertValue(balanceSheetTableRowsNode, new TypeReference<>(){});
        JsonNode incomeStatementTableRowsNode = financialRootNode.path("data").path("incomeStatementTable").path("rows");
        List<Map<String,String>> incomeStatementTableRows = objectMapper.convertValue(incomeStatementTableRowsNode, new TypeReference<>(){});

        for(Map<String,String> row : balanceSheetTableRows) {
            String key = row.get("value1");
            String value = row.get("value2");
            if("Total Equity".equals(key)) {
                totalEquity = convertCurrencyToNumber(value, CURRENCY_USD, null);
            }
        }

        for(Map<String,String> row : incomeStatementTableRows) {
            String key = row.get("value1");
            String value = row.get("value2");
            if("Net Income".equals(key)) {
                netIncome = convertCurrencyToNumber(value, CURRENCY_USD, null);
            }
        }

        // roe
        if(netIncome != null && totalEquity != null) {
            roe = netIncome.divide(totalEquity, 8, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // dividend frequency
        List<Dividend> dividends = getDividends(asset);
        dividendFrequency = dividends.size();

        // capital gain
        List<Ohlcv> ohlcvs = getOhlcvs(asset);
        BigDecimal startPrice = ohlcvs.get(ohlcvs.size() - 1).getClose();
        BigDecimal endPrice = ohlcvs.get(0).getClose();
        capitalGain = endPrice.subtract(startPrice)
                .divide(startPrice, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // total return
        totalReturn = capitalGain.add(Optional.ofNullable(dividendYield).orElse(BigDecimal.ZERO))
                .setScale(2, RoundingMode.HALF_UP);

        // update asset
        asset.setPrice(price);
        asset.setVolume(volume);
        asset.setMarketCap(marketCap);
        asset.setEps(eps);
        asset.setRoe(roe);
        asset.setPer(per);
        asset.setDividendFrequency(dividendFrequency);
        asset.setDividendYield(dividendYield);
        asset.setCapitalGain(capitalGain);
        asset.setTotalReturn(totalReturn);
    }

    void updateEtfAsset(Asset asset) {
        BigDecimal price = null;
        BigDecimal marketCap = null;
        int dividendFrequency;
        BigDecimal dividendYield = null;
        BigDecimal capitalGain;
        BigDecimal totalReturn;

        // calls summary api
        HttpHeaders headers = createNasdaqHeaders();
        String summaryUrl = String.format(
                "https://api.nasdaq.com/api/quote/%s/summary?assetclass=etf",
                asset.getSymbol()
        );
        RequestEntity<Void> summaryRequestEntity = RequestEntity.get(summaryUrl)
                .headers(headers)
                .build();
        ResponseEntity<String> summaryResponseEntity = restTemplate.exchange(summaryRequestEntity, String.class);
        JsonNode summaryRootNode;
        try {
            summaryRootNode = objectMapper.readTree(summaryResponseEntity.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode summaryDataNode = summaryRootNode.path("data").path("summaryData");
        HashMap<String, Map<String,String>> summaryDataMap = objectMapper.convertValue(summaryDataNode, new TypeReference<>() {});

        // price, market cap
        for(String name : summaryDataMap.keySet()) {
            Map<String, String> map = summaryDataMap.get(name);
            String value = map.get("value");
            if (Objects.equals(name, "PreviousClose")) {
                price = convertCurrencyToNumber(value, CURRENCY_USD, null);
            }
            if (Objects.equals(name, "MarketCap")) {
                marketCap = convertStringToNumber(value, null)
                        .divide(BigDecimal.valueOf(1_000_000), 2, RoundingMode.HALF_UP)
                        .setScale(0, RoundingMode.HALF_UP);
            }
            if (Objects.equals(name, "Yield")) {
                dividendYield = convertPercentageToNumber(value, null);
            }
        }

        // dividend frequency
        List<Dividend> dividends = getDividends(asset);
        dividendFrequency = dividends.size();

        // capital gain
        List<Ohlcv> ohlcvs = getOhlcvs(asset);
        BigDecimal startPrice = ohlcvs.get(ohlcvs.size() - 1).getClose();
        BigDecimal endPrice = ohlcvs.get(0).getClose();
        capitalGain = endPrice.subtract(startPrice)
                .divide(startPrice, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // total return
        totalReturn = capitalGain.add(Optional.ofNullable(dividendYield).orElse(BigDecimal.ZERO))
                .setScale(2, RoundingMode.HALF_UP);

        // updates
        asset.setPrice(price);
        asset.setMarketCap(marketCap);
        asset.setDividendFrequency(dividendFrequency);
        asset.setDividendYield(dividendYield);
        asset.setCapitalGain(capitalGain);
        asset.setTotalReturn(totalReturn);
    }

    /**
     * Gets prices
     * @param asset asset
     * @return prices
     */
    public List<Ohlcv> getOhlcvs(Asset asset) {
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now().minusDays(1);
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
        List<Long> timestamps = objectMapper.convertValue(resultNode.path("timestamp"), new TypeReference<>(){});
        JsonNode quoteNode = resultNode.path("indicators").path("quote").get(0);
        List<BigDecimal> opens = objectMapper.convertValue(quoteNode.path("open"), new TypeReference<>(){});
        List<BigDecimal> highs = objectMapper.convertValue(quoteNode.path("high"), new TypeReference<>(){});
        List<BigDecimal> lows = objectMapper.convertValue(quoteNode.path("low"), new TypeReference<>(){});
        List<BigDecimal> closes = objectMapper.convertValue(quoteNode.path("close"), new TypeReference<>(){});
        List<BigDecimal> volumes = objectMapper.convertValue(quoteNode.path("volume"), new TypeReference<>(){});
        List<Ohlcv> ohlcvs = new ArrayList<>();
        for (int i = 0; i < timestamps.size(); i ++) {
            long timestamp = timestamps.get(i);
            ZoneId zoneId = ZoneId.of("America/New_York");
            LocalDateTime dateTime = Instant.ofEpochSecond(timestamp)
                    .atZone(zoneId)
                    .toLocalDate()
                    .atStartOfDay();
            BigDecimal open = opens.get(i);
            BigDecimal high = highs.get(i);
            BigDecimal low = lows.get(i);
            BigDecimal close = closes.get(i);
            BigDecimal volume = volumes.get(i);
            Ohlcv ohlcv = Ohlcv.builder()
                    .dateTime(dateTime)
                    .timeZone(zoneId)
                    .open(open)
                    .high(high)
                    .low(low)
                    .close(close)
                    .volume(volume)
                    .build();
            ohlcvs.add(ohlcv);
        }
        // returns
        return ohlcvs;
    }

    /**
     * Gets dividends
     * @param asset asset
     * @return dividends
     */
    public List<Dividend> getDividends(Asset asset) {
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now().minusDays(1);
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
        List<Dividend> dividends = new ArrayList<>();
        for (Map.Entry<String, Map<String,Double>> entry : dividendsMap.entrySet()) {
            Map<String,Double> value = entry.getValue();
            LocalDate date = Instant.ofEpochSecond(value.get("date").longValue())
                    .atOffset(ZoneOffset.UTC)
                    .toLocalDate();
            BigDecimal dividendPerShare = BigDecimal.valueOf(value.get("amount"));
            Dividend dividend = Dividend.builder()
                    .date(date)
                    .dividendPerShare(dividendPerShare)
                    .build();
            dividends.add(dividend);
        }
        // returns
        return dividends;
    }

}
