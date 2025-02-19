package org.chomookun.fintics.web.api.v1.strategy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.chomookun.fintics.core.strategy.model.Strategy;

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
