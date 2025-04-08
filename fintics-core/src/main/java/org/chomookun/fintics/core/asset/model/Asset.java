package org.chomookun.fintics.core.asset.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.asset.entity.AssetEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Asset {

    private String assetId;

    private String name;

    private String market;

    private String exchange;

    private String type;

    private boolean favorite;

    private LocalDate updatedDate;

    private BigDecimal price;

    private BigDecimal volume;

    private BigDecimal marketCap;

    private BigDecimal eps;

    private BigDecimal roe;

    private BigDecimal per;

    private Integer dividendFrequency;

    private BigDecimal dividendYield;

    private BigDecimal capitalGain;

    private BigDecimal totalReturn;

    public String getSymbol() {
        return Optional.ofNullable(getAssetId())
                .map(string -> string.split("\\."))
                .filter(array -> array.length > 1)
                .map(array -> array[1])
                .orElseThrow(() -> new RuntimeException(String.format("invalid assetId[%s]", getAssetId())));
    }

    public static Asset from(AssetEntity assetEntity) {
        return Asset.builder()
                .assetId(assetEntity.getAssetId())
                .name(assetEntity.getName())
                .market(assetEntity.getMarket())
                .exchange(assetEntity.getExchange())
                .type(assetEntity.getType())
                .favorite(assetEntity.isFavorite())
                .updatedDate(assetEntity.getUpdatedDate())
                .price(assetEntity.getPrice())
                .volume(assetEntity.getVolume())
                .marketCap(assetEntity.getMarketCap())
                .eps(assetEntity.getEps())
                .roe(assetEntity.getRoe())
                .per(assetEntity.getPer())
                .dividendFrequency(assetEntity.getDividendFrequency())
                .dividendYield(assetEntity.getDividendYield())
                .capitalGain(assetEntity.getCapitalGain())
                .totalReturn(assetEntity.getTotalReturn())
                .build();
    }

}
