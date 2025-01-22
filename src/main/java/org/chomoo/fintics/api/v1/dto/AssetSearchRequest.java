package org.chomoo.fintics.api.v1.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetSearchRequest {

    private String assetId;

    private String name;

    private String market;

}
