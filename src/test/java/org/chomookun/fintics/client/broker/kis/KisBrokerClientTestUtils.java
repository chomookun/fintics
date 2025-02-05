package org.chomookun.fintics.client.broker.kis;

import org.apache.commons.io.FileUtils;
import org.chomookun.arch4j.core.common.data.IdGenerator;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class KisBrokerClientTestUtils {

    /**
     * 인증 토큰 발급 횟수 제한이 있어서 한번 발급 후 temp file 저정 후 재 사용
     * @param apiUrl api url
     * @param appKey app key
     * @param appSecret app secret
     */
    static void loadAccessToken(String apiUrl, String appKey, String appSecret) {
        try {
            // 테스트 시 토큰을 계속 발급 할수 없음 으로 파일로 저장 후 재 사용
            KisAccessToken accessToken = null;
            Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "fintics");
            String tempFileName = IdGenerator.md5(apiUrl + appKey + appSecret);
            Path tempFile = tempDir.resolve(tempFileName);
            boolean needToRefresh = true;
            if (Files.exists(tempFile)) {
                String value = FileUtils.readFileToString(tempFile.toFile(), StandardCharsets.UTF_8);
                String[] values = value.split("\t");
                LocalDateTime expireDateTime = LocalDateTime.parse(values[0]);
                String tokenValue = values[1];
                if (LocalDateTime.now().isBefore(expireDateTime)) {
                    needToRefresh = false;
                    accessToken = KisAccessToken.builder()
                            .apiUrl(apiUrl)
                            .appKey(appKey)
                            .appSecret(appSecret)
                            .accessToken(tokenValue)
                            .expireDateTime(expireDateTime)
                            .build();
                }
            }
            if (needToRefresh) {
                accessToken = KisAccessTokenRegistry.createAccessToken(apiUrl, appKey, appSecret);
                accessToken.setExpireDateTime(LocalDateTime.now().plusMinutes(3));
                FileUtils.write(tempFile.toFile(), accessToken.getExpireDateTime() + "\t" + accessToken.getAccessToken(), StandardCharsets.UTF_8);
            }
            KisAccessTokenRegistry.saveAccessToken(accessToken);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
