package org.chomookun.fintics.trade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.dao.TradeAssetEntity;
import org.chomookun.fintics.dao.TradeAssetRepository;
import org.chomookun.fintics.model.TradeAsset;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    private final String destination;

    private final boolean persist;

    private final TradeAssetRepository tradeAssetRepository;

    private final PlatformTransactionManager transactionManager;

    private final Map<String, TradeAsset> tradeAssetCacheMap = new HashMap<>();

    /**
     * loads trade assets
     * @param tradeId trade id
     * @param assetId asset id
     */
    public Optional<TradeAsset> load(String tradeId, String assetId) {
        if (persist) {
            TradeAssetEntity.Pk pk = TradeAssetEntity.Pk.builder()
                    .tradeId(tradeId)
                    .assetId(assetId)
                    .build();
            return tradeAssetRepository.findById(pk)
                    .map(TradeAsset::from);
        }
        // from cache
        String cacheKey = getCacheKey(tradeId, assetId);
        return Optional.ofNullable(tradeAssetCacheMap.get(cacheKey));
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
        messagingTemplate.convertAndSend(destination, jsonString);

        // persist entity
        if (this.persist) {
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
        // save cache
        String cacheKey = getCacheKey(tradeAsset.getTradeId(), tradeAsset.getAssetId());
        tradeAssetCacheMap.put(cacheKey, tradeAsset);
    }

    String getCacheKey(String tradeId, String assetId) {
        return String.format("%s-%s", tradeId, assetId);
    }

}
