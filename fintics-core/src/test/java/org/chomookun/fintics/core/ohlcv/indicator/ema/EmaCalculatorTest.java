package org.chomookun.fintics.core.ohlcv.indicator.ema;

import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.ohlcv.indicator.ema.Ema;
import org.chomookun.fintics.core.ohlcv.indicator.ema.EmaCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.ema.EmaContext;
import org.junit.jupiter.api.Test;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
class EmaCalculatorTest {

    @Test
    void calculate() {
        // given
        List<Ohlcv> series = new ArrayList<>();
        for(int i = 0; i < 500; i ++) {
            series.add(Ohlcv.builder()
                    .close(BigDecimal.valueOf(Math.random() * (12000-1000) + 1000))
                    .build());
        }
        // when
        List<Ema> emas = new EmaCalculator(EmaContext.of(10))
                .calculate(series);
        // then
        emas.forEach(ema -> log.debug("{}", ema));
    }

}