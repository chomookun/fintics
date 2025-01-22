package org.chomookun.fintics.api.v1.dto;

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
