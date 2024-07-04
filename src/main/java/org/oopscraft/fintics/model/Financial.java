package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.dao.FinancialEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class AssetFinancial {

    private String assetId;

    private Instant datetime;

    private BigDecimal issuedShares;

    private BigDecimal totalAssets;

    private BigDecimal totalEquity;

    private BigDecimal netIncome;

    private BigDecimal eps;

    private BigDecimal per;

    private BigDecimal roe;

    private BigDecimal roa;

    private BigDecimal ebitda;

    private BigDecimal dividendYield;

    public static AssetFinancial from(FinancialEntity assetFinancialEntity) {
        return AssetFinancial.builder()
                .assetId(assetFinancialEntity.getAssetId())
                .datetime(assetFinancialEntity.getDatetime())
                .issuedShares(assetFinancialEntity.getIssuedShares())
                .totalAssets(assetFinancialEntity.getTotalAssets())
                .totalEquity(assetFinancialEntity.getTotalEquity())
                .netIncome(assetFinancialEntity.getNetIncome())
                .eps(assetFinancialEntity.getEps())
                .per(assetFinancialEntity.getPer())
                .roe(assetFinancialEntity.getRoe())
                .roa(assetFinancialEntity.getRoa())
                .ebitda(assetFinancialEntity.getEbitda())
                .dividendYield(assetFinancialEntity.getDividendYield())
                .build();
    }

}
