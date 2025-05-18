package org.chomookun.fintics.core.trade.executor;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.trade.TradeChannels;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.core.trade.model.TradeLog;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
public class TradeLogAppender extends AppenderBase<ILoggingEvent> {

    private final PatternLayout layout;

    private final Trade trade;

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    @Builder
    private TradeLogAppender(Context context, Trade trade, StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        layout = new PatternLayout();
        layout.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level [%.-12thread] - %msg");
        layout.setContext(context);
        layout.start();
        this.trade = trade;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void append(ILoggingEvent event) {
        String logMessage = layout.doLayout(event);
        TradeLog tradeLog = TradeLog.builder()
                .tradeId(trade.getTradeId())
                .logMessage(logMessage)
                .build();
        String message;
        try {
            message = objectMapper.writeValueAsString(tradeLog);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        stringRedisTemplate.convertAndSend(TradeChannels.TRADE_LOG, message);
    }

}
