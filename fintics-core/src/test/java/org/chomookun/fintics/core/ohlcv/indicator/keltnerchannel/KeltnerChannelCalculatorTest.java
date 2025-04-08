package org.chomookun.fintics.core.ohlcv.indicator.keltnerchannel;

import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.ohlcv.indicator.AbstractCalculatorTest;
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
public class KeltnerChannelCalculatorTest extends AbstractCalculatorTest {

    @Test
    void calculate() {
        // given
        String[] columnNames = new String[]{"dateTime","open","high","low","close","volume","center","upper","lower"};
        List<Map<String,String>> inputRows = readTestResourceAsTsv(this.getClass().getPackage(), "KeltnerChannelCalculatorTest.tsv", columnNames);
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
        List<KeltnerChannel> keltnerChannels = new KeltnerChannelCalculator(KeltnerChannelContext.DEFAULT)
                .calculate(ohlcvs);
        // then
        for(int i = 0, size = keltnerChannels.size(); i < size; i ++) {
            KeltnerChannel keltnerChannel = keltnerChannels.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            Map<String,String> inputRow = inputRows.get(i);
            BigDecimal originOpen = new BigDecimal(inputRow.get("open").replaceAll(",",""));
            BigDecimal originHigh = new BigDecimal(inputRow.get("high").replaceAll(",",""));
            BigDecimal originLow = new BigDecimal(inputRow.get("low").replaceAll(",",""));
            BigDecimal originClose = new BigDecimal(inputRow.get("close").replaceAll(",",""));
            BigDecimal originCenter = new BigDecimal(inputRow.get("center").replaceAll(",",""));
            BigDecimal originUpper = new BigDecimal(inputRow.get("upper").replaceAll(",",""));
            BigDecimal originLower = new BigDecimal(inputRow.get("lower").replaceAll(",",""));
            log.info("[{}] {},{},{},{} - {},{},{} / {},{},{}",
                    i, originOpen, originHigh, originLow, originClose,
                    originCenter, originUpper, originLower,
                    keltnerChannel.getCenter().setScale(2, RoundingMode.HALF_UP),
                    keltnerChannel.getUpper().setScale(2, RoundingMode.HALF_UP),
                    keltnerChannel.getLower().setScale(2, RoundingMode.HALF_UP));
            // skip initial block
            if (i <= 20) {
                continue;
            }
            // assert (테스트 데이터 를 구할수 없음)
            // assertEquals(originCenter.doubleValue(), keltnerChannel.getUpper().doubleValue(), 0.1);
            // assertEquals(originUpper.doubleValue(), keltnerChannel.getUpper().doubleValue(), 0.1);
            // assertEquals(originLower.doubleValue(), keltnerChannel.getLower().doubleValue(), 0.1);
        }

    }

}
