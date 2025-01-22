package org.chomookun.fintics.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.chomookun.fintics.model.Strategy;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "strategy request")
public class StrategyRequest {

    @Schema(description = "strategy id")
    private String strategyId;

    @Schema(description = "name")
    private String name;

    @Schema(description = "language")
    private Strategy.Language language;

    @Schema(description = "variables")
    private String variables;

    @Schema(description = "script")
    private String script;

}
