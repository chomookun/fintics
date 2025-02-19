package org.chomookun.fintics.core.trade.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.trade.TradeChannels;
import org.chomookun.fintics.core.trade.entity.TradeAssetEntity;
import org.chomookun.fintics.core.trade.repository.TradeAssetRepository;
import org.chomookun.fintics.core.trade.model.TradeAsset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Builder
@Getter
public class TradeAssetStore {

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    private final TradeAssetRepository tradeAssetRepository;

    private final PlatformTransactionManager transactionManager;

    private final Map<String, TradeAsset> tradeAssetCacheMap = new HashMap<>();

    /**
     * loads trade assets
     * @param tradeId trade id
     * @param assetId asset id
     */
    public Optional<TradeAsset> load(String tradeId, String assetId) {
        TradeAssetEntity.Pk pk = TradeAssetEntity.Pk.builder()
                .tradeId(tradeId)
                .assetId(assetId)
                .build();
        return tradeAssetRepository.findById(pk)
                .map(TradeAsset::from);
    }

    /**
     * saves trade asset
     * 1. sends stomp message to specific destination
     * 2. if persist flag is true, save to database table
     * @param tradeAsset trade asset to handle
     */
    public void save(TradeAsset tradeAsset) {
        // trim message
        tradeAsset.setMessage(Optional.ofNullable(tradeAsset.getMessage())
                .map(String::trim)
                .orElse(null));

        // send message
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(tradeAsset);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        stringRedisTemplate.convertAndSend(TradeChannels.TRADE_ASSET.getTopic(), jsonString);

        // persist entity
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager, transactionDefinition);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            TradeAssetEntity statusEntity = TradeAssetEntity.builder()
                    .tradeId(tradeAsset.getTradeId())
                    .assetId(tradeAsset.getAssetId())
                    .previousClose(tradeAsset.getPreviousClose())
                    .open(tradeAsset.getOpen())
                    .close(tradeAsset.getClose())
                    .message(tradeAsset.getMessage())
                    .context(tradeAsset.getContext())
                    .build();
            tradeAssetRepository.save(statusEntity);
        });
    }

}
