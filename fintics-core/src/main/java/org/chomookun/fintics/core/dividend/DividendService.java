package org.chomookun.fintics.core.dividend;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.asset.AssetService;
import org.chomookun.fintics.core.dividend.client.DividendClient;
import org.chomookun.fintics.core.dividend.repository.DividendRepository;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.dividend.model.Dividend;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DividendService {

    private final DividendRepository dividendRepository;

    private final DividendClient dividendClient;

    private final AssetService assetService;

    public List<Dividend> getDividends(String assetId, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        List<Dividend> dividends = dividendRepository.findByAssetIdAndDateBetweenOrderByDateDesc(assetId, dateFrom, dateTo).stream()
                .map(Dividend::from)
                .toList();
        // dividend client
        if (dividends.isEmpty()) {
            Asset asset = assetService.getAsset(assetId).orElseThrow();
            dividends = dividendClient.getDividends(asset, dateFrom, dateTo);
            // apply pageable (client not support pagination)
            if (pageable.isPaged()) {
                long startIndex = pageable.getOffset();
                long endIndex = Math.min(dividends.size(), startIndex + pageable.getPageSize());
                dividends = dividends.subList(Math.toIntExact(startIndex), Math.toIntExact(endIndex));
            }
        }
        // returns
        return dividends;
    }

}
