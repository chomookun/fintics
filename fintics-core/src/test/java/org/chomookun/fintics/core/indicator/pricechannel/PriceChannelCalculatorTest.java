package org.chomookun.fintics.core.indicator.pricechannel;

import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.indicator.AbstractCalculatorTest;
import org.junit.jupiter.api.Test;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class PriceChannelCalculatorTest extends AbstractCalculatorTest {

    @Test
    void calculate() {
        // given
        String[] columnNames = new String[]{"dateTime","open","high","low","close","upper","lower"};
        List<Map<String,String>> inputRows = readTestResourceAsTsv(this.getClass().getPackage(), "PriceChannelCalculatorTest.tsv", columnNames);
        List<Ohlcv> ohlcvs = inputRows.stream()
                .map(row -> {
                    return Ohlcv.builder()
                            .dateTime(LocalDate.parse(row.get("dateTime"), DateTimeFormatter.ofPattern("yyyy/MM/dd")).atStartOfDay())
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
        List<PriceChannel> priceChannels = new PriceChannelCalculator(PriceChannelContext.DEFAULT)
                .calculate(ohlcvs);
        // then
        for(int i = 0, size = priceChannels.size(); i < size; i ++) {
            PriceChannel priceChannel = priceChannels.get(i);
            Ohlcv ohlcv = ohlcvs.get(i);
            Map<String,String> inputRow = inputRows.get(i);
            String originDateTime = inputRow.get("dateTime");
            BigDecimal originOpen = new BigDecimal(inputRow.get("open").replaceAll(",",""));
            BigDecimal originHigh = new BigDecimal(inputRow.get("high").replaceAll(",",""));
            BigDecimal originLow = new BigDecimal(inputRow.get("low").replaceAll(",",""));
            BigDecimal originClose = new BigDecimal(inputRow.get("close").replaceAll(",",""));
            BigDecimal originUpper = new BigDecimal(inputRow.get("upper").replaceAll(",",""));
            BigDecimal originLower = new BigDecimal(inputRow.get("lower").replaceAll(",",""));
            log.info("[{}/{}] {},{},{},{} - {},{} / {},{}",
                    originDateTime, priceChannel.getDateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                    originOpen, originHigh, originLow, originClose,
                    originUpper, originLower,
                    priceChannel.getUpper().setScale(2, RoundingMode.HALF_UP),
                    priceChannel.getLower().setScale(2, RoundingMode.HALF_UP));
            // skip initial block
            if (i <= PriceChannelContext.DEFAULT.getPeriod()) {
                continue;
            }
            // assert
            assertEquals(originUpper.doubleValue(), priceChannel.getUpper().doubleValue(), 0.1);
            assertEquals(originLower.doubleValue(), priceChannel.getLower().doubleValue(), 0.1);
        }
    }

}
