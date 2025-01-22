package org.chomoo.fintics.indicator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.chomoo.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ObvCalculatorTest extends AbstractCalculatorTest {

    @Test
    void test() {
        // given
        List<Map<String,String>> inputRows = readTsv(
                "org/chomoo/fintics/indicator/ObvCalculatorTest.tsv",
                new String[]{"dateTime","open","high","low","close","volume","OBV","Signal"}
        );
        List<Ohlcv> ohlcvs = inputRows.stream()
                .map(row -> {
                    return Ohlcv.builder()
                            .close(new BigDecimal(row.get("close").replaceAll(",","")))
                            .volume(new BigDecimal(row.get("volume").replaceAll(",","")))
                            .build();
                })
                .collect(Collectors.toList());
        Collections.reverse(inputRows);
        Collections.reverse(ohlcvs);

        // when
        List<Obv> obvs = new ObvCalculator(ObvContext.DEFAULT).calculate(ohlcvs);

        // then
        for(int i = 0, size = obvs.size(); i < size; i ++) {
            Obv obv = obvs.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            Map<String,String> inputRow = inputRows.get(i);
            BigDecimal originClosePrice = new BigDecimal(inputRow.get("close").replaceAll(",",""));
            BigDecimal originVolume = new BigDecimal(inputRow.get("volume").replaceAll(",",""));
            BigDecimal originObv = new BigDecimal(inputRow.get("OBV").replaceAll(",",""));
            BigDecimal originSignal = new BigDecimal(inputRow.get("Signal").replaceAll(",", ""));

            log.info("[{}] {},{},{}({}) / {},{},{}({})", i,
                    originClosePrice, originVolume, originObv, originSignal,
                    ohlcv.getClose(), ohlcv.getVolume(), obv.getValue(), obv.getSignal());
            assertEquals(originObv.doubleValue(), obv.getValue().doubleValue(), 1.0);
            assertEquals(originSignal.doubleValue(), obv.getSignal().doubleValue(), 1.0);
        }

    }

}
