package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.dao.AssetEntity;

import java.io.Serializable;
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

    private LocalDate updatedDate;

    private BigDecimal marketCap;

    private BigDecimal eps;

    private BigDecimal roe;

    private BigDecimal roa;

    private BigDecimal per;

    private BigDecimal dividendYield;

    /**
     * gets symbol
     * @return symbol
     */
    public String getSymbol() {
        return Optional.ofNullable(getAssetId())
                .map(string -> string.split("\\."))
                .filter(array -> array.length > 1)
                .map(array -> array[1])
                .orElseThrow(() -> new RuntimeException(String.format("invalid assetId[%s]", getAssetId())));
    }

    /**
     * get asset icon
     * @return icon url
     */
    public String getIcon() {
        return IconFactory.getIcon(this);
    }

    /**
     * gets asset link
     * @return link url
     */
    public List<Link> getLinks() {
        return LinkFactory.getLinks(this);
    }

    /**
     * asset factory method
     * @param assetEntity asset entity
     * @return asset
     */
    public static Asset from(AssetEntity assetEntity) {
        return Asset.builder()
                .assetId(assetEntity.getAssetId())
                .name(assetEntity.getName())
                .market(assetEntity.getMarket())
                .exchange(assetEntity.getExchange())
                .type(assetEntity.getType())
                .updatedDate(assetEntity.getUpdatedDate())
                .marketCap(assetEntity.getMarketCap())
                .eps(assetEntity.getEps())
                .roe(assetEntity.getRoe())
                .roa(assetEntity.getRoa())
                .per(assetEntity.getPer())
                .dividendYield(assetEntity.getDividendYield())
                .build();
    }

}
