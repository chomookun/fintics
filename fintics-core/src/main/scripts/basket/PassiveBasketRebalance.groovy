import groovy.json.JsonSlurper
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.chomookun.fintics.core.basket.rebalance.BasketRebalanceAsset

/**
 * item
 */
@Builder
@ToString
class Item {
    String symbol
    String name
    BigDecimal weight
}

static List<Item> getEtfItems(market, etfSymbol) {
    return switch(market) {
        case "US" -> getUsEtfItems(etfSymbol)
        case "KR" -> getKrEtfItems(etfSymbol)
        default -> throw new RuntimeException("Unsupported market: ${market}")
    }
}

/**
 * gets US ETF holdings
 * @param etfSymbol
 * @return
 */
static List<Item> getUsEtfItems(etfSymbol) {
    try {
        def url= new URL("https://finviz.com/api/etf_holdings/${etfSymbol}/top_ten")
        def responseJson= url.text
        def jsonSlurper = new JsonSlurper()
        def responseMap = jsonSlurper.parseText(responseJson)
        def top10Holdings = responseMap.get('rowData')
        // 결과 값이 이상할 경우(10개 이하인 경우) 에러 처리
        if (top10Holdings == null || top10Holdings.size() < 10) {
            return []
        }
        return top10Holdings.collect{
            Item.builder()
                    .symbol(it.ticker as String)
                    .name(it.name as String)
                    .weight(it.weight * 100 as BigDecimal) // Convert to percentage
                    .build()
        }
    } catch (Exception e) {
        println(e.getMessage())
        return []
    }
}

/**
 * gets KR etf items
 * @param etfSymbol
 * @return
 */
static List<Item> getKrEtfItems(etfSymbol) {
    try {
        def url = new URL("https://m.stock.naver.com/api/stock/${etfSymbol}/etfAnalysis")
        def responseJson = url.text
        def jsonSlurper = new JsonSlurper()
        def responseMap = jsonSlurper.parseText(responseJson)
        def top10Holdings = responseMap.get('etfTop10MajorConstituentAssets')
        // 결과 값이 이상할 경우(10개 이하인 경우) 에러 처리
        if (top10Holdings == null) {
            return []
        }
        return top10Holdings.collect {
            Item.builder()
                    .symbol(it.itemCode as String)
                    .name(it.itemName as String)
                    .weight(it.etfWeight.replace("%", "") as BigDecimal)
                    .build()
        }
    }catch (Exception e) {
        println(e.getMessage())
        return []
    }
}


//=======================================
// defines
//=======================================
def market = variables.market
def holdingCount = 10
def holdingWeight = 3.5

def usGrowthEtfs = [
    // large cap growth ETFs
    "QQQ",  // Invesco QQQ Trust
    "SPY",  // SPDR S&P 500
]

def usDividendEtfs = [
    // large cap dividend ETFs
    "DGRO", // iShares Core Dividend Growth ETF
    "SDY",  // SPDR S&P Dividend ETF
    "DVY",  // iShares Select Dividend ETF
    // my selection
    "DGRW", // WisdomTree U.S. Quality Dividend Growth Fund
]

def krGrowthEtfs = [
    // KOSPI 200 Growth ETFs
    "069500", // KODEX 200
]

def krDividendEtfs = [
    // KOSPI 200 Dividend ETFs
    "091160", // KODEX 배당성장
    "139260", // TIGER 배당성장
]

/**
 * gets top ETF items for the given market and ETF symbols
 * @param market
 * @param etfSymbols
 * @param holdingCount
 * @return
 */
static List<Item> getTopEtfItems(market, etfSymbols, holdingCount) {
    List<Item> etfItems = []
    etfSymbols.each {
        etfItems.addAll(getEtfItems(market, it))
    }
    // top 10 holdings by weight
    List<Item> topEtfItems = etfItems.sort { -(it.weight ?: 0) }.take(holdingCount)
    return topEtfItems
}

//=========================================
// collect etf assets
//=========================================
def topGrowthItems = []
def topDividendItems = []
switch (market) {
    case "US":
        topGrowthItems = getTopEtfItems(market, usGrowthEtfs, holdingCount)
        topDividendItems = getTopEtfItems(market, usDividendEtfs, holdingCount)
        break
    case "KR":
        topGrowthItems = getTopEtfItems(market, krGrowthEtfs, holdingCount)
        topDividendItems = getTopEtfItems(market, krDividendEtfs, holdingCount)
        break
    default:
        throw new RuntimeException("Unsupported market: ${market}")
}

//=========================================
// return
//=========================================
List<BasketRebalanceAsset> basketRebalanceAssets = []
topGrowthItems.forEach {
    BasketRebalanceAsset asset = BasketRebalanceAsset.builder()
            .symbol(it.symbol)
            .name(it.name)
            .holdingWeight(holdingWeight)
            .remark("Growth ETF")
            .build()
    basketRebalanceAssets.add(asset)
}
topDividendItems.forEach {
    BasketRebalanceAsset asset = BasketRebalanceAsset.builder()
            .symbol(it.symbol)
            .name(it.name)
            .holdingWeight(holdingWeight)
            .remark("Dividend ETF")
            .build()
    basketRebalanceAssets.add(asset)
}

log.info("basketRebalanceAssets: ${basketRebalanceAssets}")
return basketRebalanceAssets

