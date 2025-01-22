package org.chomookun.fintics.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.dao.TradeAssetRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@RequiredArgsConstructor
public class TradeAssetStoreFactory {

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    private final TradeAssetRepository profileRepository;

    private final PlatformTransactionManager transactionManager;

    /**
     * gets trade asset score
     * @param destination stomp message destination
     * @param persist persist or not
     * @return trade asset store
     */
    public TradeAssetStore getObject(String destination, boolean persist) {
        return TradeAssetStore.builder()
                .messagingTemplate(messagingTemplate)
                .objectMapper(objectMapper)
                .destination(destination)
                .persist(persist)
                .tradeAssetRepository(profileRepository)
                .transactionManager(transactionManager)
                .build();
    }

}
