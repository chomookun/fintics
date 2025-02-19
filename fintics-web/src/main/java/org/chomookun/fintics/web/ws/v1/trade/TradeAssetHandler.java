package org.chomookun.fintics.web.ws.v1.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.trade.TradeChannels;
import org.chomookun.fintics.core.trade.model.TradeAsset;
import org.chomookun.fintics.web.ws.v1.trade.dto.TradeAssetMessage;
import org.chomookun.fintics.web.ws.v1.trade.dto.TradeLogMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeAssetHandler implements MessageListener {

    private final RedisMessageListenerContainer container;

    private final ObjectMapper objectMapper;

    private final SimpMessagingTemplate simpMessagingTemplate;

    @PostConstruct
    public void init() {
        container.addMessageListener(this, TradeChannels.TRADE_ASSET);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        TradeAsset tradeAsset;
        try {
            tradeAsset = objectMapper.readValue(message.getBody(), TradeAsset.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String destination = String.format("/trades/%s/assets", tradeAsset.getTradeId());
        TradeAssetMessage tradeAssetMessage = TradeAssetMessage.from(tradeAsset);
        simpMessagingTemplate.convertAndSend(destination, tradeAssetMessage);
    }

}
