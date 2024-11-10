package org.oopscraft.fintics.api.v1.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.model.Asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetResponse {

    private String assetId;

    private String name;

    private String symbol;

    private String market;

    private String exchange;

    private String type;

    private LocalDate updatedDate;

    private BigDecimal marketCap;

    private BigDecimal eps;

    private BigDecimal roe;

    private BigDecimal roa;

    private BigDecimal per;

    private Integer dividendFrequency;

    private BigDecimal dividendYield;

    private BigDecimal capitalGain;

    private BigDecimal totalReturn;

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
    public List<LinkResponse> getLinks() {
        return LinkResponseFactory.getLinks(this);
    }

    public static AssetResponse from(Asset asset) {
        return AssetResponse.builder()
                .assetId(asset.getAssetId())
                .symbol(asset.getSymbol())
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
                .dividendFrequency(asset.getDividendFrequency())
                .dividendYield(asset.getDividendYield())
                .capitalGain(asset.getCapitalGain())
                .totalReturn(asset.getTotalReturn())
                .build();
    }

}