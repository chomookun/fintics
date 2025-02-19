package org.chomookun.fintics.web.ws.v1.trade.dto;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.trade.model.TradeLog;

@Builder
@Getter
public class TradeLogMessage {

    private String tradeId;

    private String logMessage;

    public static TradeLogMessage from(TradeLog tradeLog) {
        return TradeLogMessage.builder()
            .tradeId(tradeLog.getTradeId())
            .logMessage(tradeLog.getLogMessage())
            .build();
    }

}
