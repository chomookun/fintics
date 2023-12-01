package org.oopscraft.fintics.trade;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ToolTest {

    @Test
    void testChart() {
        // given
        List<BigDecimal> rows = new ArrayList<>();
        for(int i = 0; i < 100; i ++) {
            rows.add(BigDecimal.valueOf(100*i));
        }

        // when
        Tool tool = new Tool();
        log.info("###############{}", tool.graph("Test Graph", rows));
    }

}
