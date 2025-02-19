package org.chomookun.fintics.web.api.v1.asset.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetSearchRequest {

    private String assetId;

    private String name;

    private String market;

}
