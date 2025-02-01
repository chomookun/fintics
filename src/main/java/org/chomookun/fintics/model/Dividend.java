package org.chomookun.fintics.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class Dividend {

    private String assetId;

    private LocalDate date;

    private BigDecimal dividendPerShare;

}
