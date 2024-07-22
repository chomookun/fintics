package org.oopscraft.fintics.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LinkFactory {

    public static List<Link> getLinks(Asset asset) {
        String market = asset.getMarket();
        String type = asset.getType();
        String symbol = asset.getSymbol();
        List<Link> links = new ArrayList<>();
        switch(Optional.ofNullable(market).orElse("")) {
            case "US" -> {
                // nasdaq
                switch (Optional.ofNullable(type).orElse("")) {
                    case "STOCK" -> links.add(Link.of("Nasdaq", String.format("https://www.nasdaq.com/market-activity/stocks/%s/advanced-charting?timeframe=3m", symbol)));
                    case "ETF" -> links.add(Link.of("Nasdaq", String.format("https://www.nasdaq.com/market-activity/etf/%s/advanced-charting?timeframe=3m", symbol)));
                }
                links.add(Link.of("Yahoo", "https://finance.yahoo.com/chart/" + symbol));
                links.add(Link.of("Finviz", "https://finviz.com/quote.ashx?t=" + symbol));
            }
            case "KR" -> {
                links.add(Link.of("Naver", "https://finance.naver.com/item/fchart.naver?code=" + symbol));
                links.add(Link.of("Alphasquare", "https://alphasquare.co.kr/home/market-summary?code=" + symbol));
                // etf
                if (Objects.equals(type,"ETF")) {
                    links.add(Link.of("ETFCheck", "https://www.etfcheck.co.kr/mobile/etpitem/" + symbol));
                }
            }
            case "UPBIT" -> {
                links.add(Link.of("UPBIT", "https://upbit.com/exchange?code=CRIX.UPBIT." + symbol));
            }
        }
        return links;
    }

}
