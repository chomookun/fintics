package org.chomoo.fintics.indicator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.chomoo.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class CciCalculatorTest extends AbstractCalculatorTest {

    @Test
    void calculate() throws Throwable {
        // given
        String filePath = "org/chomoo/fintics/indicator/CciCalculatorTest.tsv";
        String[] columnNames = new String[]{"time","open","high","low","close","volume","cci","signal"};
        List<Map<String,String>> rows = readTsv(filePath, columnNames);
        List<Ohlcv> ohlcvs = convertOhlcvs(rows, "time^MM/dd,HH:mm","open","high","low","close",null);
        Collections.reverse(rows);
        Collections.reverse(ohlcvs);

        // when
        List<Cci> ccis = new CciCalculator(CciContext.DEFAULT).calculate(ohlcvs);

        // then
        for(int i = 0; i < rows.size(); i ++) {
            Map<String,String> row = rows.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            BigDecimal originCci = new BigDecimal(row.get("cci").replaceAll("[,]",""));
            BigDecimal originSignal = new BigDecimal(row.get("signal").replaceAll("[,]", ""));
            Cci cci = ccis.get(i);
            log.debug("[{}]{}|{}/{}|{}/{}", i, row.get("time"), originCci, originSignal, cci.getValue(), cci.getSignal());

            // assert
            if (i >= 20) {
                assertEquals(originCci.doubleValue(), cci.getValue().doubleValue(), 0.9);
                assertEquals(originCci.doubleValue(), cci.getValue().doubleValue(), 0.9);
            }

        }
    }

}