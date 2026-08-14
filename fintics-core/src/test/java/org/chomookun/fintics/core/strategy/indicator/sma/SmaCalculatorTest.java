package org.chomookun.fintics.core.strategy.indicator.sma;

import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.strategy.indicator.sma.Sma;
import org.chomookun.fintics.core.strategy.indicator.sma.SmaCalculator;
import org.chomookun.fintics.core.strategy.indicator.sma.SmaContext;
import org.junit.jupiter.api.Test;
import org.chomookun.fintics.core.asset.model.Ohlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SmaCalculatorTest {

    @Test
    void calculate() {
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
