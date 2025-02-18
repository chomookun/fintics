package org.chomookun.fintics.core.basket.rebalance;

import com.github.javaparser.utils.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.ohlcv.OhlcvService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.asset.AssetService;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class PythonBasketScriptRunnerTest extends CoreTestSupport {

    private final AssetService assetService;

    private final OhlcvService ohlcvService;

    String loadPythonFileAsString(String fileName) {
        String filePath = null;
        try {
            filePath = new File(".").getCanonicalPath() + "/src/test/resources/org/oopscraft/fintics/basket/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = new FileInputStream(new File(filePath))) {
            IOUtils.readLines(inputStream, StandardCharsets.UTF_8).forEach(line -> {
                stringBuilder.append(line).append(LineSeparator.LF);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }

    @Disabled
    @Test
    void run() {
        // given
        Basket basket = Basket.builder()
                .market("KR")
                .script(loadPythonFileAsString("PythonBasketScriptRunnerTest.py"))
                .build();
        // when
        PythonBasketScriptRunner pythonBasketScriptRunner = PythonBasketScriptRunner.builder()
                .basket(basket)
                .assetService(assetService)
                .ohlcvService(ohlcvService)
                .build();
        List<BasketRebalanceAsset> basketRebalanceResults = pythonBasketScriptRunner.run();
        // then
        log.info("basketRebalanceResults: {}", basketRebalanceResults);
    }

}