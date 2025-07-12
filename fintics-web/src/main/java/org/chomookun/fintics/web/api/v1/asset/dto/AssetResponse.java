package org.chomookun.fintics.web.api.v1.asset.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.asset.model.Asset;

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

    private String sector;

    private String industry;

    private boolean favorite;

    private LocalDate updatedDate;

    private BigDecimal price;

    private BigDecimal volume;

    private BigDecimal marketCap;

    private BigDecimal eps;

    private BigDecimal epsGrowth;

    private BigDecimal roe;

    private BigDecimal roeGrowth;

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
                .sector(asset.getSector())
                .industry(asset.getIndustry())
                .favorite(asset.isFavorite())
                .updatedDate(asset.getUpdatedDate())
                .price(asset.getPrice())
                .volume(asset.getVolume())
                .marketCap(asset.getMarketCap())
                .eps(asset.getEps())
                .epsGrowth(asset.getEpsGrowth())
                .roe(asset.getRoe())
                .roeGrowth(asset.getRoeGrowth())
                .per(asset.getPer())
                .dividendFrequency(asset.getDividendFrequency())
                .dividendYield(asset.getDividendYield())
                .capitalGain(asset.getCapitalGain())
                .totalReturn(asset.getTotalReturn())
                .build();
    }

}