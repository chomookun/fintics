package org.chomookun.fintics.client.asset.market;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.chomookun.arch4j.core.common.support.RestTemplateBuilder;
import org.chomookun.fintics.client.asset.AssetClient;
import org.chomookun.fintics.client.asset.AssetClientProperties;
import org.chomookun.fintics.model.Asset;
import org.chomookun.fintics.model.DividendProfit;
import org.chomookun.fintics.model.Ohlcv;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KrAssetClient extends AssetClient {

    private static final String MARKET_KR = "KR";

    private final RestTemplate restTemplate;

    public KrAssetClient(AssetClientProperties assetClientProperties) {
        super(assetClientProperties);

        // rest template
        this.restTemplate = RestTemplateBuilder.create()
                .httpRequestRetryStrategy(new DefaultHttpRequestRetryStrategy())
                .build();
    }

    /**
     * returns assets to trade
     * @return assets
     */
    @Override
    public List<Asset> getAssets() {
        List<Asset> assets = new ArrayList<>();
        assets.addAll(getStockAssetsByExchangeType("11")); // kospi
        assets.addAll(getStockAssetsByExchangeType("12")); // kosdaq
        assets.addAll(getEtfAssets());  // ETF
        return assets;
    }

    /**
     * returns asset list by exchange type
     * 코스피, 코스닥 여부에 따라 다른 부분이 존재 함.
     * @param exchangeType exchange type
     * @return assets
     * @see "https://seibro.or.kr/websquare/control.jsp?w2xPath=/IPORTAL/user/stock/BIP_CNTS02004V.xml&menuNo=41"
     */
    List<Asset> getStockAssetsByExchangeType(String exchangeType) {
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/stock/BIP_CNTS02004V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);

        String action = "SecnIssuPListEL1";
        String task = "ksd.safe.bip.cnts.Stock.process.SecnIssuPTask";
        Map<String,String> payloadMap = new LinkedHashMap<>(){{
            put("W2XPATH", w2xPath);
            put("MENU_NO","41");
            put("CMM_BTN_ABBR_NM","allview,allview,print,hwp,word,pdf,reset,reset,seach,favorites,xls,link,link,wide,wide,top,");
            put("FICS_CD", "");
            put("INDTP_CLSF_NO", "");
            put("STD_DT", "20230922");
            put("CALTOT_MART_TPCD", exchangeType);
            put("SECN_KACD", "99");
            put("AG_ORG_TPCD", "99");
            put("SETACC_MMDD", "99");
            put("ISSU_FORM", "");
            put("ORDER_BY", "TR_QTY");
            put("START_PAGE", "1");
            put("END_PAGE", "10000");
        }};
        String payloadXml = createSeibroPayloadXml(action, task, payloadMap);

        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        String responseBody = responseEntity.getBody();
        List<Map<String, String>> rows = convertSeibroXmlToList(responseBody);

        // sort
        rows.sort((o1, o2) -> {
            BigDecimal o1MarketCap = toNumber(o1.get("MARTP_TOTAMT"), BigDecimal.ZERO);
            BigDecimal o2MarketCap = toNumber(o2.get("MARTP_TOTAMT"), BigDecimal.ZERO);
            return o2MarketCap.compareTo(o1MarketCap);
        });

        // market, exchange
        String exchange;
        switch(exchangeType) {
            case "11" -> exchange = "XKRX";
            case "12" -> exchange = "XKOS";
            default -> throw new RuntimeException("invalid exchange type");
        }

        return rows.stream()
                .map(row -> {
                    return Asset.builder()
                            .assetId(toAssetId(MARKET_KR, row.get("SHOTN_ISIN")))
                            .name(row.get("KOR_SECN_NM"))
                            .market(MARKET_KR)
                            .exchange(exchange)
                            .type("STOCK")
                            .updatedDate(LocalDate.now())
                            .marketCap(toNumber(row.get("MARTP_TOTAMT"), null))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * returns ETF assets
     * @return ETF assets
     */
    List<Asset> getEtfAssets() {
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/etf/BIP_CNTS06025V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);

        String action = "secnIssuStatPList";
        String task = "ksd.safe.bip.cnts.etf.process.EtfSetredInfoPTask";
        Map<String,String> payloadMap = new LinkedHashMap<>(){{
            put("W2XPATH", w2xPath);
            put("MENU_NO","174");
            put("CMM_BTN_ABBR_NM","allview,allview,print,hwp,word,pdf,detail,seach,searchIcon,comparison,link,link,wide,wide,top,");
            put("mngco", "");
            put("SETUP_DT", "");
            put("from_TOT_RECM_RATE", "");
            put("to_TOT_RECM_RATE", "");
            put("from_NETASST_TOTAMT", "");
            put("to_NETASST_TOTAMT", "");
            put("kor_SECN_NM", "");
            put("ic4_select", "2");
            put("select_sorting", "2");
            put("START_PAGE", "1");
            put("END_PAGE", "10000");
        }};
        String payloadXml = createSeibroPayloadXml(action, task, payloadMap);

        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        String responseBody = responseEntity.getBody();
        List<Map<String, String>> rows = convertSeibroXmlToList(responseBody);

        // sort
        rows.sort((o1, o2) -> {
            BigDecimal o1MarketCap = toNumber(o1.get("NETASST_TOTAMT"), BigDecimal.ZERO);
            BigDecimal o2MarketCap = toNumber(o2.get("NETASST_TOTAMT"), BigDecimal.ZERO);
            return o2MarketCap.compareTo(o1MarketCap);
        });

        // market, exchange
        String exchange = "XKRX";

        // convert assets
        return rows.stream()
                .map(row -> {
                    // market cap (etf is 1 krw unit)
                    BigDecimal marketCap = toNumber(row.get("NETASST_TOTAMT"), null);
                    if(marketCap != null) {
                        marketCap = marketCap.divide(BigDecimal.valueOf(100_000_000), MathContext.DECIMAL32)
                                .setScale(0, RoundingMode.HALF_UP);
                    }

                    // return
                    return Asset.builder()
                            .assetId(toAssetId(MARKET_KR, row.get("SHOTN_ISIN")))
                            .name(row.get("KOR_SECN_NM"))
                            .market(MARKET_KR)
                            .exchange(exchange)
                            .type("ETF")
                            .marketCap(marketCap)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean isSupport(Asset asset) {
        return asset.getAssetId().startsWith("KR.");
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
        BigDecimal eps = null;
        BigDecimal roe = null;
        BigDecimal price = null;
        BigDecimal per = null;
        int dividendFrequency = 0;
        BigDecimal dividendYield = BigDecimal.ZERO;
        BigDecimal capitalGain = BigDecimal.ZERO;
        BigDecimal totalReturn = BigDecimal.ZERO;

        // sec info
        Map<String,String> secInfo = getSecInfo(asset);
        String issucoCustno = secInfo.get("ISSUCO_CUSTNO");

        // calculates TTM EPS
        // [투자지표](https://seibro.or.kr/websquare/control.jsp?w2xPath=/IPORTAL/user/company/BIP_CNTS01009V.xml&menuNo=10)
        try {
            String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
            String w2xPath = "/IPORTAL/user/company/BIP_CNTS01009V.xml&menuNo=10";
            HttpHeaders headers = createSeibroHeaders(w2xPath);
            headers.add("Content-Type", "application/xml;charset=UTF-8");
            String action = "invstIndexList";
            String task = "ksd.safe.bip.cnts.Company.process.EntrFnafInfoPTask";
            Map<String,String> payloadMap = new LinkedHashMap<>(){{
                put("MENU_NO", "10");
                put("ISSUCO_CUSTNO", issucoCustno);
                put("TO_YEAR", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy")));
                put("TYPE", "연결");
            }};
            String payloadXml = createSeibroPayloadXml(action, task, payloadMap);
            RequestEntity<String> requestEntity = RequestEntity.post(url)
                    .headers(headers)
                    .body(payloadXml);
            // exchange
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            // response
            String responseBody = responseEntity.getBody();
            List<Map<String,String>> responseList = convertSeibroXmlToList(responseBody);

            // calculates ttm eps
            Map<String,String> epsRow = responseList.stream().filter(row ->
                            row.get("HB").startsWith("EPS"))
                    .findFirst().orElseThrow();
            List<BigDecimal> ttmEpses = new ArrayList<>();
            for (int i = 1; i < 20; i++ ) {
                String name = String.format("A%s", i);
                String value = epsRow.get(name);
                if (i%5 == 1) {     // 1, 6, 11, 16는 연간 합산 자료임 (왜 이따구..)
                    continue;
                }
                if (value != null && value.trim().length() > 0) {
                    ttmEpses.add(toNumber(value, BigDecimal.ZERO));
                }
                if (ttmEpses.size() >= 4) {
                    break;
                }
            }
            eps = ttmEpses.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.FLOOR);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // calculates TTM ROE
        // [재무비율](https://seibro.or.kr/websquare/control.jsp?w2xPath=/IPORTAL/user/company/BIP_CNTS01008V.xml&menuNo=9)
        try {
            String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
            String w2xPath = "/IPORTAL/user/company/BIP_CNTS01009V.xml&menuNo=10";
            HttpHeaders headers = createSeibroHeaders(w2xPath);
            headers.add("Content-Type", "application/xml;charset=UTF-8");
            String action = "fnafRatioList";
            String task = "ksd.safe.bip.cnts.Company.process.EntrFnafInfoPTask";
            Map<String,String> payloadMap = new LinkedHashMap<>(){{
                put("MENU_NO", "9");
                put("ISSUCO_CUSTNO", issucoCustno);
                put("TO_YEAR", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy")));
                put("TYPE", "연결");
            }};
            String payloadXml = createSeibroPayloadXml(action, task, payloadMap);
            RequestEntity<String> requestEntity = RequestEntity.post(url)
                    .headers(headers)
                    .body(payloadXml);
            // exchange
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            // response
            String responseBody = responseEntity.getBody();
            List<Map<String,String>> responseList = convertSeibroXmlToList(responseBody);

            // calculates ttm eps
            Map<String,String> roeRow = responseList.stream().filter(row ->
                            row.get("HB").contentEquals("ROE"))
                    .findFirst().orElseThrow();
            List<BigDecimal> ttmRoes = new ArrayList<>();
            for (int i = 1; i < 20; i++ ) {
                String name = String.format("A%s", i);
                String value = roeRow.get(name);
                if (i%5 == 1) {     // 1, 6, 11, 16는 연간 합산 자료임 (왜 이따구..)
                    continue;
                }
                if (value != null && value.trim().length() > 0) {
                    ttmRoes.add(toNumber(value, BigDecimal.ZERO));
                }
                if (ttmRoes.size() >= 4) {
                    break;
                }
            }
            roe = ttmRoes.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(ttmRoes.size()), MathContext.DECIMAL32)
                    .setScale(2, RoundingMode.FLOOR);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // price
        Map<LocalDate, BigDecimal> prices = getStockPrices(asset);
        price = prices.entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElseThrow();

        // calculates PER
        if (eps.compareTo(BigDecimal.ZERO) <= 0) {
            per = BigDecimal.valueOf(9_999);
        } else {
            per = price.divide(eps, MathContext.DECIMAL32)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // calculates dividend
        Map<LocalDate, BigDecimal> dividends = getStockDividends(asset);
        dividendFrequency = dividends.size();
        BigDecimal dividendPerShare = dividends.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dividendYield = dividendPerShare.divide(price, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // capital gain
        BigDecimal startClose = prices.entrySet().stream()
                .min(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(BigDecimal.ZERO);
        BigDecimal endClose = prices.entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(BigDecimal.ZERO);
        capitalGain = endClose.subtract(startClose)
                .divide(startClose, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // total return
        totalReturn = capitalGain.add(dividendYield)
                .setScale(2, RoundingMode.HALF_UP);

        // updates asset
        asset.setEps(eps);
        asset.setRoe(roe);
        asset.setPer(per);
        asset.setPrice(price);
        asset.setDividendFrequency(dividendFrequency);
        asset.setDividendYield(dividendYield);
        asset.setCapitalGain(capitalGain);
        asset.setTotalReturn(totalReturn);
    }

    /**
     * Returns stock ohlcvs
     * @param asset asset
     * @return ohlcvs
     */
    Map<LocalDate, BigDecimal> getStockPrices(Asset asset) {
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now().minusDays(1);
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/stock/BIP_CNTS02007V.xml&menuNo=45";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);
        String action = "secnInfoDDPList";
        String task = "ksd.safe.bip.cnts.Stock.process.SecnInfoPTask";
        Map<String,String> secInfo = getSecInfo(asset);
        String isin = secInfo.get("ISIN");
        Map<LocalDate, BigDecimal> prices = new LinkedHashMap<>();
        int startPage = 1;
        int endPage = 100;
        for (int i = 0; i < 100; i ++) {
            int finalStartPage = startPage;
            int finalEndPage = endPage;
            Map<String,String> payloadMap = new LinkedHashMap<>(){{
                put("W2XPATH", w2xPath);
                put("MENU_NO","45");
                put("ISIN", isin);
                put("START_DATE", dateFrom.format(DateTimeFormatter.BASIC_ISO_DATE));
                put("END_DATE", dateTo.format(DateTimeFormatter.BASIC_ISO_DATE));
                put("START_PAGE", String.valueOf(finalStartPage));
                put("END_PAGE", String.valueOf(finalEndPage));
            }};
            String payloadXml = createSeibroPayloadXml(action, task, payloadMap);
            RequestEntity<String> requestEntity = RequestEntity.post(url)
                    .headers(headers)
                    .body(payloadXml);
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            String responseBody = responseEntity.getBody();
            List<Map<String, String>> rows = convertSeibroXmlToList(responseBody);
            rows.forEach(row -> {
                LocalDate date = LocalDate.parse(row.get("SECN_PRICE_STD_DT"), DateTimeFormatter.BASIC_ISO_DATE);
                BigDecimal price = new BigDecimal(row.get("CURDAY_CPRI"));
                prices.put(date, price);
            });

            // check next page
            if (rows.size() < 100) {
                break;
            } else {
                startPage += 100;
                endPage += 100;
            }
        }
        // returns
        return prices;
    }

    /**
     * returns stock dividends
     * @param asset asset
     * @return dividends
     * @see [배당내역전체검색] (https://seibro.or.kr/websquare/control.jsp?w2xPath=/IPORTAL/user/company/BIP_CNTS01041V.xml&menuNo=285)
     */
    Map<LocalDate, BigDecimal> getStockDividends(Asset asset) {
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now().minusDays(1);
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/company/BIP_CNTS01041V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);
        String action = "divStatInfoPList";
        String task = "ksd.safe.bip.cnts.Company.process.EntrFnafInfoPTask";
        Map<String,String> secInfo = getSecInfo(asset);
        String issucoCustno = secInfo.get("ISSUCO_CUSTNO");
        String isin = secInfo.get("SHOTN_ISIN");
        Map<String,String> payloadMap = new LinkedHashMap<>(){{
            put("W2XPATH", w2xPath);
            put("MENU_NO","285");
            put("CMM_BTN_ABBR_NM","allview,allview,print,hwp,word,pdf,searchIcon,seach,xls,link,link,wide,wide,top,");
            put("ISSUCO_CUSTNO", issucoCustno);
            put("RGT_RSN_DTAIL_SORT_CD", "02");     // 현금 배당
            put("RGT_STD_DT_FROM", dateFrom.format(DateTimeFormatter.BASIC_ISO_DATE));
            put("RGT_STD_DT_TO", dateTo.format(DateTimeFormatter.BASIC_ISO_DATE));
            put("START_PAGE", String.valueOf(1));
            put("END_PAGE", String.valueOf(30));
        }};
        String payloadXml = createSeibroPayloadXml(action, task, payloadMap);
        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        List<Map<String,String>> rows = convertSeibroXmlToList(responseBody);
        Map<LocalDate, BigDecimal> dividends = new HashMap<>();
        rows.stream()
                .filter(row -> Objects.equals(row.get("SHOTN_ISIN"), isin))     // 해당 종목 배당정보만 필터링
                .forEach(row -> {
                    LocalDate date = LocalDate.parse(row.get("RGT_STD_DT"), DateTimeFormatter.BASIC_ISO_DATE);
                    BigDecimal dividend = new BigDecimal(row.get("CASH_ALOC_AMT"));
                    dividends.put(date, dividend);
                });
        return dividends;
    }

    void updateEtfAsset(Asset asset) {
        BigDecimal price = null;
        int dividendFrequency = 0;
        BigDecimal dividendYield = BigDecimal.ZERO;
        BigDecimal capitalGain = BigDecimal.ZERO;
        BigDecimal totalReturn = BigDecimal.ZERO;

        // price
        Map<LocalDate, BigDecimal> prices = getEtfPrices(asset);
        price = prices.entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElseThrow();

        // calculates dividend
        Map<LocalDate, BigDecimal> dividends = getEtfDividends(asset);
        dividendFrequency = dividends.size();
        BigDecimal dividendPerShare = dividends.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dividendYield = dividendPerShare.divide(price, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // capital gain
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

        // updates asset
        asset.setDividendFrequency(dividendFrequency);
        asset.setDividendYield(dividendYield);
        asset.setCapitalGain(capitalGain);
        asset.setTotalReturn(totalReturn);
    }

    /**
     * Returns ETF ohlcvs
     * @param asset asset
     * @return etf ohlcvs
     * @see [기준가추이] (https://seibro.or.kr/websquare/control.jsp?w2xPath=/IPORTAL/user/etf/BIP_CNTS06033V.xml&menuNo=182)
     */
    Map<LocalDate, BigDecimal> getEtfPrices(Asset asset) {
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now().minusDays(1);
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/etf/BIP_CNTS06033V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);
        String action = "compstInfoStkDayprcList";
        String task = "ksd.safe.bip.cnts.etf.process.EtfCompstInfoPTask";
        Map<String,String> secInfo = getSecInfo(asset);
        String isin = secInfo.get("ISIN");
        Map<LocalDate, BigDecimal> prices = new HashMap<>();
        int startPage = 1;
        int endPage = 100;
        for (int i = 0; i < 100; i ++) {
            int finalStartPage = startPage;
            int finalEndPage = endPage;
            Map<String,String> payloadMap = new LinkedHashMap<>(){{
                put("W2XPATH", w2xPath);
                put("MENU_NO","182");
                put("CMM_BTN_ABBR_NM","allview,allview,print,hwp,word,pdf,searchIcon,seach,favorites float_left,search02,search02,link,link,wide,wide,top,");
                put("isin", isin);
                put("RGT_RSN_DTAIL_SORT_CD", "11");
                put("fromDt", dateFrom.format(DateTimeFormatter.BASIC_ISO_DATE));
                put("toDt", dateTo.format(DateTimeFormatter.BASIC_ISO_DATE));
                put("START_PAGE", String.valueOf(finalStartPage));
                put("END_PAGE", String.valueOf(finalEndPage));
            }};
            String payloadXml = createSeibroPayloadXml(action, task, payloadMap);
            RequestEntity<String> requestEntity = RequestEntity.post(url)
                    .headers(headers)
                    .body(payloadXml);
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            String responseBody = responseEntity.getBody();
            List<Map<String, String>> rows = convertSeibroXmlToList(responseBody);
            rows.forEach(row -> {
                LocalDate date = LocalDate.parse(row.get("STD_DT"), DateTimeFormatter.BASIC_ISO_DATE);
                BigDecimal price = new BigDecimal(row.get("CPRI"));
                prices.put(date, price);
            });
            // check next page
            if (rows.size() < 100) {
                break;
            } else {
                startPage += 100;
                endPage += 100;
            }
        }
        // returns
        return prices;
    }

    /**
     * returns etf dividends
     * @param asset asset
     * @return dividends
     * @see [분배금지급현황] (https://seibro.or.kr/websquare/control.jsp?w2xPath=/IPORTAL/user/etf/BIP_CNTS06030V.xml&menuNo=179)
     */
    Map<LocalDate, BigDecimal> getEtfDividends(Asset asset) {
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now().minusDays(1);
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/etf/BIP_CNTS06030V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);
        String action = "exerInfoDtramtPayStatPlist";
        String task = "ksd.safe.bip.cnts.etf.process.EtfExerInfoPTask";
        Map<String,String> secInfo = getSecInfo(asset);
        String isin = secInfo.get("ISIN");
        Map<String,String> payloadMap = new LinkedHashMap<>(){{
            put("W2XPATH", w2xPath);
            put("MENU_NO","179");
            put("CMM_BTN_ABBR_NM","allview,allview,print,hwp,word,pdf,searchIcon,searchIcon,seach,searchIcon,seach,link,link,wide,wide,top,");
            put("isin", isin);
            put("RGT_RSN_DTAIL_SORT_CD", "11");
            put("fromRGT_STD_DT", dateFrom.format(DateTimeFormatter.BASIC_ISO_DATE));
            put("toRGT_STD_DT", dateTo.format(DateTimeFormatter.BASIC_ISO_DATE));
            put("START_PAGE", String.valueOf(1));
            put("END_PAGE", String.valueOf(30));
        }};
        String payloadXml = createSeibroPayloadXml(action, task, payloadMap);
        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        List<Map<String,String>> rows = convertSeibroXmlToList(responseBody);
        Map<LocalDate, BigDecimal> dividends = new HashMap<>();
        rows.forEach(row -> {
            LocalDate date = LocalDate.parse(row.get("RGT_STD_DT"), DateTimeFormatter.BASIC_ISO_DATE);
            BigDecimal dividend = new BigDecimal(row.get("ESTM_STDPRC"));
            dividends.put(date, dividend);
        });
        return dividends;
    }

    /**
     * gets SEC info
     * @param asset asset
     * @return sec info
     */
    Map<String, String> getSecInfo(Asset asset) {
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/stock/BIP_CNTS02006V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);
        String action = "secnInfoDefault";
        String task = "ksd.safe.bip.cnts.Stock.process.SecnInfoPTask";
        Map<String,String> payloadMap = new LinkedHashMap<>(){{
            put("W2XPATH", w2xPath);
            put("SHOTN_ISIN", asset.getSymbol());
        }};
        String payloadXml = createSeibroPayloadXml(action, task, payloadMap);
        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        return convertSeibroXmlToMap(responseBody);
    }

    /**
     * returns seibro api header
     * @param w2xPath w2xPath
     * @return http headers for seibro
     */
    HttpHeaders createSeibroHeaders(String w2xPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/xml");
        headers.add("Origin","https://seibro.or.kr");
        headers.add("Referer","https://seibro.or.kr/websquare/control.jsp?w2xPath=" + w2xPath);
        return headers;
    }

    /**
     * creates payload XML string
     * @param action seibro api action
     * @param task seibro api task
     * @param payloadMap payload map
     * @return payload XML string
     */
    static String createSeibroPayloadXml(String action, String task, Map<String,String> payloadMap) {
        // Create a new Document
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder ;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        }catch(Throwable e){
            throw new RuntimeException(e);
        }
        Document doc = dBuilder.newDocument();

        // Create the root element <reqParam>
        Element reqParamElement = doc.createElement("reqParam");
        doc.appendChild(reqParamElement);

        // Add attributes to <reqParam>
        Attr actionAttr = doc.createAttribute("action");
        actionAttr.setValue(action);
        reqParamElement.setAttributeNode(actionAttr);

        Attr taskAttr = doc.createAttribute("task");
        taskAttr.setValue(task);
        reqParamElement.setAttributeNode(taskAttr);

        // Add child elements to <reqParam>
        for(String key : payloadMap.keySet()) {
            String value = payloadMap.get(key);
            Element childElement = doc.createElement(key);
            Attr attr = doc.createAttribute("value");
            attr.setValue(value);
            childElement.setAttributeNode(attr);
            reqParamElement.appendChild(childElement);
        }

        // convert to string
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(reqParamElement), new StreamResult(writer));
            return writer.toString();
        }catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * convert seibro xml response to map
     * @param responseXml response xml
     * @return map
     */
    Map<String, String> convertSeibroXmlToMap(String responseXml) {
        Map<String, String> map  = new LinkedHashMap<>();
        InputSource inputSource;
        StringReader stringReader;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            stringReader = new StringReader(responseXml);
            inputSource = new InputSource(stringReader);
            Document document = builder.parse(inputSource);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            XPathExpression expr = xPath.compile("/result/*");
            NodeList propertyNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for(int i = 0; i < propertyNodes.getLength(); i++) {
                Element propertyElement = (Element) propertyNodes.item(i);
                String propertyName = propertyElement.getTagName();
                String propertyValue = propertyElement.getAttribute("value");
                map.put(propertyName, propertyValue);
            }
        }catch(Throwable e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /**
     * convert seibro response XML to list
     * @param responseXml response XML
     * @return list of seibro response map
     */
    static List<Map<String, String>> convertSeibroXmlToList(String responseXml) {
        List<Map<String,String>> list = new ArrayList<>();
        InputSource inputSource;
        StringReader stringReader;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            stringReader = new StringReader(responseXml);
            inputSource = new InputSource(stringReader);
            Document document = builder.parse(inputSource);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            Double count = (Double) xPath.evaluate("count(//vector)", document, XPathConstants.NUMBER);
            if(count.intValue() == 0) {
                throw new RuntimeException("response body error - vector element count is 0.");
            }

            XPathExpression expr = xPath.compile("//vector/data/result");
            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for(int i = 0; i < nodeList.getLength(); i++) {
                Map<String, String> map = new LinkedHashMap<>();
                Node result = nodeList.item(i);
                NodeList propertyNodes = result.getChildNodes();
                for(int ii = 0; ii < propertyNodes.getLength(); ii++) {
                    Element propertyElement = (Element) propertyNodes.item(ii);
                    String propertyName = propertyElement.getTagName();
                    String propertyValue = propertyElement.getAttribute("value");
                    map.put(propertyName, propertyValue);
                }
                list.add(map);
            }

        }catch(Throwable e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /**
     * convert to string to number
     * @param value number string
     * @param defaultValue default number
     * @return converted number
     */
    BigDecimal toNumber(Object value, BigDecimal defaultValue) {
        try {
            String valueString = value.toString().replace(",", "");
            return new BigDecimal(valueString);
        }catch(Throwable e){
            return defaultValue;
        }
    }

}
