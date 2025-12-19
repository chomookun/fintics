package org.chomookun.fintics.core.basket.rebalance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.arch4j.core.common.test.CoreTestUtil;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.asset.AssetService;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.ohlcv.OhlcvService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class GroovyBasketScriptRunnerTest extends CoreTestSupport {

    private final AssetService assetService;

    private final OhlcvService ohlcvService;

    @Test
    void run() {
        // given
        Basket basket = Basket.builder()
                .market("KR")
                .script(CoreTestUtil.readTestResourceAsString(this.getClass().getPackage(), "GroovyBasketScriptRunnerTest.groovy"))
                .build();
        // when
        GroovyBasketScriptRunner groovyBasketScriptRunner = GroovyBasketScriptRunner.builder()
                .basket(basket)
                .assetService(assetService)
                .ohlcvService(ohlcvService)
                .build();
        List<BasketRebalanceAsset> basketRebalanceAssets = groovyBasketScriptRunner.run();
        // then
        log.info("basketRebalanceAssets: {}", basketRebalanceAssets);
    }

    @Disabled
    @Test
    void runActiveBasketRebalanceUs() {
        // given
        String script = CoreTestUtil.readFileAsString("src/main/scripts/basket/ActiveBasketRebalance.groovy");
        String variables = """
                market=US
                totalHoldingWeight=70
                holdingCount=40
                maxHoldingWeight=3.0
                minHoldingWeight=0.5
                stepHoldingWeight=0.07
                favoriteEnabled=true
                favoriteCount=1000
                """;
        Basket basket = Basket.builder()
                .market("KR")
                .script(script)
                .variables(variables)
                .build();
        // when
        GroovyBasketScriptRunner groovyBasketScriptRunner = GroovyBasketScriptRunner.builder()
                .basket(basket)
                .assetService(assetService)
                .ohlcvService(ohlcvService)
                .build();
        List<BasketRebalanceAsset> basketRebalanceAssets = groovyBasketScriptRunner.run();
        // then
        log.info("basketRebalanceAssets: {}", basketRebalanceAssets);
    }

    @Disabled
    @Test
    void runActiveBasketRebalanceKr() {
        // given
        String script = CoreTestUtil.readFileAsString("src/main/scripts/basket/ActiveBasketRebalance.groovy");
        String variables = """
                market=KR
                totalHoldingWeight=70
                holdingCount=40
                maxHoldingWeight=3.0
                minHoldingWeight=0.5
                stepHoldingWeight=0.07
                favoriteEnabled=true
                favoriteCount=1000
                """;
        Basket basket = Basket.builder()
                .market("KR")
                .script(script)
                .variables(variables)
                .build();
        // when
        GroovyBasketScriptRunner groovyBasketScriptRunner = GroovyBasketScriptRunner.builder()
                .basket(basket)
                .assetService(assetService)
                .ohlcvService(ohlcvService)
                .build();
        List<BasketRebalanceAsset> basketRebalanceAssets = groovyBasketScriptRunner.run();
        // then
        log.info("basketRebalanceAssets: {}", basketRebalanceAssets);
    }

}
