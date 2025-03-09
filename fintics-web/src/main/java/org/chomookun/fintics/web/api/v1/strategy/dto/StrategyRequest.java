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

    private String strategyId;

    private String name;

    private Integer sort;

    private Strategy.Language language;

    private String variables;

    private String script;

}
