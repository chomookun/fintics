package org.chomookun.fintics.web.api.v1.strategy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.chomookun.arch4j.core.common.data.PageableUtils;
import org.chomookun.fintics.web.api.v1.strategy.dto.StrategyRequest;
import org.chomookun.fintics.web.api.v1.strategy.dto.StrategyResponse;
import org.chomookun.fintics.core.strategy.model.Strategy;
import org.chomookun.fintics.core.strategy.model.StrategySearch;
import org.chomookun.fintics.core.strategy.StrategyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "strategy")
@RestController
@RequestMapping("/api/v1/strategies")
@PreAuthorize("hasAuthority('strategy')")
@RequiredArgsConstructor
public class StrategyRestController {

    private final StrategyService strategyService;

    @Operation(summary = "Returns list of strategy")
    @GetMapping
    public ResponseEntity<List<StrategyResponse>> getStrategies(
            @RequestParam(value = "name", required = false) String name,
            @PageableDefault Pageable pageable
    ) {
        StrategySearch strategySearch = StrategySearch.builder()
                .name(name)
                .build();
        Page<Strategy> strategyPage = strategyService.getStrategies(strategySearch, pageable);
        List<StrategyResponse> strategyResponses = strategyPage.getContent().stream()
                .map(StrategyResponse::from)
                .toList();
        long total = strategyPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("strategies", pageable, total))
                .body(strategyResponses);
    }

    @Operation(summary = "Gets the specified strategy")
    @GetMapping("{strategyId}")
    public ResponseEntity<StrategyResponse> getStrategy(@PathVariable("strategyId") String strategyId) {
        StrategyResponse ruleResponse = strategyService.getStrategy(strategyId)
                .map(StrategyResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(ruleResponse);
    }

    @Operation(summary = "Creates new strategy")
    @PostMapping
    @PreAuthorize("hasAuthority('strategy:edit')")
    @Transactional
    public ResponseEntity<StrategyResponse> createStrategy(@RequestBody StrategyRequest strategyRequest) {
        Strategy strategy = Strategy.builder()
                .name(strategyRequest.getName())
                .sort(strategyRequest.getSort())
                .language(strategyRequest.getLanguage())
                .variables(strategyRequest.getVariables())
                .script(strategyRequest.getScript())
                .build();
        Strategy savedStrategy = strategyService.saveStrategy(strategy);
        return ResponseEntity.ok(StrategyResponse.from(savedStrategy));
    }

    @Operation(summary = "Modifies the specified strategy")
    @PutMapping("{strategyId}")
    @PreAuthorize("hasAuthority('strategy:edit')")
    @Transactional
    public ResponseEntity<StrategyResponse> modifyStrategy(@PathVariable("strategyId") String strategyId, @RequestBody StrategyRequest strategyRequest) {
        Strategy strategy = strategyService.getStrategy(strategyId).orElseThrow();
        strategy.setName(strategyRequest.getName());
        strategy.setSort(strategyRequest.getSort());
        strategy.setLanguage(strategyRequest.getLanguage());
        strategy.setVariables(strategyRequest.getVariables());
        strategy.setScript(strategyRequest.getScript());
        Strategy savedStrategy = strategyService.saveStrategy(strategy);
        return ResponseEntity.ok(StrategyResponse.from(savedStrategy));
    }

    @Operation(summary = "Deletes the specified strategy")
    @DeleteMapping("{strategyId}")
    @PreAuthorize("hasAuthority('strategy:edit')")
    @Transactional
    public void deleteStrategy(@PathVariable("strategyId") String strategyId) {
        strategyService.deleteStrategy(strategyId);
    }

    @PatchMapping("{strategyId}/sort")
    public ResponseEntity<Void> changeStrategySort(@PathVariable("strategyId") String strategyId, @RequestParam("sort") Integer sort) {
        strategyService.changeStrategySort(strategyId, sort);
        return ResponseEntity.ok().build();
    }

}
