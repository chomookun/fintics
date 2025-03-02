package org.chomookun.fintics.core.dividend.client.market;

import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.chomookun.arch4j.core.common.support.RestTemplateBuilder;
import org.chomookun.fintics.core.dividend.client.DividendClient;
import org.chomookun.fintics.core.dividend.client.DividendClientProperties;
import org.chomookun.fintics.core.common.client.SeibroClientSupport;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.dividend.model.Dividend;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class KrDividendClient extends DividendClient implements SeibroClientSupport {

    private final RestTemplate restTemplate;

    /**
     * Constructor
     * @param dividendClientProperties dividend client properties
     */
    public KrDividendClient(DividendClientProperties dividendClientProperties) {
        super(dividendClientProperties);

        // rest template
        this.restTemplate = RestTemplateBuilder.create()
                .httpRequestRetryStrategy(new DefaultHttpRequestRetryStrategy())
                .build();
    }

    /**
     * Checks if the client supports the asset
     * @param asset asset
     * @return support or not
     */
    @Override
    public boolean isSupport(Asset asset) {
        return Objects.equals(asset.getMarket(), "KR");
    }

    @Override
    public List<Dividend> getDividends(Asset asset, LocalDate dateFrom, LocalDate dateTo) {
        return switch(asset.getType()) {
            case "STOCK" -> getStockDividends(asset, dateFrom, dateTo);
            case "ETF" -> getEtfDividends(asset, dateFrom, dateTo);
            default -> throw new IllegalArgumentException(String.format("Unsupported asset type: %s", asset.getType()));
        };
    }

    /**
     * Gets stock dividends
     * @param asset stock asset
     * @param dateFrom date from
     * @param dateTo date to
     * @return stock dividends
     */
    List<Dividend> getStockDividends(Asset asset, LocalDate dateFrom, LocalDate dateTo) {
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
                put("CMM_BTN_ABBR_NM", "allview,allview,print,hwp,word,pdf,searchIcon,search,xls,link,link,wide,wide,top,");
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
                    .filter(row -> Objects.equals(row.get("SHOTN_ISIN"), isin))     // 해당 종목 배당 정보 만 필터링
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
     * Gets ETF dividends
     * @param asset ETF asset
     * @param dateFrom date from
     * @param dateTo date to
     * @return ETF dividends
     */
    List<Dividend> getEtfDividends(Asset asset, LocalDate dateFrom, LocalDate dateTo) {
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
                put("CMM_BTN_ABBR_NM", "allview,allview,print,hwp,word,pdf,searchIcon,searchIcon,search,searchIcon,search,link,link,wide,wide,top,");
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
