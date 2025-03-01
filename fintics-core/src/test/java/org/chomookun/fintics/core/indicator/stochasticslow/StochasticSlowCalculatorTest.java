package org.chomookun.fintics.core.indicator.stochasticslow;

import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.indicator.AbstractCalculatorTest;
import org.junit.jupiter.api.Test;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class StochasticSlowCalculatorTest extends AbstractCalculatorTest {

    @Test
    void calculate() throws Throwable {
        // given
        String[] columnNames = new String[]{"time","open","high","low","close","volume","slowK","slowD"};
        List<Map<String,String>> rows = readTestResourceAsTsv(this.getClass().getPackage(), "StochasticSlowCalculatorTest.tsv", columnNames);
        List<Ohlcv> ohlcvs = convertOhlcvs(rows, "time^MM/dd,HH:mm","open","high","low","close",null);
        Collections.reverse(rows);
        Collections.reverse(ohlcvs);
        // when
        List<StochasticSlow> stochasticSlows = new StochasticSlowCalculator(StochasticSlowContext.DEFAULT).calculate(ohlcvs);
        // then
        for(int i = 0; i < rows.size(); i ++) {
            Map<String,String> row = rows.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            BigDecimal originSlowK = new BigDecimal(row.get("slowK").replaceAll("[%]",""));
            BigDecimal originSlowD = new BigDecimal(row.get("slowD").replaceAll("[%]", ""));
            StochasticSlow stochasticSlow = stochasticSlows.get(i);
            log.debug("[{}]{}|{}/{}|{}/{}", i, row.get("time"), originSlowK, originSlowD, stochasticSlow.getSlowK(), stochasticSlow.getSlowD());
            // assert
            if (i >= 5) {
                assertEquals(originSlowK.doubleValue(), stochasticSlow.getSlowK().setScale(2, RoundingMode.HALF_UP).doubleValue(), 0.1);
                assertEquals(originSlowD.doubleValue(), stochasticSlow.getSlowD().setScale(2, RoundingMode.HALF_UP).doubleValue(), 0.1);
            }
        }
    }

}