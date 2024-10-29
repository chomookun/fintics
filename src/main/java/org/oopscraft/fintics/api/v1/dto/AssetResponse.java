package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetResponse {

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

    private String icon;

    @Builder.Default
    private List<LinkResponse> links = new ArrayList<>();

    public static AssetResponse from(Asset asset) {
        return AssetResponse.builder()
                .assetId(asset.getAssetId())
                .name(asset.getName())
                .market(asset.getMarket())
                .exchange(asset.getExchange())
                .type(asset.getType())
                .updatedDate(asset.getUpdatedDate())
                .marketCap(asset.getMarketCap())
                .eps(asset.getEps())
                .roe(asset.getRoe())
                .roa(asset.getRoa())
                .per(asset.getPer())
                .dividendYield(asset.getDividendYield())
                .icon(asset.getIcon())
                .links(LinkResponse.from(asset.getLinks()))
                .build();
    }

}