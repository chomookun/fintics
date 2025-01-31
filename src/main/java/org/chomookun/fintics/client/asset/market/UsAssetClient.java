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
import org.chomookun.fintics.model.Asset;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class UsAssetClient extends AssetClient {

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
                    return Asset.builder()
                            .assetId(toAssetId(MARKET_US, row.get("symbol")))
                            .name(row.get("name"))
                            .market(MARKET_US)
                            .exchange(exchangeMic)
                            .type("STOCK")
                            .marketCap(new BigDecimal(StringUtils.defaultIfBlank(row.get("marketCap"), "0")))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * gets list of ETF
     * @see [Nasdaq Symbol Screener](https://www.nasdaq.com/market-activity/etf/screener)
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
                BigDecimal o1LastSalePrice = convertCurrencyToNumber(o1.get("lastSalePrice"));
                BigDecimal o2LastSalePrice = convertCurrencyToNumber(o2.get("lastSalePrice"));
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
        };
    }

    void updateStockAsset(Asset asset) {
        BigDecimal marketCap = null;
        BigDecimal totalAssets = null;
        BigDecimal totalEquity = null;
        BigDecimal netIncome = null;
        BigDecimal eps = null;
        BigDecimal roe = null;
        BigDecimal per = null;
        BigDecimal dividendYield = null;
        int dividendFrequency = 0;
        BigDecimal capitalGain = null;
        BigDecimal totalReturn = null;

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
            if (name.equals("MarketCap")) {
                marketCap = convertStringToNumber(value);
            }
            if (name.equals("EarningsPerShare")) {
                eps = convertCurrencyToNumber(value);
            }
            if (name.equals("PERatio")) {
                per = convertStringToNumber(value);
            }
            if(name.equals("Yield")) {
                dividendYield = convertPercentageToNumber(value);
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
                totalEquity = convertCurrencyToNumber(value);
            }
            if("Total Assets".equals(key)) {
                totalAssets = convertCurrencyToNumber(value);
            }
        }

        for(Map<String,String> row : incomeStatementTableRows) {
            String key = row.get("value1");
            String value = row.get("value2");
            if("Net Income".equals(key)) {
                netIncome = convertCurrencyToNumber(value);
            }
        }

        // roe
        if(netIncome != null && totalEquity != null) {
            roe = netIncome.divide(totalEquity, 8, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // dividend frequency
        Map<LocalDate, BigDecimal> dividends = getDividends(asset);
        dividendFrequency = dividends.size();

        // capital gain
        Map<LocalDate, BigDecimal> prices = getPrices(asset);
        BigDecimal startPrice = prices.entrySet().stream()
                .min(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(BigDecimal.ZERO);
        BigDecimal endPrice = prices.entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(BigDecimal.ZERO);
        capitalGain = endPrice.subtract(startPrice)
                .divide(startPrice, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // total return
        totalReturn = capitalGain.add(dividendYield)
                .setScale(2, RoundingMode.HALF_UP);

        // update asset
        asset.setEps(eps);
        asset.setRoe(roe);
        asset.setPer(per);
        asset.setDividendFrequency(dividendFrequency);
        asset.setDividendYield(dividendYield);
        asset.setCapitalGain(capitalGain);
        asset.setTotalReturn(totalReturn);
    }

    void updateEtfAsset(Asset asset) {
        BigDecimal marketCap = null;
        int dividendFrequency = 0;
        BigDecimal dividendYield = null;
        BigDecimal capitalGain = null;
        BigDecimal totalReturn = null;

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
            if (Objects.equals(name, "MarketCap")) {
                marketCap = convertStringToNumber(value);
            }
            if (Objects.equals(name, "Yield")) {
                dividendYield = convertPercentageToNumber(value);
            }
        }

        // dividend frequency
        Map<LocalDate, BigDecimal> dividends = getDividends(asset);
        dividendFrequency = dividends.size();

        // capital gain
        Map<LocalDate, BigDecimal> prices = getPrices(asset);
        BigDecimal startPrice = prices.entrySet().stream()
                .min(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(BigDecimal.ZERO);
        BigDecimal endPrice = prices.entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(BigDecimal.ZERO);
        capitalGain = endPrice.subtract(startPrice)
                .divide(startPrice, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // total return
        totalReturn = capitalGain.add(dividendYield)
                .setScale(2, RoundingMode.HALF_UP);

        // updates
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
    public Map<LocalDate, BigDecimal> getPrices(Asset asset) {
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

        Map<LocalDate, BigDecimal> prices = new HashMap<>();
        for (int i = 0; i < timestamps.size(); i ++) {
            long timestamp = timestamps.get(i);
            LocalDate date = Instant.ofEpochSecond(timestamp)
                    .atZone(ZoneId.of("America/New_York"))
                    .toLocalDate();
            BigDecimal close = closes.get(i);
            prices.put(date, close);
        }
        // returns
        return prices;
    }

    /**
     * Gets dividends
     * @param asset asset
     * @return dividends
     */
    public Map<LocalDate, BigDecimal> getDividends(Asset asset) {
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
        Map<LocalDate, BigDecimal> dividends = new HashMap<>();
        for (Map.Entry<String, Map<String,Double>> entry : dividendsMap.entrySet()) {
            Map<String,Double> value = entry.getValue();
            LocalDate date = Instant.ofEpochSecond(value.get("date").longValue())
                    .atOffset(ZoneOffset.UTC)
                    .toLocalDate();
            BigDecimal dividend = BigDecimal.valueOf(value.get("amount"));
            dividends.put(date, dividend);
        }
        // returns
        return dividends;
    }

    /**
     * creates nasdaq http headers
     * @return http headers
     */
    HttpHeaders createNasdaqHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("authority","api.nasdaq.com");
        headers.add("origin","https://www.nasdaq.com");
        headers.add("referer","https://www.nasdaq.com");
        headers.add("sec-ch-ua","\"Chromium\";v=\"116\", \"Not)A;Brand\";v=\"24\", \"Google Chrome\";v=\"116\"");
        headers.add("sec-ch-ua-mobile","?0");
        headers.add("sec-ch-ua-platform", "macOS");
        headers.add("sec-fetch-dest","empty");
        headers.add("sec-fetch-mode","cors");
        headers.add("sec-fetch-site", "same-site");
        headers.add("user-agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
        return headers;
    }

    /**
     * creates yahoo finance http headers
     * @return http headers
     */
    HttpHeaders createYahooHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("authority"," query1.finance.yahoo.com");
        headers.add("Accept", "*/*");
        headers.add("origin", "https://finance.yahoo.com");
        headers.add("referer", "");
        headers.add("Sec-Ch-Ua","\"Chromium\";v=\"118\", \"Google Chrome\";v=\"118\", \"Not=A?Brand\";v=\"99\"");
        headers.add("Sec-Ch-Ua-Mobile","?0");
        headers.add("Sec-Ch-Ua-Platform", "macOS");
        headers.add("Sec-Fetch-Dest","document");
        headers.add("Sec-Fetch-Mode","navigate");
        headers.add("Sec-Fetch-Site", "none");
        headers.add("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
        return headers;
    }

    /**
     * converts string to number
     * @param value string
     * @return number
     */
    BigDecimal convertStringToNumber(String value) {
        if (value == null) {
            return null;
        }
        value = value.replace(",", "");
        try {
            return new BigDecimal(value);
        }catch(Throwable e){
            return null;
        }
    }

    /**
     * converts currency string to number
     * @param value currency string
     * @return currency number
     */
    BigDecimal convertCurrencyToNumber(String value) {
        if (value == null) {
            return null;
        }
        try {
            value = value.replace(CURRENCY_USD.getSymbol(), "");
            value = value.replace(",","");
            value = value.trim().isEmpty() ? "0" : value;
            return new BigDecimal(value);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * converts percentage string to number
     * @param value percentage string
     * @return percentage number
     */
    BigDecimal convertPercentageToNumber(String value) {
        value = value.replace("%", "");
        try {
            return new BigDecimal(value);
        }catch(Throwable e){
            return null;
        }
    }

}
