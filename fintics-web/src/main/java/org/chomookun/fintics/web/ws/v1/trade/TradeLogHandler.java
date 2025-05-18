package org.chomookun.fintics.web.ws.v1.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.trade.TradeChannels;
import org.chomookun.fintics.core.trade.model.TradeLog;
import org.chomookun.fintics.web.ws.v1.trade.dto.TradeLogMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Lazy(false)
@RequiredArgsConstructor
@Slf4j
public class TradeLogHandler implements MessageListener {

    private final RedisMessageListenerContainer container;

    private final ObjectMapper objectMapper;

    private final SimpMessagingTemplate simpMessagingTemplate;

    @PostConstruct
    void initialize() {
        container.addMessageListener(this, TradeChannels.TRADE_LOG_CHANNEL);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        TradeLog tradeLog;
        try {
            tradeLog = objectMapper.readValue(message.getBody(), TradeLog.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String destination = String.format("/trades/%s/log", tradeLog.getTradeId());
        TradeLogMessage tradeLogResponse = TradeLogMessage.from(tradeLog);
        simpMessagingTemplate.convertAndSend(destination, tradeLogResponse);
    }

}
