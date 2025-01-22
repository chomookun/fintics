package org.chomookun.fintics.strategy;

import com.github.javaparser.utils.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.FinticsConfiguration;
import org.chomookun.fintics.model.BasketAsset;
import org.chomookun.fintics.model.Ohlcv;
import org.chomookun.fintics.model.Strategy;
import org.chomookun.fintics.model.TradeAsset;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class PythonStrategyRunnerTest extends CoreTestSupport {

    String loadPythonFileAsString(String fileName) {
        String filePath = null;
        try {
            filePath = new File(".").getCanonicalPath() + "/src/test/resources/org/chomookun/fintics/strategy/" + fileName;
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

    @Test
    void run() {
        // given
        String script = loadPythonFileAsString("PythonStrategyRunnerTest.py");
        Strategy strategy = Strategy.builder()
                .script(script)
                .build();
        LocalDateTime dateTime = LocalDateTime.now();
        BasketAsset basketAsset = BasketAsset.builder()
                .assetId("test")
                .name("test name")
                .build();
        TradeAsset tradeAsset = TradeAsset.builder()
                .tradeId("test")
                .minuteOhlcvs(List.of(
                        Ohlcv.of(basketAsset.getAssetId(), Ohlcv.Type.MINUTE, LocalDateTime.now(), null, 100, 100, 100, 100, 100, false)
                ))
                .build();
        // when
        PythonStrategyRunner pythonStrategyRunner = new PythonStrategyRunner(
                strategy,
                null,
                dateTime,
                basketAsset,
                tradeAsset,
                null,
                null
        );
        StrategyResult strategyResult = pythonStrategyRunner.run();
        // then
        log.info("strategyResult: {}", strategyResult);
    }

}