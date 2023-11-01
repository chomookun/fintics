package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.implementation.bind.annotation.Super;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderBook {

    BigDecimal price;

    BigDecimal bidPrice;

    BigDecimal askPrice;

}
