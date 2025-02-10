package org.chomookun.fintics.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderBook {

    private BigDecimal price;

    private BigDecimal askPrice;

    private BigDecimal bidPrice;

    private BigDecimal tickPrice;

}
