package org.oopscraft.fintics.calculator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.Ohlcv;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class MacdCalculatorTest extends AbstractCalculatorTest {

    @Test
    void test() throws Throwable {
        // given
        List<Map<String,String>> rows = readTsv(
                "org/oopscraft/fintics/calculator/MacdCalculatorTest.tsv",
                new String[]{"time","open","high","low","close","5","10","20","60","120","MACD","Signal","MACD-Oscillator"}
        );
        List<Ohlcv> ohlcvs = convertOhlcvs(rows, "time:MM/dd,HH:mm", "open", "high", "low", "close", null);
        Collections.reverse(rows);
        Collections.reverse(ohlcvs);

        // when
        List<Macd> macds = new MacdCalculator(MacdContext.DEFAULT).calculate(ohlcvs);

        // then
        for(int i = 0; i < ohlcvs.size(); i ++) {
            Map<String,String> row = rows.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            Macd macd = macds.get(i);
            log.debug("[{}] {}/{}/{}, {}/{}/{}", i,
                    row.get("MACD"), row.get("Signal"), row.get("MACD-Oscillator"),
                    macd.getValue(), macd.getSignal(), macd.getOscillator());

            // 초반 데이터는 데이터 부족으로 불일치함.
            if(i < (26*3) + 1) {
                continue;
            }
            // 이후 부터는 값이 일치해야함.
            assertEquals(new BigDecimal(row.get("MACD")).doubleValue(), macd.getValue().doubleValue(), 0.02);
            assertEquals(new BigDecimal(row.get("Signal")).doubleValue(), macd.getSignal().doubleValue(), 0.02);
            assertEquals(new BigDecimal(row.get("MACD-Oscillator")).doubleValue(), macd.getOscillator().doubleValue(), 0.02);
        }
    }

}