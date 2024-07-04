package org.oopscraft.fintics.indicator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ChaikinOscillatorCalculatorTest extends AbstractCalculatorTest {

    @Test
    void test() {
        // given
        List<Map<String,String>> inputRows = readTsv(
                "org/oopscraft/fintics/indicator/ChaikinOscillatorCalculatorTest.tsv",
                new String[]{"dateTime","open","high","low","close","volume","CO","Signal"}
        );
        List<Ohlcv> ohlcvs = inputRows.stream()
                .map(row -> {
                    return Ohlcv.builder()
                            .open(new BigDecimal(row.get("open").replaceAll(",","")))
                            .high(new BigDecimal(row.get("high").replaceAll(",", "")))
                            .low(new BigDecimal(row.get("low").replaceAll(",","")))
                            .close(new BigDecimal(row.get("close").replaceAll(",","")))
                            .volume(new BigDecimal(row.get("volume").replaceAll(",","")))
                            .build();
                })
                .collect(Collectors.toList());
        Collections.reverse(inputRows);
        Collections.reverse(ohlcvs);

        // when
        List<ChaikinOscillator> chaikinOscillators = new ChaikinOscillatorCalculator(ChaikinOscillatorContext.DEFAULT)
                .calculate(ohlcvs);

        // then
        for(int i = 0, size = chaikinOscillators.size(); i < size; i ++) {
            ChaikinOscillator chaikinOscillator = chaikinOscillators.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            Map<String,String> inputRow = inputRows.get(i);
            BigDecimal originOpenPrice = new BigDecimal(inputRow.get("open").replaceAll(",",""));
            BigDecimal originHighPrice = new BigDecimal(inputRow.get("high").replaceAll(",",""));
            BigDecimal originLowPrice = new BigDecimal(inputRow.get("low").replaceAll(",",""));
            BigDecimal originClosePrice = new BigDecimal(inputRow.get("close").replaceAll(",",""));
            BigDecimal originVolume = new BigDecimal(inputRow.get("volume").replaceAll(",",""));
            BigDecimal originCo = new BigDecimal(inputRow.get("CO").replaceAll(",",""));
            BigDecimal originSignal = new BigDecimal(inputRow.get("Signal").replaceAll(",",""));

            log.info("[{}] {},{},{},{},{},{},{} / {},{},{},{},{},{},{}", i,
                    originOpenPrice, originHighPrice, originLowPrice, originClosePrice, originVolume, originCo, originSignal,
                    ohlcv.getOpen(), ohlcv.getHigh(), ohlcv.getLow(), ohlcv.getClose(), ohlcv.getVolume(), chaikinOscillator.getValue(), chaikinOscillator.getSignal());

            // assert
            assertEquals(originCo.doubleValue(), chaikinOscillator.getValue().doubleValue(), 0.1);
            assertEquals(originSignal.doubleValue(), chaikinOscillator.getSignal().doubleValue(), 0.1);
        }

    }

}
