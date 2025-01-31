import groovy.json.JsonSlurper
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.jsoup.Jsoup
import org.chomookun.fintics.basket.BasketRebalanceAsset
import org.chomookun.fintics.model.Asset

/**
 * item
 */
@Builder
@ToString
class Item {
    String symbol
    String name
    String etfSymbol
    BigDecimal score
}

/**
 * gets ETF holdings
 * @param etfSymbol
 * @return
 */
static List<Item> getEtfItems(etfSymbol) {
    def url= new URL("https://finviz.com/api/etf_holdings/${etfSymbol}/top_ten")
    def responseJson= url.text
    def jsonSlurper = new JsonSlurper()
    def responseMap = jsonSlurper.parseText(responseJson)
    def top10Holdings = responseMap.get('rowData')
    // 결과 값이 이상할 경우(10개 이하인 경우) 에러 처리
    if (top10Holdings == null || top10Holdings.size() < 10) {
        throw new NoSuchElementException("Top 10 holdings data is incomplete or missing - ${etfSymbol}")
    }
    return top10Holdings.collect{
        Item.builder()
                .symbol(it.ticker as String)
                .name(it.name as String)
                .etfSymbol(etfSymbol)
                .build()
    }
}

//=======================================
// defines
//=======================================
List<Item> candidateItems = []

//=======================================
// collect etf items
//=======================================
// ETF list
def etfSymbols = [
        'DGRO',     // iShares Core Dividend Growth ETF
        'DGRW',     // WisdomTree U.S. Quality Dividend Growth Fund
        'JEPI',     // JPMorgan Equity Premium Income ETF
        'DIVO',     // Amplify CWP Enhanced Dividend Income ETF
        'SCHD',     // Schwab U.S. Dividend Equity ETF
        'MOAT',     // VanEck Morningstar Wide Moat ETF
]
etfSymbols.each{
    def etfItems = getEtfItems(it)
    println ("etfItems[${it}]: ${etfItems}")
    candidateItems.addAll(etfItems)
}

//========================================
// distinct items
//========================================
candidateItems = candidateItems
        .groupBy { it.symbol }
        .collect { symbol, items ->
            def item = items[0]
            def etfSymbol = items*.etfSymbol.join(',')
            return Item.builder()
                    .symbol(item.symbol)
                    .name(item.name)
                    .etfSymbol(etfSymbol)
                    .build();
        }
log.info("candidateItems: ${candidateItems}")

//=========================================
// filter
//=========================================
List<Item> finalItems = candidateItems.findAll {
    // checks already fixed
    boolean alreadyFixed = basket.getBasketAssets().findAll{balanceAsset ->
        balanceAsset.getSymbol() == it.symbol && balanceAsset.isFixed()
    }
    if (alreadyFixed) {
        return false
    }

    // check asset
    Asset asset = assetService.getAsset("US.${it.symbol}").orElse(null)
    if (asset == null) {
        return false
    }

    // STOCK 이 아니면 제외
    if (asset.getType() != "STOCK") {
        return false
    }

    //  ROE
    def roe = asset.getRoe() ?: 0.0
    if (roe < 5.0) {    // ROE 5 이하는 수익성 없는 회사로 제외
        return false
    }

    // PER
    def per = asset.getPer() ?: 100

    // dividendYield
    def dividendYield = asset.getDividendYield() ?: 0.0

    // score
    it.score = roe + dividendYield

    // return
    return it
}
log.info("finalItems: ${finalItems}")

//=========================================
// sort by score
//=========================================
def maxAssetCount = 50
def holdingWeightPerAsset = 2.0
def fixedAssetCount = basket.getBasketAssets().findAll{it.enabled && it.fixed}.size()
def targetAssetCount = (maxAssetCount - fixedAssetCount) as Integer
finalItems = finalItems
        .sort{ -(it.score?:0)}
        .take(targetAssetCount)

//=========================================
// return
//=========================================
List<BasketRebalanceAsset> basketRebalanceResults = finalItems.collect{
    BasketRebalanceAsset.of(it.symbol, it.name, holdingWeightPerAsset, it.etfSymbol)
}
log.info("basketRebalanceResults: ${basketRebalanceResults}")
return basketRebalanceResults
