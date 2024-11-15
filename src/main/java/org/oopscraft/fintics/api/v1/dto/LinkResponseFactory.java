package org.oopscraft.fintics.api.v1.dto;

import org.oopscraft.fintics.api.v1.dto.AssetResponse;
import org.oopscraft.fintics.api.v1.dto.LinkResponse;
import org.oopscraft.fintics.model.Asset;

import java.util.*;

public class LinkResponseFactory {

    public static List<LinkResponse> getLinks(AssetResponse asset) {
        List<LinkResponse> links = new ArrayList<>();
        String symbol = asset.getSymbol();
        String market = Optional.ofNullable(asset.getMarket()).orElse("");
        switch (market) {
            case "US" -> links.addAll(getUsLinks(asset));
            case "KR" -> links.addAll(getKrLinks(asset));
            case "UPBIT" -> links.addAll(getUpbitLinks(asset));
        }
        return links;
    }

    static List<LinkResponse> getUsLinks(AssetResponse asset) {
        List<LinkResponse> links = new ArrayList<>();
        String symbol = asset.getSymbol();
        String type = Optional.ofNullable(asset.getType()).orElse("");
        String exchange = Optional.ofNullable(asset.getExchange()).orElse("");
        // alphasquare
        links.add(LinkResponse.of("Alphasquare", String.format("https://alphasquare.co.kr/home/market-summary?code=%s", asset.getSymbol())));
        // nasdaq
        switch (type) {
            case "STOCK" -> links.add(LinkResponse.of("Nasdaq", String.format("https://www.nasdaq.com/market-activity/stocks/%s", symbol)));
            case "ETF" -> links.add(LinkResponse.of("Nasdaq", String.format("https://www.nasdaq.com/market-activity/etf/%s", symbol)));
        }
        // yahoo
        links.add(LinkResponse.of("Yahoo", String.format("https://finance.yahoo.com/quote/%s", symbol)));
        // finviz
        links.add(LinkResponse.of("Finviz", String.format("https://finviz.com/quote.ashx?t=%s", symbol)));
        // seekingalpha
        links.add(LinkResponse.of("Seekingalpha", String.format("https://seekingalpha.com/symbol/%s", symbol)));
        // morningstar
        switch (exchange) {
            case "XASE" -> links.add(LinkResponse.of("Morningstar", String.format("https://www.morningstar.com/%ss/arcx/%s/quote", type.toLowerCase(), symbol.toLowerCase())));
            default -> links.add(LinkResponse.of("Morningstar", String.format("https://www.morningstar.com/%ss/%s/%s/quote", type.toLowerCase(), exchange.toLowerCase(), symbol.toLowerCase())));
        }
        // etf.com
        if (Objects.equals(type, "ETF")) {
            links.add(LinkResponse.of("etf.com", String.format("https://etf.com/%s", symbol)));
        }
        // return
        return links;
    }

    static List<LinkResponse> getKrLinks(AssetResponse asset) {
        List<LinkResponse> links = new ArrayList<>();
        // alphasquare
        links.add(LinkResponse.of("Alphasquare", String.format("https://alphasquare.co.kr/home/market-summary?code=%s", asset.getSymbol())));
        // naver
        links.add(LinkResponse.of("Naver", String.format("https://finance.naver.com/item/main.naver?code=%s", asset.getSymbol())));
        // etf
        if (Objects.equals(asset.getType(),"ETF")) {
            // k-etf
            links.add(LinkResponse.of("K-ETF", String.format("https://www.k-etf.com/etf/%s",asset.getSymbol())));
            // etfcheck
            links.add(LinkResponse.of("ETFCheck", String.format("https://www.etfcheck.co.kr/mobile/etpitem/%s", asset.getSymbol())));
        }
        // return
        return links;
    }

    static List<LinkResponse> getUpbitLinks(AssetResponse asset) {
        return List.of(
                LinkResponse.of("Upbit", String.format("https://upbit.com/exchange?code=CRIX.UPBIT.%s", asset.getSymbol()))
        );
    }

}
