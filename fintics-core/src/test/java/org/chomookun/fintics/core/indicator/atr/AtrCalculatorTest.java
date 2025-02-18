package org.chomookun.fintics.core.indicator.atr;

import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.indicator.AbstractCalculatorTest;
import org.junit.jupiter.api.Test;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class AtrCalculatorTest extends AbstractCalculatorTest {

    @Test
    void calculate() throws Throwable {
        // given
        String filePath = getPackageTestResourcesDir(this.getClass()) + "AtrCalculatorTest.tsv";
        String[] columnNames = new String[]{"time","open","high","low","close","volume","value","signal"};
        List<Map<String,String>> rows = readTsv(filePath, columnNames);
        List<Ohlcv> ohlcvs = convertOhlcvs(rows, "time^MM/dd,HH:mm","open","high","low","close",null);
        Collections.reverse(rows);
        Collections.reverse(ohlcvs);

        // when
        List<Atr> atrs = new AtrCalculator(AtrContext.DEFAULT).calculate(ohlcvs);

        // then
        for(int i = 0; i < rows.size(); i ++) {
            Map<String,String> row = rows.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            BigDecimal originAtr = new BigDecimal(row.get("value").replaceAll("[,]",""));
            BigDecimal originSignal = new BigDecimal(row.get("signal").replaceAll("[,]", ""));
            Atr atr = atrs.get(i);
            log.info("[{}]{}|{}/{}|{}/{}", i, row.get("time"), originAtr, originSignal, atr.getValue(), atr.getSignal());

            // assert
            if (i >= 14) {
                assertEquals(originAtr.doubleValue(), atr.getValue().setScale(2, RoundingMode.HALF_UP).doubleValue(), 0.1);
                assertEquals(originSignal.doubleValue(), atr.getSignal().setScale(2, RoundingMode.HALF_UP).doubleValue(), 0.1);
            }

        }
    }

}