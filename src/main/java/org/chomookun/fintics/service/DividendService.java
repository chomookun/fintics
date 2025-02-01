package org.chomookun.fintics.service;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.client.dividend.DividendClient;
import org.chomookun.fintics.model.Asset;
import org.chomookun.fintics.model.Dividend;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DividendService {

    private final DividendClient dividendClient;

    private final AssetService assetService;

    public List<Dividend> getDividends(String assetId, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        List<Dividend> dividends = new ArrayList<>();
        Asset asset = assetService.getAsset(assetId).orElseThrow();
        dividends = dividendClient.getDividends(asset, dateFrom, dateTo);
        return dividends;
    }

}
