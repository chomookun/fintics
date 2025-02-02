package org.chomookun.fintics.model;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.dao.DividendEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class Dividend {

    private String assetId;

    private LocalDate date;

    private BigDecimal dividendPerShare;

    /**
     * factory method
     * @param dividendEntity dividend entity
     * @return dividend
     */
    public static Dividend from(DividendEntity dividendEntity) {
        return Dividend.builder()
                .assetId(dividendEntity.getAssetId())
                .date(dividendEntity.getDate())
                .dividendPerShare(dividendEntity.getDividendPerShare())
                .build();
    }

}
