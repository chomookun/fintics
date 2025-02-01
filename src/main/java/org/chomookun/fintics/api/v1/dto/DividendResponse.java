package org.chomookun.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.model.Dividend;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Getter
public class DividendResponse {

    private LocalDate date;

    private BigDecimal dividendPerShare;

    /**
     * factory method
     * @param dividend dividend
     * @return dividend response
     */
    public static DividendResponse from(Dividend dividend) {
        return DividendResponse.builder()
                .date(dividend.getDate())
                .dividendPerShare(dividend.getDividendPerShare())
                .build();
    }

}
