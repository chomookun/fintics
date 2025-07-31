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
def holdingCount = variables.holdingCount as Integer
def holdingWeight = variables.holdingWeight as BigDecimal

// US Growth ETFs
def usGrowthEtfs = [
        // my selection
        "JEPQ", // JPMorgan Nasdaq Equity Premium Income ETF
        "GPIQ", // Goldman Sachs Nasdaq-100 Premium Income ETF
        "QDVO", // Amplify ETF Trust Amplify CWP Growth & Income ETF
        // index
        "QQQ",  // Invesco QQQ Trust
        "SPY",  // SPDR S&P 500 ETF Trust
        "VUG",  // Vanguard Growth ETF
        "IUSG", // iShares Core S&P U.S. Growth ETF
        "IVW",  // iShares S&P 500 Growth ETF
        "XLK",  // Technology Select Sector SPDR Fund
        "IYW",  // iShares U.S. Technology ETF
        // sector
        "VGT",  // Vanguard Information Tech ETF
        "SMH",  // VanEck Vectors Semiconductor ETF
        "SOXX", // iShares PHLX Semiconductor ETF
        "AIQ",  // Global X Artificial Intelligence & Technology ETF
        "BOTZ", // Global X Robotics & Artificial Intelligence ETF
]

// US dividend ETFs
def usDividendEtfs = [
        // my selection
        "DGRW", // WisdomTree U.S. Quality Dividend Growth Fund
        "DIVO", // Amplify CWP Enhanced Dividend Income ETF
        "JEPI", // JPMorgan Equity Premium Income ETF
        "BALI", // iShares Advantage Large Cap Income ETF
        // index
        "DGRO", // iShares Core Dividend Growth ETF
        "SDY",  // SPDR S&P Dividend ETF
        "DVY",  // iShares Select Dividend ETF
        "VYM",  // Vanguard High Dividend Yield ETF
        "RDVY", // First Trust Rising Dividend Achievers ETF
        "FDVV", // Fidelity High Dividend ETF
]

// KR Growth ETFs
def krGrowthEtfs = [
        // my selection
        "472150",   // TIGER 배당커버드콜액티브
        "498400",   // KODEX 200타겟위클리커버드콜
        "496080",   // TIGER 코리아밸류업
        // index
        "069500",   // KODEX 200
        "494890",   // KODEX 200액티브
        "451060",   // 1Q K200액티브
        "495230",   // KoAct 코리아밸류업액티브
        "495060",   // TIMEFOLIO 코리아밸류업액티브
        "325010",   // KODEX 성장주
        "0074K0",   // KoAct K수출핵심기업TOP30액티브
        // sector
]

// KR dividend ETFs
def krDividendEtfs = [
        // my selection
        "441800",   // TIMEFOLIO Korea플러스배당액티브
        "161510",   // PLUS 고배당주
        "0018C0",   // PLUS 고배당주위클리고정커버드콜
        "0052D0",   // TIGER 코리아배당다우존스
        "498410",   // KODEX 금융고배당TOP10타겟위클리커버드콜
        // index
        "279530",   // KODEX 고배당주
        "104530",   // KIWOOM 고배당
        "266160",   // RISE 고배당
        "210780",   // TIGER 코스피고배당
        "322410",   // HANARO 고배당
        "211900",   // KODEX 배당성장
]

/**
 * gets rank ETF items for the given market and ETF symbols
 * @param market
 * @param etfSymbols
 * @param holdingCount
 * @return
 */
static List<Item> getRankEtfItems(market, etfSymbols, holdingCount) {
    List<Item> etfItems = []
    etfSymbols.each {
        etfItems.addAll(getEtfItems(market, it))
    }
    // symbol alias map
    Map<String, String> symbolAliasMap = [
            "GOOG": "GOOGL",    // Google Inc. Class C => Class A
            "005935": "005930", // 삼성전자 우선주 => 삼성전자
            "005385": "005380", // 현대자동차1우 => 현대자동차
            "005387": "005380"  // 현대자동차2우 => 현대자동차
    ]

    // 통합 기준으로 symbol 치환
    etfItems = etfItems.collect { item ->
        def unifiedSymbol = symbolAliasMap.get(item.symbol, item.symbol)
        item.symbol = unifiedSymbol
        item
    }

    // group by symbol and sum weights
    etfItems = etfItems
            .groupBy { it.symbol }
            .collect { symbol, items ->
                new Item(
                        symbol: symbol,
                        name: items[0].name,
                        weight: items*.weight.findAll().sum() ?: 0
                )
            }
    // sort by weight
    List<Item> topEtfItems = etfItems.sort { -(it.weight ?: 0) }
    return topEtfItems
}

def isMatchedAsset(market, symbol) {
    def assetId = "${market}.${symbol}"
    Asset asset = assetService.getAsset(assetId).orElse(null)
    if (asset == null) {
        return false
    }

    // STOCK 이 아니면 제외
    if (asset.getType() != "STOCK") {
        return false
    }

    // default true
    return true
}

//=========================================
// collect all etf assets
//=========================================
def rankGrowthItems = []
def rankDividendItems = []
switch (market) {
    case "US":
        rankGrowthItems = getRankEtfItems(market, usGrowthEtfs, holdingCount)
        rankDividendItems = getRankEtfItems(market, usDividendEtfs, holdingCount)
        break
    case "KR":
        rankGrowthItems = getRankEtfItems(market, krGrowthEtfs, holdingCount)
        rankDividendItems = getRankEtfItems(market, krDividendEtfs, holdingCount)
        break
    default:
        throw new RuntimeException("Unsupported market: ${market}")
}

// Top growth items
rankGrowthItems = rankGrowthItems.findAll{ isMatchedAsset(market, it.symbol)}
def topGrowthItems = rankGrowthItems.take(holdingCount)

// Top dividend items
rankDividendItems = rankDividendItems.findAll{ isMatchedAsset(market, it.symbol) }
rankDividendItems.removeIf(dividendItem -> topGrowthItems.any {it.symbol == dividendItem.symbol})
def topDividendItems = rankDividendItems.take(holdingCount)

//=========================================
// return
//=========================================
List<BasketRebalanceAsset> basketRebalanceAssets = []
topGrowthItems.forEach {
    BasketRebalanceAsset asset = BasketRebalanceAsset.builder()
            .symbol(it.symbol)
            .name(it.name)
            .holdingWeight(holdingWeight)
            .remark("Growth ETF (${it.weight}%)")
            .build()
    basketRebalanceAssets.add(asset)
}
topDividendItems.forEach {
    BasketRebalanceAsset asset = BasketRebalanceAsset.builder()
            .symbol(it.symbol)
            .name(it.name)
            .holdingWeight(holdingWeight)
            .remark("Dividend ETF (${it.weight}%)")
            .build()
    basketRebalanceAssets.add(asset)
}

log.info("basketRebalanceAssets: ${basketRebalanceAssets}")
return basketRebalanceAssets

