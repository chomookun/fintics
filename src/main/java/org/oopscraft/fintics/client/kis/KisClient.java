package org.oopscraft.fintics.client.kis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.arch4j.core.support.ValueMap;
import org.oopscraft.fintics.client.Client;
import org.oopscraft.fintics.model.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class KisClient extends Client {

    private final boolean production;

    private final String apiUrl;

    private final String appKey;

    private final String appSecret;

    private final String accountNo;

    private final ObjectMapper objectMapper;

    public KisClient(Properties properties) {
        super(properties);
        this.production = Boolean.parseBoolean(properties.getProperty("production"));
        this.apiUrl = properties.getProperty("apiUrl");
        this.appKey = properties.getProperty("appKey");
        this.appSecret = properties.getProperty("appSecret");
        this.accountNo = properties.getProperty("accountNo");
        this.objectMapper = new ObjectMapper();
    }

    String getAccessKey() {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .build();
        ValueMap payloadMap = new ValueMap(){{
            put("grant_type","client_credentials");
            put("appkey", appKey);
            put("appsecret", appSecret);
        }};
        RequestEntity<Map<String,Object>> requestEntity = RequestEntity
                .post(apiUrl + "/oauth2/tokenP")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = responseEntity.getBody();
        return responseMap.getString("access_token");
    }

    HttpHeaders createHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        httpHeaders.add("authorization", "Bearer " + getAccessKey());
        httpHeaders.add("appkey", appKey);
        httpHeaders.add("appsecret", appSecret);
        return httpHeaders;
    }

    @Override
    public AssetIndicator getAssetIndicator(String symbol, AssetType type) {
        AssetIndicator assetIndicator = AssetIndicator.builder()
                .symbol(symbol)
                .type(type)
                .build();

        List<AssetTransaction> dailyAssetTransactions = getDailyAssetTransactions(symbol, type);
        assetIndicator.setDailyAssetTransactions(dailyAssetTransactions);

        List<AssetTransaction> hourlyAssetTransactions = getHourlyAssetTransactions(symbol, type);
        assetIndicator.setHourlyAssetTransactions(hourlyAssetTransactions);

        List<AssetTransaction> minuteAssetTransactions = getMinuteAssetTransactions(symbol, type);
        assetIndicator.setMinuteAssetTransaction(minuteAssetTransactions);

        return assetIndicator;
    }

    private List<AssetTransaction> getDailyAssetTransactions(String symbol, AssetType type) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-daily-price";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "FHKST01010400");
        String fidCondMrktDivCode;
        String fidInputIscd;
        switch(type) {
            case STOCK:
                fidCondMrktDivCode = "J";
                fidInputIscd = symbol;
                break;
            case ETF:
                fidCondMrktDivCode = "ETF";
                fidInputIscd = symbol;
                break;
            case ETN:
                fidCondMrktDivCode = "ETN";
                fidInputIscd = "Q" + symbol;
                break;
            default:
                throw new RuntimeException("invalid asset type - " + type);
        }
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("FID_COND_MRKT_DIV_CODE", fidCondMrktDivCode)
                .queryParam("FID_INPUT_ISCD", fidInputIscd)
                .queryParam("FID_PERIOD_DIV_CODE", "D") // 일봉
                .queryParam("FID_ROG_ADJ_PRC", "0")     // 수정주가
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

        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        List<ValueMap> output = objectMapper.convertValue(rootNode.path("output"), new TypeReference<>(){});

        return output.stream()
                .map(row -> {
                    LocalDateTime dateTime = LocalDateTime.parse(row.getString("stck_bsop_date"), DateTimeFormatter.BASIC_ISO_DATE);
                    return AssetTransaction.builder()
                            .dateTime(dateTime)
                            .price(row.getNumber("stck_clpr"))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AssetTransaction> getHourlyAssetTransactions(String symbol, AssetType type) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations/inquire-time-itemconclusion";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "FHPST01060000");
        String fidCondMrktDivCode;
        String fidInputIscd;
        switch(type) {
            case STOCK:
                fidCondMrktDivCode = "J";
                fidInputIscd = symbol;
                break;
            case ETF:
                fidCondMrktDivCode = "ETF";
                fidInputIscd = symbol;
                break;
            case ETN:
                fidCondMrktDivCode = "ETN";
                fidInputIscd = "Q" + symbol;
                break;
            default:
                throw new RuntimeException("invalid asset type - " + type);
        }
        String fidInputHour1 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("FID_COND_MRKT_DIV_CODE", fidCondMrktDivCode)
                .queryParam("FID_INPUT_ISCD", fidInputIscd)
                .queryParam("FID_INPUT_HOUR_1", fidInputHour1)
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

        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        List<ValueMap> output2 = objectMapper.convertValue(rootNode.path("output2"), new TypeReference<>(){});

        return output2.stream()
                .map(row -> {
                    LocalDateTime dateTime = LocalDateTime.parse(row.getString("stck_cntg_hour"), DateTimeFormatter.ofPattern("HHmmss"));
                    return AssetTransaction.builder()
                            .dateTime(dateTime)
                            .price(row.getNumber("stck_prpr"))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AssetTransaction> getMinuteAssetTransactions(String symbol, AssetType type) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/quotations//inquire-time-itemchartprice";
        HttpHeaders headers = createHeaders();
        headers.add("tr_id", "FHKST03010200");
        String fidEtcClsCode = "";
        String fidCondMrktDivCode;
        String fidInputIscd;
        switch(type) {
            case STOCK:
                fidCondMrktDivCode = "J";
                fidInputIscd = symbol;
                break;
            case ETF:
                fidCondMrktDivCode = "ETF";
                fidInputIscd = symbol;
                break;
            case ETN:
                fidCondMrktDivCode = "ETN";
                fidInputIscd = "Q" + symbol;
                break;
            default:
                throw new RuntimeException("invalid asset type - " + type);
        }
        String fidInputHour1 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("FID_ETC_CLS_CODE", fidEtcClsCode)
                .queryParam("FID_COND_MRKT_DIV_CODE", fidCondMrktDivCode)
                .queryParam("FID_INPUT_ISCD", fidInputIscd)
                .queryParam("FID_INPUT_HOUR_1", fidInputHour1)
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

        String rtCd = objectMapper.convertValue(rootNode.path("rt_cd"), String.class);
        String msg1 = objectMapper.convertValue(rootNode.path("msg1"), String.class);
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg1);
        }

        List<ValueMap> output2 = objectMapper.convertValue(rootNode.path("output2"), new TypeReference<>(){});

        return output2.stream()
                .map(row -> {
                    LocalDateTime dateTime = LocalDateTime.parse(
                            row.getString("stck_bsop_date") + row.getString("stck_cntg_hour"),
                            DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    );
                    BigDecimal price = row.getNumber("stck_prpr");
                    return AssetTransaction.builder()
                            .dateTime(dateTime)
                            .price(price)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Balance getBalance() {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/inquire-balance";
        HttpHeaders headers = createHeaders();
        String trId = production ? "TTTC8424R" : "VTTC8434R";
        headers.add("tr_id", trId);
        url = UriComponentsBuilder.fromUriString(url)
                .queryParam("CANO", accountNo.split("-")[0])
                .queryParam("ACNT_PRDT_CD", accountNo.split("-")[1])
                .queryParam("AFHR_FLPR_YN", "N")
                .queryParam("OFL_YN", "")
                .queryParam("INQR_DVSN", "02")
                .queryParam("UNPR_DVSN", "01")
                .queryParam("FUND_STTL_ICLD_YN", "N")
                .queryParam("FNCG_AMT_AUTO_RDPT_YN", "N")
                .queryParam("PRCS_DVSN", "00")
                .queryParam("CTX_AREA_FK100", "")
                .queryParam("CTX_AREA_NK100", "")
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
        JsonNode output1Node = rootNode.path("output1");
        List<ValueMap> output1 = objectMapper.convertValue(output1Node, new TypeReference<>(){});

        JsonNode output2Node = rootNode.path("output2");
        List<ValueMap> output2 = objectMapper.convertValue(output2Node, new TypeReference<List<ValueMap>>(){});

        List<BalanceAsset> assets = output1.stream()
                .map(row -> {
                    return BalanceAsset.builder()
                            .accountNo(accountNo)
                            .symbol(row.getString("pdno"))
                            .name(row.getString("prdt_name"))
                            .quantity(row.getNumber("hldg_qty"))
                            .build();
                })
                .collect(Collectors.toList());

        return Balance.builder()
                .accountNo(accountNo)
                .total(output2.get(0).getNumber("tot_evlu_amt"))
                .cash(output2.get(0).getNumber("dnca_tot_amt"))
                .balanceAssets(assets)
                .build();
    }

    @Override
    public void buyAsset(TradeAsset tradeAsset, BigDecimal price, int quantity) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/order-cash";
        HttpHeaders headers = createHeaders();
        String trId = production ? "TTTC0802U" : "VTTC9802U";
        headers.add("tr_id", trId);
        ValueMap payloadMap = new ValueMap(){{
            put("CANO", accountNo.split("-")[0]);
            put("ACNT_PRDT_CD", accountNo.split("-")[1]);
            put("PDNO", tradeAsset.getSymbol());
            put("ORD_DVSN", "00");
            put("ORD_QTY", quantity);
            put("ORD_UNPR", price);
        }};
        RequestEntity<ValueMap> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();
        String rtCd = responseMap.getString("rt_cd");
        String msg = responseMap.getString("msg");
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg);
        }
    }

    @Override
    public void sellAsset(BalanceAsset balanceAsset, BigDecimal price, int quantity) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .build();
        String url = apiUrl + "/uapi/domestic-stock/v1/trading/order-cash";
        HttpHeaders headers = createHeaders();
        String trId = production ? "TTTC0801U" : "VTTC0801U";
        headers.add("tr_id", trId);
        ValueMap payloadMap = new ValueMap(){{
            put("CANO", accountNo.split("-")[0]);
            put("ACNT_PRDT_CD", accountNo.split("-")[1]);
            put("PDNO", balanceAsset.getSymbol());
            put("ORD_DVSN", "00");
            put("ORD_QTY", quantity);
            put("ORD_UNPR", price);
        }};
        RequestEntity<ValueMap> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = Optional.ofNullable(responseEntity.getBody())
                .orElseThrow();
        String rtCd = responseMap.getString("rt_cd");
        String msg = responseMap.getString("msg");
        if(!"0".equals(rtCd)) {
            throw new RuntimeException(msg);
        }
    }

}
