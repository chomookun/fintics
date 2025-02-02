package org.chomookun.fintics.service;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.client.dividend.DividendClient;
import org.chomookun.fintics.dao.DividendRepository;
import org.chomookun.fintics.model.Asset;
import org.chomookun.fintics.model.Dividend;
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

    /**
     * Returns dividends
     * @param assetId asset id
     * @param dateFrom date from
     * @param dateTo date to
     * @param pageable pageable
     * @return list of dividend
     */
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
