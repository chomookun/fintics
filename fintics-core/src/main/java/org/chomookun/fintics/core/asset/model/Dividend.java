package org.chomookun.fintics.core.asset.model;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.asset.entity.DividendEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class Dividend {

    private String assetId;

    private LocalDate date;

    private BigDecimal dividendPerShare;

    public static Dividend from(DividendEntity dividendEntity) {
        return Dividend.builder()
                .assetId(dividendEntity.getAssetId())
                .date(dividendEntity.getDate())
                .dividendPerShare(dividendEntity.getDividendPerShare())
                .build();
    }

}
