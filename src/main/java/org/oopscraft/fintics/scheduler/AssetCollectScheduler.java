package org.oopscraft.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.arch4j.core.support.ValueMap;
import org.oopscraft.fintics.dao.AssetEntity;
import org.oopscraft.fintics.dao.AssetRepository;
import org.oopscraft.fintics.model.AssetType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.persistence.EntityManager;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetCollectScheduler {

    @Value("${fintics.scheduler.asset-collect-scheduler.limit:100}")
    private Integer limit = 100;

    private final EntityManager entityManager;

    private final AssetRepository assetRepository;

    @Scheduled(initialDelay = 3_000, fixedDelay = 360_000 * 24)
    @Transactional
    public void collectAssets() {
        log.info("Start collect assets.");
        LocalDateTime collectedAt = LocalDateTime.now();

        collectEtfAssets(collectedAt);

        collectStockAssets(collectedAt);

        // delete not merged
        entityManager.createQuery(
                        "delete from AssetEntity where collectedAt <> :collectedAt"
                )
                .setParameter("collectedAt", collectedAt)
                .executeUpdate();
        entityManager.flush();
        log.info("End collect assets.");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void collectStockAssets(LocalDateTime collectedAt) {
        collectStockAssetsByMarketType(collectedAt, "11");    // kospi
        collectStockAssetsByMarketType(collectedAt, "12");    // kosdaq
    }

    private void collectStockAssetsByMarketType(LocalDateTime collectedAt, String marketType) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .readTimeout(30_000)
                .build();

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
            put("CALTOT_MART_TPCD", marketType);
            put("SECN_KACD", "99");
            put("AG_ORG_TPCD", "99");
            put("SETACC_MMDD", "99");
            put("ISSU_FORM", "");
            put("ORDER_BY", "TR_QTY");
            put("START_PAGE", "1");
            put("END_PAGE", "10000");
        }};
        String payloadXml = createPayloadXml(action, task, payloadMap);

        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        String responseBody = responseEntity.getBody();
        List<ValueMap> rows = convertXmlToList(responseBody);

        // sort, limit
        rows.sort((o1, o2) -> {
            BigDecimal o1MarketCap = new BigDecimal(StringUtils.defaultIfBlank(o1.getString("MARTP_TOTAMT"),"0"));
            BigDecimal o2MarketCap = new BigDecimal(StringUtils.defaultIfBlank(o2.getString("MARTP_TOTAMT "),"0"));
            return o2MarketCap.compareTo(o1MarketCap);
        });
        if(rows.size() > limit) {
            rows.subList(limit, rows.size()).clear();
        }

        List<AssetEntity> assetEntities = rows.stream()
                .map(row -> {
                    String symbol = row.getString("SHOTN_ISIN");
                    AssetEntity assetEntity = AssetEntity.builder()
                            .symbol(symbol)
                            .name(row.getString("KOR_SECN_NM"))
                            .type(AssetType.STOCK)
                            .collectedAt(collectedAt)
                            .build();
                    return assetEntity;
                })
                .collect(Collectors.toList());
        assetRepository.saveAllAndFlush(assetEntities);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void collectEtfAssets(LocalDateTime collectedAt) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .readTimeout(30_000)
                .build();

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
        String payloadXml = createPayloadXml(action, task, payloadMap);

        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        String responseBody = responseEntity.getBody();
        List<ValueMap> rows = convertXmlToList(responseBody);

        // sort, limit
        rows.sort((o1, o2) -> {
            BigDecimal o1MarketCap = new BigDecimal(StringUtils.defaultIfBlank(o1.getString("NETASST_TOTAMT"),"0"));
            BigDecimal o2MarketCap = new BigDecimal(StringUtils.defaultIfBlank(o2.getString("NETASST_TOTAMT"),"0"));
            return o2MarketCap.compareTo(o1MarketCap);
        });
        if(rows.size() > limit) {
            rows.subList(limit, rows.size()).clear();
        }

        List<AssetEntity> assetEntities = rows.stream()
                .map(row -> {
                    String symbol = row.getString("SHOTN_ISIN");
                    return AssetEntity.builder()
                            .symbol(symbol)
                            .name(row.getString("KOR_SECN_NM"))
                            .type(AssetType.ETF)
                            .collectedAt(collectedAt)
                            .build();
                })
                .collect(Collectors.toList());
        assetRepository.saveAllAndFlush(assetEntities);
    }

    public static HttpHeaders createSeibroHeaders(String w2xPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/xml");
        headers.add("Origin","https://seibro.or.kr");
        headers.add("Referer","https://seibro.or.kr/websquare/control.jsp?w2xPath=" + w2xPath);
        return headers;
    }

    public static String createPayloadXml(String action, String task, Map<String,String> payloadMap) {

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

    public static List<ValueMap> convertXmlToList(String responseXml) {
        List<ValueMap> list = new ArrayList<>();
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
                ValueMap map = new ValueMap();
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

    public static ValueMap convertXmlToMap(String responseXml) {
        ValueMap map  = new ValueMap();
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

    public static String getIsin(String symbol) {
        ValueMap map = getSecInfo(symbol);
        return Optional.ofNullable(map.getString("ISIN"))
                .orElseThrow();
    }

    public static String getIssucoCustNo(String symbol) {
        ValueMap map = getSecInfo(symbol);
        return Optional.ofNullable(map.getString("ISSUCO_CUSTNO"))
                .orElseThrow();
    }

    private static ValueMap getSecInfo(String symbol) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .readTimeout(30_000)
                .build();

        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";

        String w2xPath = "/IPORTAL/user/stock/BIP_CNTS02006V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);

        String action = "secnInfoDefault";
        String task = "ksd.safe.bip.cnts.Stock.process.SecnInfoPTask";
        Map<String,String> payloadMap = new LinkedHashMap<>(){{
            put("W2XPATH", w2xPath);
            put("SHOTN_ISIN", symbol);
        }};
        String payloadXml = createPayloadXml(action, task, payloadMap);

        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        String responseBody = responseEntity.getBody();
        return convertXmlToMap(responseBody);
    }


}
