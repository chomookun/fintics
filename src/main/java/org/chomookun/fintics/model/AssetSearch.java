package org.chomookun.fintics.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetSearch {

    private String assetId;

    private String name;

    private String market;

    private String type;

    private Boolean favorite;

    private BigDecimal perFrom;

    private BigDecimal perTo;

    private BigDecimal roeFrom;

    private BigDecimal roeTo;

    private BigDecimal roaFrom;

    private BigDecimal roaTo;

}
