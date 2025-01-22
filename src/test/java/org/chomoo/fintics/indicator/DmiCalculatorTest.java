package org.chomoo.fintics.indicator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.chomoo.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class DmiCalculatorTest extends AbstractCalculatorTest {

    void calculate(String fileName) {
        // given
        List<Map<String,String>> rows = readTsv(
                "org/chomoo/fintics/indicator/" + fileName,
                new String[]{"time","open","high","low","close","adx","pdi","mdi"});
        List<Ohlcv> ohlcvs = convertOhlcvs(rows, "time^MM/dd,HH:mm", "open", "high", "low", "close", "volume");
        Collections.reverse(rows);
        Collections.reverse(ohlcvs);

        // when
        List<Dmi> dmis = new DmiCalculator(DmiContext.DEFAULT).calculate(ohlcvs);

        // then
        for(int i = 0; i < dmis.size(); i ++) {
            Map<String,String> row  = rows.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            Dmi dmi = dmis.get(i);
            log.info("[{}][{}|{}|{}|{}] {}\t{}\t{} | {}\t{}\t{}",
                    i, row.get("time"), row.get("high"), row.get("low"), row.get("close"),
                    row.get("adx"), row.get("pdi"), row.get("mdi"),
                    dmi.getAdx(), dmi.getPdi(), dmi.getMdi()
            );
            // 초반 데이터는 데이터 부족으로 불일치함.
            if(i < 50) {
                continue;
            }
            // 이후 부터는 값이 일치해야함.
            assertEquals(new BigDecimal(row.get("adx").replaceAll("%","")).doubleValue(), dmi.getAdx().doubleValue(), 1);
            assertEquals(new BigDecimal(row.get("pdi").replaceAll("%","")).doubleValue(), dmi.getPdi().doubleValue(), 1);
            assertEquals(new BigDecimal(row.get("mdi").replaceAll("%","")).doubleValue(), dmi.getMdi().doubleValue(), 1);
        }
    }

    @Test
    @Order(2)
    void calculate01() {
        calculate("DmiCalculatorTest.01.tsv");
    }

    @Test
    @Order(3)
    void calculate02() {
        calculate("DmiCalculatorTest.02.tsv");
    }

}