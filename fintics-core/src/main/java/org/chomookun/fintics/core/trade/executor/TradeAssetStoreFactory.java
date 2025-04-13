package org.chomookun.fintics.core.trade.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.trade.repository.TradeAssetRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@RequiredArgsConstructor
public class TradeAssetStoreFactory {

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    private final TradeAssetRepository profileRepository;

    private final PlatformTransactionManager transactionManager;

    public TradeAssetStore getObject() {
        return TradeAssetStore.builder()
                .stringRedisTemplate(stringRedisTemplate)
                .objectMapper(objectMapper)
                .tradeAssetRepository(profileRepository)
                .transactionManager(transactionManager)
                .build();
    }

}
