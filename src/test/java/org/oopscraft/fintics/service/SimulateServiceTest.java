package org.oopscraft.fintics.service;

import com.github.javaparser.utils.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.Simulate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class SimulateServiceTest extends CoreTestSupport {

    private final SimulateService simulateService;

    private String readFileAsString(String filePath) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            IOUtils.readLines(inputStream, StandardCharsets.UTF_8).forEach(line -> {
                stringBuilder.append(line).append(LineSeparator.LF);
            });
        }
        return stringBuilder.toString();
    }

    void simulateWithFileName(String fileName) throws Exception {
        // given
        String holdCondition = readFileAsString("org/oopscraft/fintics/service/SimulateService.holdCondition.groovy");
        Integer interval = 30;

        String filePath = "org/oopscraft/fintics/service/" + fileName;
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter("\t")
                .setHeader("time","open","high","low","close","MACD","MACD-Signal","MACD-Oscillator", "RSI", "RSI-Signal")
                .setSkipHeaderRecord(true)
                .build();
        final List<Ohlcv> ohlcvs = new ArrayList<>();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .forEach(record -> {
                        ohlcvs.add(Ohlcv.builder()
                                .dateTime(LocalDateTime.parse("2000 " + record.get("time"),DateTimeFormatter.ofPattern("yyyy MM/dd,HH:mm")))
                                .openPrice(Double.parseDouble(record.get("open").replaceAll(",","")))
                                .highPrice(Double.parseDouble(record.get("high").replaceAll(",","")))
                                .lowPrice(Double.parseDouble(record.get("low").replaceAll(",","")))
                                .closePrice(Double.parseDouble(record.get("close").replaceAll(",","")))
                                .build());
                    });
        }

        // when
        Simulate simulate = Simulate.builder()
                .holdCondition(holdCondition)
                .interval(interval)
                .startAt(LocalTime.of(9,30))
                .endAt(LocalTime.of(15,0))
                .ohlcvs(ohlcvs)
                .feeRate(0.02)
                .bidAskSpread(5.0)
                .build();
        simulate = simulateService.simulate(simulate);

        // then
        log.info("{}", simulate.getHoldConditionResults());
    }

    @Test
    void simulate20231025_KODEX레버리지() throws Exception {
        simulateWithFileName("SimulateService.ohlcv.20231025_KODEX레버리지.tsv");
    }

    @Test
    void simulate20231026_20231026_KODEX코스닥150() throws Exception {
        simulateWithFileName("SimulateService.ohlcv.20231026_KODEX코스닥150.tsv");
    }

    @Test
    void simulate20231026_KODEX코스닥150선물인버스() throws Exception {
        simulateWithFileName("SimulateService.ohlcv.20231026_KODEX코스닥150선물인버스.tsv");
    }

    @Test
    void simulate20231027_20231026_KODEX코스닥150() throws Exception {
        simulateWithFileName("SimulateService.ohlcv.20231027_KODEX코스닥150.tsv");
    }

    @Test
    void simulate20231027_KODEX코스닥150선물인버스() throws Exception {
        simulateWithFileName("SimulateService.ohlcv.20231027_KODEX코스닥150선물인버스.tsv");
    }

}