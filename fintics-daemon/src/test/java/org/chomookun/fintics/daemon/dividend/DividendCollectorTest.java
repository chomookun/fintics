package org.chomookun.fintics.daemon.dividend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.daemon.FinticsDaemonConfiguration;
import org.chomookun.fintics.core.dividend.entity.DividendEntity;
import org.chomookun.fintics.core.asset.model.Asset;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest(classes = FinticsDaemonConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class DividendCollectorTest extends CoreTestSupport {

    private final DividendCollector dividendCollector;

    static List<Asset> getUsStockAssets() {
        return List.of(
                Asset.builder()
                        .assetId("US.MSFT")
                        .name("Microsoft Corporation Common Stock")
                        .market("US")
                        .type("STOCK")
                        .build()
        );
    }

    static List<Asset> getUsEtfAssets() {
        return List.of(
                Asset.builder()
                        .assetId("US.SPY")
                        .market("US")
                        .type("ETF")
                        .build()
        );
    }

    static List<Asset> getKrStockAssets() {
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

    static List<Asset> getKrEtfAssets() {
        return List.of(
                Asset.builder()
                        .assetId("KR.069500")
                        .name("KODEX 200")
                        .market("KR")
                        .type("ETF")
                        .build()
        );
    }

    @Tag("manual")
    @Test
    void collect() {
        // given
        // when
        dividendCollector.collect();
        // then
        List<DividendEntity> dividendEntities = entityManager.createQuery("select a from DividendEntity a", DividendEntity.class)
                .getResultList();
        log.info("dividendEntities:{}", dividendEntities);
    }

    @Tag("manual")
    @ParameterizedTest
    @MethodSource({"getUsStockAssets", "getUsEtfAssets", "getKrStockAssets", "getKrEtfAssets"})
    void saveDividends(Asset asset) {
        // given
        // when
        dividendCollector.saveDividends(asset);
        // then
        List<DividendEntity> dividendEntities = entityManager.createQuery("select a from DividendEntity a where a.assetId = :assetId", DividendEntity.class)
                .setParameter("assetId", asset.getAssetId())
                .getResultList();
        log.info("dividendEntities:{}", dividendEntities);
    }

}