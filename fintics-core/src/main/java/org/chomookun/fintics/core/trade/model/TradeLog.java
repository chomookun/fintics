package org.chomookun.fintics.core.trade.model;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeLog {

    private String tradeId;

    private String logMessage;

}
