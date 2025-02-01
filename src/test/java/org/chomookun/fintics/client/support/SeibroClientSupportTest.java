package org.chomookun.fintics.client.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.chomookun.arch4j.core.common.support.RestTemplateBuilder;
import org.chomookun.fintics.FinticsConfiguration;
import org.chomookun.fintics.model.Asset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class SeibroClientSupportTest {

    RestTemplate restTemplate = RestTemplateBuilder.create()
            .httpRequestRetryStrategy(new DefaultHttpRequestRetryStrategy())
            .build();

    SeibroClientSupport getSeibroClientSupport() {
        return new SeibroClientSupport() {};
    }

    static List<Asset> getStockAssets() {
        return List.of(
                Asset.builder()
                        .assetId("KR.005930")
                        .name("Samsung Electronics")
                        .market("KR")
                        .type("STOCK")
                        .marketCap(BigDecimal.TEN)
                        .build()
        );
    }

    static List<Asset> getEtfAssets() {
        return List.of(
                Asset.builder()
                        .assetId("KR.069500")
                        .name("KODEX 200")
                        .market("KR")
                        .type("ETF")
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource({"getStockAssets", "getEtfAssets"})
    void getSeibroSecInfo(Asset asset) {
        // when
        Map<String, String> secInfo = getSeibroClientSupport().getSeibroSecInfo(asset, restTemplate);
        // then
        log.info("secInfo:{}", secInfo);
        assertNotNull(secInfo.get("ISSUCO_CUSTNO"));
    }

}