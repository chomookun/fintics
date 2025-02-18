package org.chomookun.fintics.web.api.v1.dividend;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.web.common.doc.PageableAsQueryParam;
import org.chomookun.arch4j.web.common.data.PageableUtils;
import org.chomookun.fintics.web.api.v1.dividend.dto.DividendResponse;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/dividends")
@PreAuthorize("hasAuthority('api.dividends')")
@Tag(name = "dividends", description = "dividends operation")
@RequiredArgsConstructor
public class DividendRestController {

    private final DividendService dividendService;

    @GetMapping("{assetId}")
    @Operation(description = "gets dividends")
    @PageableAsQueryParam
    public ResponseEntity<List<DividendResponse>> getDailyOhlcvs(
            @PathVariable("assetId")
            @Parameter(description = "asset id")
            String assetId,
            @RequestParam(value = "dateTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "date from")
            LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "date to")
            LocalDate dateTo,
            @Parameter(hidden = true)
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
