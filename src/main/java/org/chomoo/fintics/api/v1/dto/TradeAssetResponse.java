package org.chomoo.fintics.api.v1.dto;

import lombok.*;
import org.chomoo.fintics.model.TradeAsset;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetResponse {

    private String tradeId;

    private String assetId;

    private BigDecimal previousClose;

    private BigDecimal open;

    private BigDecimal close;

    private BigDecimal netChange;

    private BigDecimal netChangePercentage;

    private BigDecimal intraDayNetChange;

    private BigDecimal intraDayNetChangePercentage;

    private String message;

    public static TradeAssetResponse from(TradeAsset profile) {
        return TradeAssetResponse.builder()
                .tradeId(profile.getTradeId())
                .assetId(profile.getAssetId())
                .previousClose(profile.getPreviousClose())
                .open(profile.getOpen())
                .close(profile.getClose())
                .netChange(profile.getNetChange())
                .netChangePercentage(profile.getNetChangePercentage())
                .intraDayNetChange(profile.getIntraDayNetChange())
                .intraDayNetChangePercentage(profile.getIntraDayNetChangePercentage())
                .message(profile.getMessage())
                .build();
    }

    public static List<TradeAssetResponse> from(List<TradeAsset> profiles) {
        return profiles.stream()
                .map(TradeAssetResponse::from)
                .collect(Collectors.toList());
    }

}
