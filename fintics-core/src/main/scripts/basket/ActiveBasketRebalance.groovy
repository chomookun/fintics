import groovy.json.JsonSlurper
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.chomookun.fintics.core.basket.rebalance.BasketRebalanceAsset

import java.math.RoundingMode

/**
 * item
 */
@Builder
@ToString
class Item {
    String symbol
    String name
    BigDecimal count
    BigDecimal weight
    BigDecimal score
    String remark
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
        top10Holdings = top10Holdings.collect{
            Item.builder()
                    .symbol(it.ticker as String)
                    .name(it.name as String)
                    .weight(it.weight * 100 as BigDecimal) // Convert to percentage
                    .build()
        }
        // re-calculate weight in top holdings
        def top10Weight = top10Holdings.sum { it.weight } as BigDecimal
        top10Holdings = top10Holdings.collect {
            it.weight = (it.weight / top10Weight) * 100 // Convert to percentage
            return it
        }
        // return top 10 holdings
        return top10Holdings
    } catch (Exception e) {
        println(e.getMessage())
        throw e
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
        top10Holdings = top10Holdings.collect {
            Item.builder()
                    .symbol(it.itemCode as String)
                    .name(it.itemName as String)
                    .weight(it.etfWeight.replace("%", "") as BigDecimal)
                    .build()
        }
        // re-calculate weight in top holdings
        def top10Weight = top10Holdings.sum { it.weight } as BigDecimal
        top10Holdings = top10Holdings.collect {
            it.weight = (it.weight / top10Weight) * 100 // Convert to percentage
            return it
        }
        // return top 10 holdings
        return top10Holdings
    }catch (Exception e) {
        println(e.getMessage())
        throw e
    }
}


//=======================================
// defines
//=======================================
log.info("== variables: ${variables}")
def market = variables.market
def totalHoldingWeight = variables.totalHoldingWeight as BigDecimal
def holdingCount = variables.holdingCount as Integer
def maxHoldingWeight = variables.maxHoldingWeight as BigDecimal
def minHoldingWeight = variables.minHoldingWeight as BigDecimal
def stepHoldingWeight = variables.stepHoldingWeight as BigDecimal

// US ETFs
def usEtfs = [
        // my selection
        "JEPQ", // JPMorgan Nasdaq Equity Premium Income ETF
        "GPIQ", // Goldman Sachs Nasdaq-100 Premium Income ETF
        "QDVO", // Amplify ETF Trust Amplify CWP Growth & Income ETF
        "DGRW", // WisdomTree U.S. Quality Dividend Growth Fund
        "DIVO", // Amplify CWP Enhanced Dividend Income ETF
        "BALI", // iShares Advantage Large Cap Income ETF
        "JEPI", // JPMorgan Equity Premium Income ETF
        // growth
        "QQQ",  // Invesco QQQ Trust
        "SPY",  // SPDR S&P 500 ETF Trust
        "SPYG", // SPDR Portfolio S&P 500 Growth ETF
        "VOO",  // Vanguard S&P 500 ETF
        "VOOG", // Vanguard S&P 500 Growth ETF
        "SPMO", // Invesco S&P 500 Momentum ETF
        "IWF",  // iShares Russell 1000 Growth ETF
        "VUG",  // Vanguard Growth ETF
        "IUSG", // iShares Core S&P U.S. Growth ETF
        "IVW",  // iShares S&P 500 Growth ETF
        "MGK",  // Vanguard Mega Cap Growth ETF
        "XLK",  // Technology Select Sector SPDR Fund
        "IYW",  // iShares U.S. Technology ETF
        "VGT",  // Vanguard Information Tech ETF
        "QGRW", // WisdomTree Trust WisdomTree U.S. Quality Growth Fund
        "XNTK", // SPDR NYSE Technology ETF
        "BAI",  // BlackRock ETF Trust iShares A.I. Innovation and Tech Active ET
        "FXL",  // First Trust Technology AlphaDEX
        "XT",   // iShares Exponential Technologies ETF
        // dividend
        "DGRO", // iShares Core Dividend Growth ETF
        "SHCH", // Schwab U.S. Dividend Equity ETF
        "SDY",  // SPDR S&P Dividend ETF
        "DVY",  // iShares Select Dividend ETF
        "VYM",  // Vanguard High Dividend Yield ETF
        "RDVY", // First Trust Rising Dividend Achievers ETF
        "FDVV", // Fidelity High Dividend ETF
        "FDRR", // Fidelity Dividend ETF for Rising Rates
        "DLN",  // WisdomTree U.S. LargeCap Dividend Fund
        "DTD",  // WisdomTree U.S. Total Dividend Fund
        "DHS",  // WisdomTree U.S. High Dividend Fund
        "MOAT", // VanEck Morningstar Wide Moat ETF
]

