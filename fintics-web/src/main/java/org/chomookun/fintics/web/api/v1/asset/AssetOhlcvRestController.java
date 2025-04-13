package org.chomookun.fintics.web.api.v1.asset;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.data.PageableUtils;
import org.chomookun.arch4j.web.common.doc.PageableAsQueryParam;
import org.chomookun.fintics.core.ohlcv.OhlcvService;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.chomookun.fintics.web.api.v1.asset.dto.OhlcvResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Tag(name = "asset")
@RestController
@RequestMapping("/api/v1/assets")
@PreAuthorize("hasAuthority('asset')")
@RequiredArgsConstructor
@Slf4j
public class AssetOhlcvRestController {

    private final OhlcvService ohlcvService;

    @Operation(summary = "Gets asset ohlcvs")
    @Parameter(name = "assetId", in = ParameterIn.PATH , required = true, example = "US.AAPL")
    @Parameter(name = "pageable", hidden = true)
    @PageableAsQueryParam
    @GetMapping("{assetId}/ohlcvs")
    @Cacheable(cacheNames = "ohlcvs", keyGenerator = "simpleKeyGenerator")
    public ResponseEntity<List<OhlcvResponse>> getAssetOhlcvs(
            @PathVariable("assetId") String assetId,
            @RequestParam(value = "type", required = false) Ohlcv.Type type,
            @RequestParam(value = "dateTimeFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTimeFrom,
            @RequestParam(value = "dateTimeTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTimeTo,
            Pageable pageable
    ) {
        // default parameter
        dateTimeFrom = Optional.ofNullable(dateTimeFrom)
                .orElse(LocalDate.of(1,1,1).atTime(LocalTime.MIN));
        dateTimeTo = Optional.ofNullable(dateTimeTo)
                .orElse(LocalDate.of(9999,12,31).atTime(LocalTime.MAX));
        // get ohlcvs
        List<OhlcvResponse> ohlcvResponses = ohlcvService.getOhlcvs(assetId, type, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(OhlcvResponse::from)
                .toList();
        // response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("ohlcvs", pageable))
                .body(ohlcvResponses);
    }

    /**
     * clears daily ohlcvs
     */
    @Scheduled(fixedRate = 60_000)
    @PreAuthorize("permitAll()")
    @CacheEvict(cacheNames = "ohlcvs", allEntries = true)
    public void clearDailyOhlcvs() {
        log.info("clear daily ohlcvs");
    }

}
