package org.oopscraft.fintics.client.broker.kis;

import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.common.support.RestTemplateBuilder;
import org.oopscraft.arch4j.core.common.support.ValueMap;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * kis access token registry in memory
 */
@Slf4j
public class KisAccessTokenRegistry {

    private static final Set<KisAccessToken> accessTokens = Collections.synchronizedSet(new HashSet<>());

    private final static Object LOCK_OBJECT = new Object();

    /**
     * returns rest template
     * @return rest template
     */
    static RestTemplate getRestTemplate() {
        return RestTemplateBuilder.create()
                .httpRequestRetryHandler(new KisHttpRequestRetryHandler(3))
                .build();
    }

    /**
     * gets access token
     * @param apiUrl api url
     * @param appKey api key
     * @param appSecret api secret
     * @return access token
     */
    public synchronized static KisAccessToken getAccessToken(String apiUrl, String appKey, String appSecret) throws InterruptedException {
        synchronized (LOCK_OBJECT) {
            KisAccessToken accessToken = accessTokens.stream()
                    .filter(element ->
                            Objects.equals(element.getApiUrl(), apiUrl)
                                    && Objects.equals(element.getAppKey(), appKey)
                                    && Objects.equals(element.getAppSecret(), appSecret))
                    .findFirst()
                    .orElse(null);

            if (accessToken == null || accessToken.isExpired()) {
                // 한국 투자 증권 정책 상 1분에 1회 호출 가능함(호출 시 무조건 카운팅 됨)
                try {
                    accessToken = refreshAccessToken(apiUrl, appKey, appSecret);
                } catch (Throwable e) {
                    // 토큰 발급 자체도 1분당 1회발급 제약에 걸리게 됨으로
                    // 오류 발생 시에는 1분(이상) 호출 자체를 하지 않아야 함.
                    // Invalid 한 만료기간 1분인 TEMP_ERROR_TOKEN 을 발행 하고
                    // 1분 간은 인증 오류가 발생 하고
                    // 1분 후 만료 시 재호출 됨(그때 정상 이면 복구가 되어야 함, 그때도 장애 상태 이면 계속 반복)
                    log.warn("Refresh access token error: {}", e.getMessage());
                    accessToken = KisAccessToken.builder()
                            .apiUrl(apiUrl)
                            .appKey(appKey)
                            .appSecret(appSecret)
                            .accessToken("TEMP_ERROR_TOKEN")
                            .expireDateTime(LocalDateTime.now().plusSeconds(60))     // 만료 시간 1분 후로 설정(1분후 만료 됨으로 재발급 요청됨)
                            .build();
                }
                accessTokens.remove(accessToken);
                accessTokens.add(accessToken);
            }

            // return
            return accessToken;
        }
    }

    /**
     * refresh access token
     * @param apiUrl api url
     * @param appKey app key
     * @param appSecret app secret
     * @return kis access token object
     * @see [접근토큰발급(P)[인증-001]](https://apiportal.koreainvestment.com/apiservice/oauth2#L_fa778c98-f68d-451e-8fff-b1c6bfe5cd30)
     */
    private synchronized static KisAccessToken refreshAccessToken(String apiUrl, String appKey, String appSecret) throws InterruptedException {
        log.info("Refresh Access Token - {}", apiUrl);
        RestTemplate restTemplate = getRestTemplate();
        ValueMap payloadMap = new ValueMap(){{
            put("grant_type","client_credentials");
            put("appkey", appKey);
            put("appsecret", appSecret);
        }};
        RequestEntity<Map<String,Object>> requestEntity = RequestEntity
                .post(apiUrl + "/oauth2/tokenP")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadMap);
        Thread.sleep(1_000);
        ResponseEntity<ValueMap> responseEntity = restTemplate.exchange(requestEntity, ValueMap.class);
        ValueMap responseMap = responseEntity.getBody();
        String accessToken = responseMap.getString("access_token");
        LocalDateTime expiredDateTime = LocalDateTime.parse(
                responseMap.getString("access_token_token_expired"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        log.info("expiredDateTime: {}", expiredDateTime);
        return KisAccessToken.builder()
                .apiUrl(apiUrl)
                .appKey(appKey)
                .appSecret(appSecret)
                .accessToken(accessToken)
                .expireDateTime(expiredDateTime)
                .build();
    }

}
