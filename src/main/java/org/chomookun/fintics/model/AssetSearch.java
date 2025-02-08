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

}
