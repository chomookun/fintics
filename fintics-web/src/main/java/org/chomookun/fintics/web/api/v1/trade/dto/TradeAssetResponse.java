package org.chomookun.fintics.web.api.v1.trade.dto;

import lombok.*;
import org.chomookun.fintics.core.trade.model.TradeAsset;
import org.chomookun.fintics.web.api.v1.order.dto.StrategyResultResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetResponse {

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

    private StrategyResultResponse strategyResult;

    public static TradeAssetResponse from(TradeAsset tradeAsset) {
        TradeAssetResponse tradeAssetResponse = TradeAssetResponse.builder()
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
                .build();
        if (tradeAsset.getStrategyResult() != null) {
            tradeAssetResponse.setStrategyResult(StrategyResultResponse.from(tradeAsset.getStrategyResult()));
        }
        return tradeAssetResponse;
    }

    public static List<TradeAssetResponse> from(List<TradeAsset> profiles) {
        return profiles.stream()
                .map(TradeAssetResponse::from)
                .collect(Collectors.toList());
    }

}