// KR ETFs
def krEtfs = [
        //-----------------------------------------
        // growth
        //-----------------------------------------
        // my selection
        "472150",   // TIGER 배당커버드콜액티브
        "498400",   // KODEX 200타겟위클리커버드콜
        "496080",   // TIGER 코리아밸류업
        "441800",   // TIMEFOLIO Korea플러스배당액티브
        "161510",   // PLUS 고배당주
        "279530",   // KODEX 고배당주
        "0052D0",   // TIGER 코리아배당다우존스
        // growth
        "069500",   // KODEX 200
        "494890",   // KODEX 200액티브
        "451060",   // 1Q K200액티브
        "495230",   // KoAct 코리아밸류업액티브
        "385720",   // TIMEFOLIO 코스피액티브
        "495060",   // TIMEFOLIO 코리아밸류업액티브
        "325010",   // KODEX 성장주
        "0074K0",   // KoAct K수출핵심기업TOP30액티브
        "280920",   // PLUS 주도업종
        "226380",   // ACE Fn성장소비주도주
        "395760",   // PLUS ESG성장주액티브
        "373490",   // KODEX 코리아혁신성장액티브
        "444200",   // SOL 코리아메가테크액티브
        // dividend
        "104530",   // KIWOOM 고배당
        "266160",   // RISE 고배당
        "210780",   // TIGER 코스피고배당
        "322410",   // HANARO 고배당
        "211900",   // KODEX 배당성장
        "476850",   // KoAct 배당성장액티브
        "325020",   // KODEX 배당가치
        "251590",   // PLUS 고배당저변동50
        "447430",   // ACE 주주환원가치주액티브
        "494330",   // ACE 라이프자산주주가치액티브
]

/**
 * gets rank ETF items for the given market and ETF symbols
 * @param market
 * @param etfSymbols
 * @return
 */
def getRankEtfItems(market, etfSymbols) {
    List<Item> etfItems = []
    etfSymbols.each {
        etfItems.addAll(getEtfItems(market, it))
    }

    // 우선주,Class 등 본주로 치환
    Map<String, String> symbolAliasMap = [
            "GOOG": "GOOGL",    // Google Inc. Class C => Class A
            "005935": "005930", // 삼성전자 우선주 => 삼성전자
            "005385": "005380", // 현대자동차1우 => 현대자동차
            "005387": "005380"  // 현대자동차2우 => 현대자동차
    ]
    etfItems = etfItems.collect { item ->
        def unifiedSymbol = symbolAliasMap.get(item.symbol, item.symbol)
        item.symbol = unifiedSymbol
        item
    }

    // group by symbol
    etfItems = etfItems
            .groupBy { it.symbol }
            .collect { symbol, items ->
                new Item(
                        symbol: symbol,
                        name: items[0].name,
                        count: items.size(),
                        weight: items*.weight.findAll().average() ?: 0
                )
            }

    // filter stock
    etfItems = etfItems.findAll{
        def assetId = "${market}.${it.symbol}"
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

    // score
    etfItems = etfItems.collect { item ->
        // count score - 전체 대상 ETF 개수 대비 해당 종목을 포함한 ETF 개수로 계산
        def countScore = item.count / etfSymbols.size() * 100

        // 해당 종목에 포함된 ETF 에서 차지하는 보유비중 평균
        def weightScore = item.weight

        // score
        item.score = [
                countScore,
                weightScore
        ].average() as BigDecimal

        // remark
        item.remark = "Count: ${item.count}(${countScore}), Weight: ${item.weight}%(${weightScore}), Score: ${item.score}"

        // returns
        return item
    }

    // filter - 2개 이상 ETF에 포함된 종목
    etfItems = etfItems.findAll { it.count >= 2 }

    // sort by weight
    List<Item> topEtfItems = etfItems.sort { -(it.score ?: 0) }
    log.info("[${market}] Ranked ETF items: ${topEtfItems}")
    return topEtfItems
}

static Map<String, BigDecimal> calculateHoldingWeights(List<Item> items, BigDecimal totalHoldingWeight, BigDecimal maxHoldingWeight, BigDecimal minHoldingWeight, BigDecimal stepHoldingWeight) {
    Map<String, BigDecimal> result = new LinkedHashMap<>()
    for (int i = 0; i < items.size(); i++) {
        def item = items.get(i)
        def holdingWeight = maxHoldingWeight - (stepHoldingWeight * i)
        holdingWeight = holdingWeight.max(minHoldingWeight)
        result.put(item.symbol, holdingWeight)
    }

    for (int i = items.size() - 1; i >=0; i --) {
        def totalWeight = result.values().sum() as BigDecimal
        // check
        if (totalWeight <= totalHoldingWeight) {
            break;
        }
        // reduce
        def symbol = items.get(i).symbol
        result.put(symbol, minHoldingWeight)
        // check
        totalWeight = result.values().sum() as BigDecimal
        if (totalWeight <= totalHoldingWeight) {
            break;
        }
    }
    // returns
    return result
}

//=========================================
// collect all etf assets
//=========================================
def rankItems = []
switch (market) {
    case "US":
        rankItems = getRankEtfItems(market, usEtfs)
        break
    case "KR":
        rankItems = getRankEtfItems(market, krEtfs)
        break
    default:
        throw new RuntimeException("Unsupported market: ${market}")
}

// Top growth items
def topItems = rankItems.take(holdingCount)

// linear ranking weight 생성
def growthHoldingWeightMap = calculateHoldingWeights(topItems, totalHoldingWeight, maxHoldingWeight, minHoldingWeight, stepHoldingWeight)

//=========================================
// return
//=========================================
List<BasketRebalanceAsset> basketRebalanceAssets = []
topItems.forEach {
    BigDecimal holdingWeight = growthHoldingWeightMap[it.symbol]
    BasketRebalanceAsset asset = BasketRebalanceAsset.builder()
            .symbol(it.symbol)
            .name(it.name)
            .holdingWeight(holdingWeight)
            .remark("ETF - ${it.remark}")
            .build()
    basketRebalanceAssets.add(asset)
}

log.info("basketRebalanceAssets: ${basketRebalanceAssets}")
return basketRebalanceAssets

