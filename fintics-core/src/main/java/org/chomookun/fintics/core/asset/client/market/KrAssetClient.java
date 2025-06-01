package org.chomookun.fintics.core.asset.client.market;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.chomookun.arch4j.core.common.support.RestTemplateBuilder;
import org.chomookun.fintics.core.asset.client.AssetClient;
import org.chomookun.fintics.core.asset.client.AssetClientProperties;
import org.chomookun.fintics.core.common.client.SeibroClientSupport;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.dividend.model.Dividend;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KrAssetClient extends AssetClient implements SeibroClientSupport {

    private static final String MARKET_KR = "KR";

    private final RestTemplate restTemplate;

    /**
     * Constructor
     * @param assetClientProperties asset client properties
     */
    public KrAssetClient(AssetClientProperties assetClientProperties) {
        super(assetClientProperties);
        // rest template
        this.restTemplate = RestTemplateBuilder.create()
                .httpRequestRetryStrategy(new DefaultHttpRequestRetryStrategy())
                .build();
    }

    /**
     * Returns assets to trade
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
     * Returns asset list by exchange type
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
            BigDecimal o1MarketCap = convertStringToNumber(o1.get("MARTP_TOTAMT"), BigDecimal.ZERO);
            BigDecimal o2MarketCap = convertStringToNumber(o2.get("MARTP_TOTAMT"), BigDecimal.ZERO);
            return o2MarketCap.compareTo(o1MarketCap);
        });
        // market, exchange
        String exchange;
        switch(exchangeType) {
            case "11" -> exchange = "XKRX";
            case "12" -> exchange = "XKOS";
            default -> throw new RuntimeException("invalid exchange type");
        }

        // returns
        return rows.stream()
                .map(row -> {
                    // sector, industry
                    String sector = row.get("BIG_CD_NM");
                    String industry = row.get("MID_CD_NM");
                    return Asset.builder()
                            .assetId(toAssetId(MARKET_KR, row.get("SHOTN_ISIN")))
                            .name(row.get("KOR_SECN_NM"))
                            .market(MARKET_KR)
                            .exchange(exchange)
                            .type("STOCK")
                            .sector(sector)
                            .industry(industry)
                            .updatedDate(LocalDate.now())
                            .marketCap(convertStringToNumber(row.get("MARTP_TOTAMT"), null))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns ETF assets
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
            BigDecimal o1MarketCap = convertStringToNumber(o1.get("NETASST_TOTAMT"), BigDecimal.ZERO);
            BigDecimal o2MarketCap = convertStringToNumber(o2.get("NETASST_TOTAMT"), BigDecimal.ZERO);
            return o2MarketCap.compareTo(o1MarketCap);
        });
        // market, exchange
        String exchange = "XKRX";
        // converts and returns
        return rows.stream()
                .map(row -> {
                    // market cap (etf is 1 krw unit)
                    BigDecimal marketCap = convertStringToNumber(row.get("NETASST_TOTAMT"), null);
                    if(marketCap != null) {
                        marketCap = marketCap.divide(BigDecimal.valueOf(100_000_000), MathContext.DECIMAL32)
                                .setScale(0, RoundingMode.HALF_UP);
                    }

                    // sector
                    String sector = null;
                    String etfBigSortNm = row.get("ETF_BIG_SORT_NM");
                    if (etfBigSortNm != null) {
                        sector = etfBigSortNm.split("/")[0].trim();
                    }

                    // industry
                    String industry = row.get("ETF_SORT_NM");

                    // return
                    return Asset.builder()
                            .assetId(toAssetId(MARKET_KR, row.get("SHOTN_ISIN")))
                            .name(row.get("KOR_SECN_NM"))
                            .market(MARKET_KR)
                            .exchange(exchange)
                            .type("ETF")
                            .sector(sector)
                            .industry(industry)
                            .marketCap(marketCap)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns whether asset is supported
     * @param asset asset
     * @return true if supported
     */
    @Override
    public boolean isSupport(Asset asset) {
        return asset.getAssetId().startsWith("KR.");
    }

    /**
     * Populates asset
     * @param asset asset
     */
    @Override
    public void populateAsset(Asset asset) {
        switch(Optional.ofNullable(asset.getType()).orElse("")) {
            case "STOCK" -> populateStockAsset(asset);
            case "ETF" -> populateEtfAsset(asset);
            default -> throw new RuntimeException("Unsupported asset type");
        }
    }

    /**
     * Updates stock asset
     * @param asset stock asset
     */
    void populateStockAsset(Asset asset) {
        BigDecimal price = null;
        BigDecimal volume = null;
        BigDecimal marketCap = asset.getMarketCap();
        BigDecimal eps = null;
        BigDecimal roe = null;
        BigDecimal per = null;
        int dividendFrequency = 0;
        BigDecimal dividendYield = BigDecimal.ZERO;
        BigDecimal capitalGain = BigDecimal.ZERO;
        BigDecimal totalReturn = BigDecimal.ZERO;
        // sec info
        Map<String,String> secInfo = getSeibroSecInfo(asset, restTemplate);
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
                    ttmEpses.add(convertStringToNumber(value, BigDecimal.ZERO));
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
                    ttmRoes.add(convertStringToNumber(value, BigDecimal.ZERO));
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
        // price,volume
        List<Ohlcv> ohlcvs = getStockOhlcvs(asset);
        price = ohlcvs.get(0).getClose();
        volume = ohlcvs.get(0).getVolume();
        // calculates PER
        if (eps.compareTo(BigDecimal.ZERO) <= 0) {
            per = BigDecimal.valueOf(9_999);
        } else {
            per = price.divide(eps, MathContext.DECIMAL32)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        // calculates dividend
        List<Dividend> dividends = getStockDividends(asset);
        dividendFrequency = dividends.size();
        BigDecimal dividendPerShare = dividends.stream()
                .map(Dividend::getDividendPerShare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dividendYield = dividendPerShare.divide(price, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        // capital gain
        BigDecimal startClose = ohlcvs.get(ohlcvs.size()-1).getClose();
        BigDecimal endClose =  ohlcvs.get(0).getClose();
        capitalGain = endClose.subtract(startClose)
                .divide(startClose, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        // total return
        totalReturn = capitalGain.add(dividendYield)
                .setScale(2, RoundingMode.HALF_UP);
        // updates asset
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

    /**
     * Returns stock ohlcvs
     * @param asset asset
     * @return ohlcvs
     * @see <a href="https://seibro.or.kr/websquare/control.jsp?w2xPath=/IPORTAL/user/stock/BIP_CNTS02007V.xml&menuNo=45">일자별시세</a>
     */
    List<Ohlcv> getStockOhlcvs(Asset asset) {
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now();
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/stock/BIP_CNTS02007V.xml&menuNo=45";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);
        String action = "secnInfoDDPList";
        String task = "ksd.safe.bip.cnts.Stock.process.SecnInfoPTask";
        Map<String,String> secInfo = getSeibroSecInfo(asset, restTemplate);
        String isin = secInfo.get("ISIN");
        List<Ohlcv> ohlcvs = new ArrayList<>();
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
                LocalDateTime dateTime = LocalDate.parse(row.get("SECN_PRICE_STD_DT"), DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay();
                BigDecimal open = new BigDecimal(row.get("MARTP"));
                BigDecimal high = new BigDecimal(row.get("HGPRC"));
                BigDecimal low = new BigDecimal(row.get("LWPRC"));
                BigDecimal close = new BigDecimal(row.get("CURDAY_CPRI"));
                BigDecimal volume = new BigDecimal(row.get("TR_QTY"));
                Ohlcv ohlcv = Ohlcv.builder()
                        .assetId(asset.getAssetId())
                        .dateTime(dateTime)
                        .timeZone(ZoneId.of("Asia/Seoul"))
                        .type(Ohlcv.Type.DAILY)
                        .dateTime(dateTime)
                        .open(open)
                        .high(high)
                        .low(low)
                        .close(close)
                        .volume(volume)
                        .build();
                ohlcvs.add(ohlcv);
            });
            // check next page
            if (rows.size() < 100) {
                break;
            } else {
                startPage += 100;
                endPage += 100;
            }
        }
        // sort
        ohlcvs.sort(Comparator.comparing(Ohlcv::getDateTime).reversed());
        // returns
        return ohlcvs;
    }

    /**
     * Returns stock dividends
     * @param asset asset
     * @return dividends
     * @see <a href="https://seibro.or.kr/websquare/control.jsp?w2xPath=/IPORTAL/user/company/BIP_CNTS01041V.xml&menuNo=285">배당내역전체검색</a>
     */
    List<Dividend> getStockDividends(Asset asset) {
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now();
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/company/BIP_CNTS01041V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);
        String action = "divStatInfoPList";
        String task = "ksd.safe.bip.cnts.Company.process.EntrFnafInfoPTask";
        Map<String,String> secInfo = getSeibroSecInfo(asset, restTemplate);
        String issucoCustno = secInfo.get("ISSUCO_CUSTNO");
        String isin = secInfo.get("SHOTN_ISIN");
        List<Dividend> dividends = new ArrayList<>();
        int startPage = 1;
        int endPage = 100;
        for (int i = 0; i < 100; i ++) {
            int finalStartPage = startPage;
            int finalEndPage = endPage;
            Map<String, String> payloadMap = new LinkedHashMap<>() {{
                put("W2XPATH", w2xPath);
                put("MENU_NO", "285");
                put("CMM_BTN_ABBR_NM", "allview,allview,print,hwp,word,pdf,searchIcon,seach,xls,link,link,wide,wide,top,");
                put("ISSUCO_CUSTNO", issucoCustno);
                put("RGT_RSN_DTAIL_SORT_CD", "02");     // 현금 배당
                put("RGT_STD_DT_FROM", dateFrom.format(DateTimeFormatter.BASIC_ISO_DATE));
                put("RGT_STD_DT_TO", dateTo.format(DateTimeFormatter.BASIC_ISO_DATE));
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
            rows.stream()
                    .filter(row -> Objects.equals(row.get("SHOTN_ISIN"), isin))     // 해당 종목 배당정보만 필터링
                    .forEach(row -> {
                        LocalDate date = LocalDate.parse(row.get("RGT_STD_DT"), DateTimeFormatter.BASIC_ISO_DATE);
                        BigDecimal dividendPerShare = new BigDecimal(row.get("CASH_ALOC_AMT"));
                        Dividend dividend = Dividend.builder()
                                .assetId(asset.getAssetId())
                                .date(date)
                                .dividendPerShare(dividendPerShare)
                                .build();
                        dividends.add(dividend);
                    });
            // check next page
            if (rows.size() < 100) {
                break;
            } else {
                startPage += 100;
                endPage += 100;
            }
        }
        // sort
        dividends.sort(Comparator.comparing(Dividend::getDate).reversed());
        // returns
        return dividends;
    }

    /**
     * Populates ETF asset
     * @param asset ETF asset
     */
    void populateEtfAsset(Asset asset) {
        BigDecimal price;
        BigDecimal volume;
        BigDecimal marketCap = asset.getMarketCap();
        int dividendFrequency = 0;
        BigDecimal dividendYield = BigDecimal.ZERO;
        BigDecimal capitalGain = BigDecimal.ZERO;
        BigDecimal totalReturn = BigDecimal.ZERO;
        // price
        List<Ohlcv> ohlcvs = getEtfOhlcvs(asset);
        price = ohlcvs.get(0).getClose();
        volume = ohlcvs.get(0).getVolume();
        // calculates dividend
        List<Dividend> dividends = getEtfDividends(asset);
        dividendFrequency = dividends.size();
        BigDecimal dividendPerShare = dividends.stream()
                .map(Dividend::getDividendPerShare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dividendYield = dividendPerShare.divide(price, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        // capital gain
        BigDecimal startPrice = ohlcvs.get(ohlcvs.size() - 1).getClose();
        BigDecimal endPrice = ohlcvs.get(0).getClose();
        capitalGain = endPrice.subtract(startPrice)
                .divide(startPrice, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        // total return
        totalReturn = capitalGain.add(dividendYield)
                .setScale(2, RoundingMode.HALF_UP);
        // updates asset
        asset.setPrice(price);
        asset.setVolume(volume);
        asset.setMarketCap(marketCap);
        asset.setDividendFrequency(dividendFrequency);
        asset.setDividendYield(dividendYield);
        asset.setCapitalGain(capitalGain);
        asset.setTotalReturn(totalReturn);
    }

    /**
     * Returns ETF ohlcvs
     * @param asset asset
     * @return etf ohlcvs
     * @see <a href="https://seibro.or.kr/websquare/control.jsp?w2xPath=/IPORTAL/user/etf/BIP_CNTS06033V.xml&menuNo=182">기준가추이</a>
     */
    List<Ohlcv> getEtfOhlcvs(Asset asset) {
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now();
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/etf/BIP_CNTS06033V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);
        String action = "compstInfoStkDayprcList";
        String task = "ksd.safe.bip.cnts.etf.process.EtfCompstInfoPTask";
        Map<String,String> secInfo = getSeibroSecInfo(asset, restTemplate);
        String isin = secInfo.get("ISIN");
        List<Ohlcv> ohlcvs = new ArrayList<>();
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
                LocalDateTime dateTime = LocalDate.parse(row.get("STD_DT"), DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay();
                BigDecimal open = new BigDecimal(row.get("MARTP"));
                BigDecimal high = new BigDecimal(row.get("HGPRC"));
                BigDecimal low = new BigDecimal(row.get("LWPRC"));
                BigDecimal close = new BigDecimal(row.get("CPRI"));
                BigDecimal volume = new BigDecimal(row.get("TR_QTY"));
                Ohlcv ohlcv = Ohlcv.builder()
                        .assetId(asset.getAssetId())
                        .dateTime(dateTime)
                        .timeZone(ZoneId.of("Asia/Seoul"))
                        .type(Ohlcv.Type.DAILY)
                        .dateTime(dateTime)
                        .open(open)
                        .high(high)
                        .low(low)
                        .close(close)
                        .volume(volume)
                        .build();
                ohlcvs.add(ohlcv);
            });
            // check next page
            if (rows.size() < 100) {
                break;
            } else {
                startPage += 100;
                endPage += 100;
            }
        }
        // sort
        ohlcvs.sort(Comparator.comparing(Ohlcv::getDateTime).reversed());
        // returns
        return ohlcvs;
    }

    /**
     * Returns etf dividends
     * @param asset asset
     * @return dividends
     * @see <a href="https://seibro.or.kr/websquare/control.jsp?w2xPath=/IPORTAL/user/etf/BIP_CNTS06030V.xml&menuNo=179">분배금지급현황</a>
     */
    List<Dividend> getEtfDividends(Asset asset) {
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now();
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/etf/BIP_CNTS06030V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);
        String action = "exerInfoDtramtPayStatPlist";
        String task = "ksd.safe.bip.cnts.etf.process.EtfExerInfoPTask";
        Map<String,String> secInfo = getSeibroSecInfo(asset, restTemplate);
        String isin = secInfo.get("ISIN");
        List<Dividend> dividends = new ArrayList<>();
        int startPage = 1;
        int endPage = 100;
        for (int i = 0; i < 100; i ++) {
            int finalStartPage = startPage;
            int finalEndPage = endPage;
            Map<String, String> payloadMap = new LinkedHashMap<>() {{
                put("W2XPATH", w2xPath);
                put("MENU_NO", "179");
                put("CMM_BTN_ABBR_NM", "allview,allview,print,hwp,word,pdf,searchIcon,searchIcon,seach,searchIcon,seach,link,link,wide,wide,top,");
                put("isin", isin);
                put("RGT_RSN_DTAIL_SORT_CD", "11");
                put("fromRGT_STD_DT", dateFrom.format(DateTimeFormatter.BASIC_ISO_DATE));
                put("toRGT_STD_DT", dateTo.format(DateTimeFormatter.BASIC_ISO_DATE));
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
                LocalDate date = LocalDate.parse(row.get("RGT_STD_DT"), DateTimeFormatter.BASIC_ISO_DATE);
                BigDecimal dividendPerShare = new BigDecimal(row.get("ESTM_STDPRC"));
                Dividend dividend = Dividend.builder()
                        .assetId(asset.getAssetId())
                        .date(date)
                        .dividendPerShare(dividendPerShare)
                        .build();
                dividends.add(dividend);
            });
            // check next page
            if (rows.size() < 100) {
                break;
            } else {
                startPage += 100;
                endPage += 100;
            }
        }
        // sort
        dividends.sort(Comparator.comparing(Dividend::getDate).reversed());
        // returns
        return dividends;
    }

}
