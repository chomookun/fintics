package org.chomookun.fintics.web.ws.v1.trade.dto;

import lombok.*;
import org.chomookun.fintics.core.strategy.runner.StrategyResult;
import org.chomookun.fintics.core.trade.model.TradeAsset;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetMessage {

    private String tradeId;

    private String assetId;

    private LocalDateTime dateTime;

    private BigDecimal previousClose;

    private BigDecimal open;

    private BigDecimal close;

    private BigDecimal volume;

    private BigDecimal netChange;

    private BigDecimal netChangePercentage;

    private BigDecimal intraDayNetChange;

    private BigDecimal intraDayNetChangePercentage;

    private String message;

    private StrategyResult strategyResult;

    public static TradeAssetMessage from(TradeAsset tradeAsset) {
        return TradeAssetMessage.builder()
                .tradeId(tradeAsset.getTradeId())
                .assetId(tradeAsset.getAssetId())
                .dateTime(tradeAsset.getDateTime())
                .previousClose(tradeAsset.getPreviousClose())
                .open(tradeAsset.getOpen())
                .close(tradeAsset.getClose())
                .volume(tradeAsset.getVolume())
                .netChange(tradeAsset.getNetChange())
                .netChangePercentage(tradeAsset.getNetChangePercentage())
                .intraDayNetChange(tradeAsset.getIntraDayNetChange())
                .intraDayNetChangePercentage(tradeAsset.getIntraDayNetChangePercentage())
                .message(tradeAsset.getMessage())
                .strategyResult(tradeAsset.getStrategyResult())
                .build();
    }

    public static List<TradeAssetMessage> from(List<TradeAsset> tradeAssets) {
        return tradeAssets.stream()
                .map(TradeAssetMessage::from)
                .collect(Collectors.toList());
    }

}
