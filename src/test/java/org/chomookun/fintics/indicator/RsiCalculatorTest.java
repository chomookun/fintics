package org.chomookun.fintics.indicator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.chomookun.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class RsiCalculatorTest extends AbstractCalculatorTest {

    @Test
    void calculate() throws Throwable {
        // given
        String filePath = "org/chomookun/fintics/indicator/RsiCalculatorTest.tsv";
        String[] columnNames = new String[]{"time","open","high","low","close","5","10","20","60","120","RSI","Signal"};
        List<Map<String,String>> rows = readTsv(filePath, columnNames);
        List<Ohlcv> ohlcvs = convertOhlcvs(rows, "time^MM/dd,HH:mm","open","high","low","close",null);
        Collections.reverse(rows);
        Collections.reverse(ohlcvs);

        // when
        List<Rsi> rsis = new RsiCalculator(RsiContext.DEFAULT).calculate(ohlcvs);

        // then
        for(int i = 0; i < rows.size(); i ++) {
            Map<String,String> row = rows.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            BigDecimal originRsi = new BigDecimal(row.get("RSI").replaceAll("[,%]",""));
            BigDecimal originRsiSignal = new BigDecimal(row.get("Signal").replaceAll("[,%]", ""));
            Rsi rsi = rsis.get(i);
            log.debug("[{}]{}|{}/{}|{}/{}", i, row.get("time"), originRsi, originRsiSignal, rsi.getValue(), rsi.getSignal());

            // period + 1 전의 RSI는 데이터부족으로 중립(50)으로 반환됨.
            if(i < 14 + 1) {
                assertEquals(50, rsi.getValue().doubleValue());
            }
            // 이후 부터는 값이 일치해야함.
            else{
                assertEquals(originRsi.doubleValue(), rsi.getValue().doubleValue(), 0.02);
            }

            // pried + signalPeriod + 2 부터는 signal 값도 일치 해야 함
            if(i > 14 + 9 + 2) {
                assertEquals(originRsiSignal.doubleValue(), rsi.getSignal().doubleValue(), 1.0);
            }
        }
    }

}