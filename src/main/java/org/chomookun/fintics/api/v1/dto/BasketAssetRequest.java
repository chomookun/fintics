package org.chomookun.fintics.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "trade asset request")
public class BasketAssetRequest {

    @Schema(description = "trade id", example = "test_trade")
    private String tradeId;

    @Schema(description = "asset id", example = "US.AAPL")
    private String assetId;

    @Schema(description = "fixed", example = "true")
    private boolean fixed;

    @Schema(description = "enabled", example = "true")
    private boolean enabled;

    @Schema(description = "hold ratio", example = "10")
    private BigDecimal holdingWeight;

    @Schema(description = "variables", example = "param1=value1")
    private String variables;

}
