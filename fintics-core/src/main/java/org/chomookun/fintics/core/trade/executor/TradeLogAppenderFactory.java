package org.chomookun.fintics.core.trade.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.trade.model.Trade;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import ch.qos.logback.core.Context;

@Component
@RequiredArgsConstructor
public class TradeLogAppenderFactory {

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    public TradeLogAppender getObject(Context context, Trade trade) {
        return TradeLogAppender.builder()
                .context(context)
                .trade(trade)
                .stringRedisTemplate(stringRedisTemplate)
                .objectMapper(objectMapper)
                .build();
    }

}
