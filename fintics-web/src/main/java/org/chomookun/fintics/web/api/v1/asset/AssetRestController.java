package org.chomookun.fintics.web.api.v1.asset;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.web.common.doc.PageableAsQueryParam;
import org.chomookun.arch4j.core.common.data.PageableUtils;
import org.chomookun.fintics.core.asset.model.Ohlcv;
import org.chomookun.fintics.web.api.v1.asset.dto.AssetResponse;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.asset.model.AssetSearch;
import org.chomookun.fintics.core.asset.AssetService;
import org.chomookun.fintics.web.api.v1.asset.dto.DividendResponse;
import org.chomookun.fintics.web.api.v1.asset.dto.OhlcvResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
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
public class AssetRestController {

    private final AssetService assetService;

    @Operation(summary = "Returns list of asset")
    @Parameter(name = "pageable", hidden = true)
    @PageableAsQueryParam
    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAssets(
            @RequestParam(value = "assetId", required = false) String assetId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "market", required = false) String market,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "favorite", required = false) Boolean favorite,
            @PageableDefault Pageable pageable
    ) {
        AssetSearch assetSearch = AssetSearch.builder()
                .assetId(assetId)
                .name(name)
                .market(market)
                .type(type)
                .favorite(favorite)
                .build();
        Page<Asset> assetPage = assetService.getAssets(assetSearch, pageable);
        List<AssetResponse> assetResponses = assetPage.getContent().stream()
                .map(AssetResponse::from)
                .toList();
        long total = assetPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("asset", pageable, total))
                .body(assetResponses);
    }

    @Operation(summary = "Get asset info")
    @Parameter(name = "assetId", example = "US.AAPL", required = true, in = ParameterIn.PATH)
    @GetMapping("{assetId}")
    public ResponseEntity<AssetResponse> getAsset(@PathVariable("assetId") String assetId){
        AssetResponse assetResponse = assetService.getAsset(assetId)
                .map(AssetResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(assetResponse);
    }

    @Operation(summary = "Gets asset ohlcvs")
    @Parameter(name = "assetId", in = ParameterIn.PATH , required = true, example = "US.AAPL")
    @Parameter(name = "pageable", hidden = true)
    @PageableAsQueryParam
    @GetMapping("{assetId}/ohlcvs")
    public ResponseEntity<List<OhlcvResponse>> getOhlcvs(
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
        List<OhlcvResponse> ohlcvResponses = assetService.getOhlcvs(assetId, type, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(OhlcvResponse::from)
                .toList();
        // response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("ohlcvs", pageable))
                .body(ohlcvResponses);
    }

    @Operation(summary = "Gets asset dividends")
    @GetMapping("{assetId}/dividends")
    @PageableAsQueryParam
    public ResponseEntity<List<DividendResponse>> getDividends(
            @PathVariable("assetId") String assetId,
            @RequestParam(value = "dateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Pageable pageable
    ) {
        // default parameter
        dateFrom = Optional.ofNullable(dateFrom)
                .orElse(LocalDate.of(1,1,1));
        dateTo = Optional.ofNullable(dateTo)
                .orElse(LocalDate.of(9999,12,31));
        // get ohlcvs
        List<DividendResponse> dividendResponses = assetService.getDividends(assetId, dateFrom, dateTo, pageable).stream()
                .map(DividendResponse::from)
                .toList();
        // response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("items", pageable))
                .body(dividendResponses);
    }

    @Operation(summary = "Creates favorite")
    @PostMapping("{assetId}/favorite")
    @PreAuthorize("hasAuthority('asset:edit')")
    @Transactional
    public ResponseEntity<Void> createFavorite(@PathVariable("assetId") String assetId){
        assetService.setFavorite(assetId, true);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Deletes favorite")
    @DeleteMapping("{assetId}/favorite")
    @PreAuthorize("hasAuthority('asset:edit')")
    @Transactional
    public ResponseEntity<Void> deleteFavorite(@PathVariable("assetId") String assetId){
        assetService.setFavorite(assetId, false);
        return ResponseEntity.ok().build();
    }


}
