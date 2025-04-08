package org.chomookun.fintics.web.api.v1.asset;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.web.common.doc.PageableAsQueryParam;
import org.chomookun.arch4j.core.common.data.PageableUtils;
import org.chomookun.fintics.web.api.v1.asset.dto.DividendResponse;
import org.chomookun.fintics.core.dividend.DividendService;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Tag(name = "asset")
@RestController
@RequestMapping("/api/v1/assets")
@PreAuthorize("hasAuthority('asset')")
@RequiredArgsConstructor
@Slf4j
public class AssetDividendRestController {

    private final DividendService dividendService;

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
        List<DividendResponse> dividendResponses = dividendService.getDividends(assetId, dateFrom, dateTo, pageable).stream()
                .map(DividendResponse::from)
                .toList();
        // response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("items", pageable))
                .body(dividendResponses);
    }

}
