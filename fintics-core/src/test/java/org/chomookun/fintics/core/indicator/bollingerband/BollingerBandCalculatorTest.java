package org.chomookun.fintics.core.indicator.bollingerband;

import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.indicator.AbstractCalculatorTest;
import org.chomookun.fintics.core.indicator.bolangerband.BollingerBand;
import org.chomookun.fintics.core.indicator.bolangerband.BollingerBandCalculator;
import org.chomookun.fintics.core.indicator.bolangerband.BollingerBandContext;
import org.junit.jupiter.api.Test;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class BollingerBandCalculatorTest extends AbstractCalculatorTest {

    @Test
    void calculate() {
        // given
        String filePath = getPackageTestResourcesDir(this.getClass()) + "BollingerBandCalculatorTest.tsv";
        List<Map<String,String>> inputRows = readTsv(
                filePath,
                new String[]{"dateTime","open","high","low","close","ubb","mbb","lbb","bandWidth","percentB"}
        );
        List<Ohlcv> ohlcvs = inputRows.stream()
                .map(row -> {
                    return Ohlcv.builder()
                            .open(new BigDecimal(row.get("open").replaceAll(",","")))
                            .high(new BigDecimal(row.get("high").replaceAll(",", "")))
                            .low(new BigDecimal(row.get("low").replaceAll(",","")))
                            .close(new BigDecimal(row.get("close").replaceAll(",","")))
                            .build();
                })
                .collect(Collectors.toList());
        Collections.reverse(inputRows);
        Collections.reverse(ohlcvs);

        // when
        List<BollingerBand> bollingerBands = new BollingerBandCalculator(BollingerBandContext.DEFAULT)
                .calculate(ohlcvs);

        // then
        for(int i = 0, size = bollingerBands.size(); i < size; i ++) {
            BollingerBand bollingerBand = bollingerBands.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            Map<String,String> inputRow = inputRows.get(i);
            BigDecimal originOpenPrice = new BigDecimal(inputRow.get("open").replaceAll(",",""));
            BigDecimal originHighPrice = new BigDecimal(inputRow.get("high").replaceAll(",",""));
            BigDecimal originLowPrice = new BigDecimal(inputRow.get("low").replaceAll(",",""));
            BigDecimal originClosePrice = new BigDecimal(inputRow.get("close").replaceAll(",",""));
            BigDecimal originUbb = new BigDecimal(inputRow.get("ubb").replaceAll(",",""));
            BigDecimal originMbb = new BigDecimal(inputRow.get("mbb").replaceAll(",",""));
            BigDecimal originLbb = new BigDecimal(inputRow.get("lbb").replaceAll(",",""));
            BigDecimal originBandWidth = new BigDecimal(inputRow.get("bandWidth").replaceAll(",",""));
            BigDecimal originPercentB = new BigDecimal(inputRow.get("percentB").replaceAll("[,|%]",""));

            log.info("[{}] {},{},{}({},{}) / {},{},{}({},{})", i,
                    originUbb, originMbb, originLbb, originBandWidth, originPercentB,
                    bollingerBand.getUpper().setScale(2, RoundingMode.HALF_UP),
                    bollingerBand.getMiddle().setScale(2, RoundingMode.HALF_UP),
                    bollingerBand.getLower().setScale(2, RoundingMode.HALF_UP),
                    bollingerBand.getWidth(),
                    bollingerBand.getPercentB());

            // skip initial block
            if(i < (26*3) + 1) {
                continue;
            }

            // assert
            assertEquals(originUbb.doubleValue(), bollingerBand.getUpper().doubleValue(), 0.1);
            assertEquals(originMbb.doubleValue(), bollingerBand.getMiddle().doubleValue(), 0.1);
            assertEquals(originLbb.doubleValue(), bollingerBand.getLower().doubleValue(), 0.1);
            assertEquals(originBandWidth.doubleValue(), bollingerBand.getWidth().doubleValue(), 0.01);
            assertEquals(originPercentB.doubleValue(), bollingerBand.getPercentB().doubleValue(), 1);
        }

    }

}
