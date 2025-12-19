package org.chomookun.fintics.web.api.v1.basket;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.chomookun.arch4j.core.common.support.texttable.TextTable;
import org.chomookun.arch4j.core.common.data.PageableUtils;
import org.chomookun.fintics.core.basket.model.BasketDivider;
import org.chomookun.fintics.core.basket.rebalance.*;
import org.chomookun.fintics.web.api.v1.basket.dto.BasketRequest;
import org.chomookun.fintics.web.api.v1.basket.dto.BasketResponse;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.basket.model.BasketAsset;
import org.chomookun.fintics.core.basket.model.BasketSearch;
import org.chomookun.fintics.core.basket.BasketService;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "basket")
@RestController
@RequestMapping("/api/v1/baskets")
@PreAuthorize("hasAuthority('basket')")
@RequiredArgsConstructor
@Slf4j
public class BasketRestController {

    private final BasketService basketService;

    private final BasketScriptRunnerFactory basketScriptRunnerFactory;

    private final BasketRebalanceTaskExecutor basketRebalanceTaskExecutor;
    private final BasketRebalanceTaskFactory basketRebalanceTaskFactory;

    @Operation(summary = "Gets baskets")
    @GetMapping
    public ResponseEntity<List<BasketResponse>> getBrokers(
            @RequestParam(value = "name", required = false) String name,
            @PageableDefault Pageable pageable
    ) {
        BasketSearch basketSearch = BasketSearch.builder()
                .name(name)
                .build();
        Page<Basket> basketPage = basketService.getBaskets(basketSearch, pageable);
        List<BasketResponse> basketResponses = basketPage.getContent().stream()
                .map(BasketResponse::from)
                .toList();
        long total = basketPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("basket", pageable, total))
                .body(basketResponses);
    }

    @Operation(summary = "Gets basket")
    @GetMapping("{basketId}")
    public ResponseEntity<BasketResponse> getBasket(@PathVariable("basketId") String basketId) {
        BasketResponse basketResponse = basketService.getBasket(basketId)
                .map(BasketResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(basketResponse);
    }

    @Operation(summary = "Creates new basket")
    @PostMapping
    @PreAuthorize("hasAuthority('basket:edit')")
    public ResponseEntity<BasketResponse> createBasket(@RequestBody BasketRequest basketRequest) {
        // basket
        Basket basket = Basket.builder()
                .name(basketRequest.getName())
                .sort(basketRequest.getSort())
                .market(basketRequest.getMarket())
                .rebalanceEnabled(basketRequest.isRebalanceEnabled())
                .rebalanceSchedule(basketRequest.getRebalanceSchedule())
                .language(basketRequest.getLanguage())
                .variables(basketRequest.getVariables())
                .script(basketRequest.getScript())
                .build();
        // basket assets
        List<BasketAsset> basketAssets = basketRequest.getBasketAssets().stream()
                .map(basketAssetRequest ->
                        BasketAsset.builder()
                                .assetId(basketAssetRequest.getAssetId())
                                .fixed(basketAssetRequest.isFixed())
                                .enabled(basketAssetRequest.isEnabled())
                                .holdingWeight(basketAssetRequest.getHoldingWeight())
                                .variables(basketAssetRequest.getVariables())
                        .build())
                .collect(Collectors.toList());
        basket.setBasketAssets(basketAssets);
        // basket dividers
        List<BasketDivider> basketDividers = basketRequest.getBasketDividers().stream()
                .map(basketDividerRequest -> {
                    return BasketDivider.builder()
                            .dividerId(basketDividerRequest.getDividerId())
                            .sort(basketDividerRequest.getSort())
                            .name(basketDividerRequest.getName())
                            .build();
                })
                .collect(Collectors.toList());
        basket.setBasketDividers(basketDividers);
        // save
        Basket savedBasket = basketService.saveBasket(basket);
        // submit rebalance task
        BasketRebalanceTask basketRebalanceTask = basketRebalanceTaskFactory.getObject(savedBasket);
        basketRebalanceTaskExecutor.submitTask(basketRebalanceTask);
        // response
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BasketResponse.from(savedBasket));
    }

    @Operation(summary = "Modifies specified broker")
    @PutMapping("{basketId}")
    @PreAuthorize("hasAuthority('basket:edit')")
    public ResponseEntity<BasketResponse> modifyBasket(@PathVariable("basketId") String basketId, @RequestBody BasketRequest basketRequest) {
        // basket
        Basket basket = basketService.getBasket(basketId).orElseThrow();
        basket.setName(basketRequest.getName());
        basket.setSort(basketRequest.getSort());
        basket.setMarket(basketRequest.getMarket());
        basket.setRebalanceEnabled(basketRequest.isRebalanceEnabled());
        basket.setRebalanceSchedule(basketRequest.getRebalanceSchedule());
        basket.setLanguage(basketRequest.getLanguage());
        basket.setVariables(basketRequest.getVariables());
        basket.setScript(basketRequest.getScript());
        // basket assets
        List<BasketAsset> basketAssets = basketRequest.getBasketAssets().stream()
                .map(basketAssetRequest -> {
                    return BasketAsset.builder()
                            .assetId(basketAssetRequest.getAssetId())
                            .sort(basketAssetRequest.getSort())
                            .fixed(basketAssetRequest.isFixed())
                            .enabled(basketAssetRequest.isEnabled())
                            .holdingWeight(basketAssetRequest.getHoldingWeight())
                            .variables(basketAssetRequest.getVariables())
                            .build();
                })
                .collect(Collectors.toList());
        basket.setBasketAssets(basketAssets);
        // basket dividers
        List<BasketDivider> basketDividers = basketRequest.getBasketDividers().stream()
                .map(basketDividerRequest -> {
                    return BasketDivider.builder()
                            .dividerId(basketDividerRequest.getDividerId())
                            .sort(basketDividerRequest.getSort())
                            .name(basketDividerRequest.getName())
                            .build();
                })
                .collect(Collectors.toList());
        basket.setBasketDividers(basketDividers);
        // save
        Basket savedBasket = basketService.saveBasket(basket);
        // submit rebalance task
        BasketRebalanceTask basketRebalanceTask = basketRebalanceTaskFactory.getObject(savedBasket);
        basketRebalanceTaskExecutor.submitTask(basketRebalanceTask);
        // response
        return ResponseEntity.ok(BasketResponse.from(savedBasket));
    }

    @Operation(summary = "Deletes specified basket")
    @DeleteMapping("{basketId}")
    @PreAuthorize("hasAuthority('basket:edit')")
    public ResponseEntity<Void> deleteBasket(@PathVariable("basketId") String basketId) {
        basketService.deleteBasket(basketId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Changes basket sort")
    @PatchMapping("{basketId}/sort")
    @PreAuthorize("hasAuthority('basket:edit')")
    public ResponseEntity<Void> changeBasketSort(@PathVariable("basketId") String basketId, @RequestParam("sort") Integer sort) {
        basketService.changeBasketSort(basketId, sort);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Tests basket rebalance script")
    @PostMapping("test")
    @PreAuthorize("hasAuthority('basket:edit')")
    public ResponseEntity<StreamingResponseBody> testBasket(@RequestBody BasketRequest basketRequest) {
        StreamingResponseBody stream = outputStream -> {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger logger = (Logger) LoggerFactory.getLogger(this.getClass().getSimpleName());
            logger.setLevel(Level.DEBUG);
            // PatternLayout 생성 및 설정 (로그 메시지 형식 지정)
            PatternLayout layout = new PatternLayout();
            layout.setPattern("%msg%n");
            layout.setContext(context);
            layout.start();
            OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
            appender.setContext(context);
            appender.setLayout(layout);
            appender.setOutputStream(outputStream);
            appender.setImmediateFlush(true);
            logger.addAppender(appender);
            try {
                // start logger
                appender.start();
                // creates basket rebalance runner
                Basket basket = Basket.builder()
                        .language(basketRequest.getLanguage())
                        .variables(basketRequest.getVariables())
                        .script(basketRequest.getScript())
                        .build();
                BasketScriptRunner basketRebalanceRunner = basketScriptRunnerFactory.getObject(basket);
                basketRebalanceRunner.setLog(logger);
                List<BasketRebalanceAsset> basketRebalanceAssets = basketRebalanceRunner.run();
                // prints results
                TextTable textTable = TextTable.valueOf(basketRebalanceAssets);
                logger.info("# Basket Rebalance Assets [{}]", basketRebalanceAssets.size());
                logger.info("{}", textTable);
                // Flush after logging to ensure logs are sent
                outputStream.flush();

            } catch (Throwable e) {
                outputStream.write(ExceptionUtils.getStackTrace(e).getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                throw new RuntimeException(e);
            } finally {
                // stop logger
                appender.stop();
                logger.detachAppender(appender);
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(stream);
    }

}
