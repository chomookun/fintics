package org.chomoo.fintics.indicator;

import lombok.extern.slf4j.Slf4j;
import org.chomoo.fintics.indicator.Sma;
import org.chomoo.fintics.indicator.SmaCalculator;
import org.chomoo.fintics.indicator.SmaContext;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.chomoo.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SmaCalculatorTest {

    @Test
    @Order(1)
    void test1() {
        // given
        List<Ohlcv> ohlcvs = new ArrayList<>(){{
            add(Ohlcv.builder().close(BigDecimal.valueOf(100)).build());
            add(Ohlcv.builder().close(BigDecimal.valueOf(200)).build());
            add(Ohlcv.builder().close(BigDecimal.valueOf(300)).build());
        }};

        // when
        List<Sma> smas = new SmaCalculator(SmaContext.of(3)).calculate(ohlcvs);

        // then
        smas.forEach(sma -> log.debug("{}", sma));
    }

}
