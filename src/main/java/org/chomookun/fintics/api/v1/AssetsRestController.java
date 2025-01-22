package org.chomookun.fintics.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.web.common.data.PageableAsQueryParam;
import org.chomookun.arch4j.web.common.data.PageableUtils;
import org.chomookun.fintics.api.v1.dto.AssetResponse;
import org.chomookun.fintics.model.Asset;
import org.chomookun.fintics.model.AssetSearch;
import org.chomookun.fintics.service.AssetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@PreAuthorize("hasAuthority('api.assets')")
@Tag(name = "assets", description = "Assets")
@RequiredArgsConstructor
@Slf4j
public class AssetsRestController {

    private final AssetService assetService;

    /**
     * gets list of assets
     * @param assetId asset id
     * @param name asset name
     * @param market market
     * @param pageable pageable
     * @return list of assets
     */
    @GetMapping
    @Operation(summary = "gets list of assets")
    @PageableAsQueryParam
    public ResponseEntity<List<AssetResponse>> getAssets(
            @RequestParam(value = "assetId", required = false)
            @Parameter(name ="asset id", description = "asset id", example="US.AAPL")
                    String assetId,
            @RequestParam(value = "name", required = false)
            @Parameter(name = "name", description = "asset name")
                    String name,
            @RequestParam(value = "market", required = false)
            @Parameter(name= "market", description = "US|KR|...")
                    String market,
            @RequestParam(value = "type", required = false)
            @Parameter(name = "type", description = "STOCK|ETF")
                    String type,
            @Parameter(hidden = true)
                    Pageable pageable
    ) {
        AssetSearch assetSearch = AssetSearch.builder()
                .assetId(assetId)
                .name(name)
                .market(market)
                .type(type)
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

    /**
     * gets specific asset
     * @param assetId asset id
     * @return asset response
     */
    @GetMapping("{assetId}")
    @Operation(description = "get asset info")
    public ResponseEntity<AssetResponse> getAsset(
            @PathVariable("assetId")
            @Parameter(name = "asset id", description = "asset id", example = "US.AAPL")
                    String assetId
    ){
        AssetResponse assetResponse = assetService.getAsset(assetId)
                .map(AssetResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(assetResponse);
    }

}
