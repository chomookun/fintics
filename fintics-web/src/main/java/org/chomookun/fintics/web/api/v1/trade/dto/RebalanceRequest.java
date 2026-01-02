package org.chomookun.fintics.web.api.v1.trade.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RebalanceRequest {

    private String assetId;

}
