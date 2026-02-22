import groovy.json.JsonSlurper
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.chomookun.fintics.core.asset.model.AssetSearch
import org.chomookun.fintics.core.basket.rebalance.BasketRebalanceAsset
import org.springframework.data.domain.Pageable

import java.math.RoundingMode

/**
 * item
 */
@Builder
@ToString
class Item {
    String symbol
    String name
    Set<String> etfs
    Integer count
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
        def connection = url.openConnection()
        def responseJson = connection.inputStream.text
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
        def connection = url.openConnection()
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
        def responseJson
        try {
            responseJson = connection.inputStream.text
        } catch (Exception e) {
            return []
        }
        def jsonSlurper = new JsonSlurper()
        def responseMap = jsonSlurper.parseText(responseJson)
        def top10Holdings = responseMap.get('etfTop10MajorConstituentAssets')
        // 결과 값이 이상할 경우(10개 이하인 경우) 에러 처리
        if (top10Holdings == null) {
            return []
        }
        top10Holdings = top10Holdings.collect {
            def weightStr = (it.etfWeight ?: "").toString()
                    .replace("%", "")
                    .trim()

            // 숫자 없는 케이스 방어
            def weightVal = (weightStr.isNumber() ? new BigDecimal(weightStr) : BigDecimal.ZERO)

            Item.builder()
                    .symbol(it.itemCode as String)
                    .name(it.itemName as String)
                    .weight(weightVal)
                    .build()
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
def market = variables.market as String
def totalHoldingWeight = variables.totalHoldingWeight as BigDecimal
def holdingCount = variables.holdingCount as Integer
def maxHoldingWeight = variables.maxHoldingWeight as BigDecimal
def minHoldingWeight = variables.minHoldingWeight as BigDecimal
def stepHoldingWeight = variables.stepHoldingWeight as BigDecimal
def favoriteEnabled = variables.favoriteEnabled?.toString()?.toBoolean() ?: false
def favoriteCount = variables.favoriteCount as Integer

// US ETFs
def usEtfs = [
        // growth
        "SPUS", // (*)SP Funds S&P 500 Sharia Industry Exclusions ETF
        "JEPQ", // (*)JPMorgan Nasdaq Equity Premium Income ETF
        "GPIQ", // (*)Goldman Sachs Nasdaq-100 Premium Income ETF
        "QDVO", // (*)Amplify ETF Trust Amplify CWP Growth & Income ETF
        "SPTE", // (*)SP Funds Trust SP Funds S&P Global Technology ETF
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
        "XT",   // iShares Exponential Technologies ETF
        // dividend
        "DGRW", // (*)WisdomTree U.S. Quality Dividend Growth Fund
        "DIVO", // (*)Amplify CWP Enhanced Dividend Income ETF
        "IDVO", // (*)Amplify CWP International Enhanced Dividend Income ETF
        "BALI", // (*)iShares Advantage Large Cap Income ETF
        "DLN",  // (*)WisdomTree U.S. LargeCap Dividend Fund
        "DGRO", // iShares Core Dividend Growth ETF
        "SDY",  // SPDR S&P Dividend ETF
        "DVY",  // iShares Select Dividend ETF
        "RDVY", // First Trust Rising Dividend Achievers ETF
        "VYM",  // Vanguard High Dividend Yield ETF
        "FDVV", // Fidelity High Dividend ETF
        "SPYD", // State Street SPDR Portfolio S&P 500 High Dividend ETF
        "HDV",  // iShares Core High Dividend ETF
        "DHS",  // WisdomTree U.S. High Dividend Fund
        "SCHD", // Schwab U.S. Dividend Equity ETF
        "FDRR", // Fidelity Dividend ETF for Rising Rates
        "DTD",  // WisdomTree U.S. Total Dividend Fund
        "JEPI", // JPMorgan Equity Premium Income ETF
        "MOAT", // VanEck Morningstar Wide Moat ETF
]

// KR ETFs
def krEtfs = [
        // growth
        "472150",   // (*)TIGER 배당커버드콜액티브
        "498400",   // (*)KODEX 200타겟위클리커버드콜
        "496080",   // (*)TIGER 코리아밸류업
        "069500",   // KODEX 200
        "494890",   // KODEX 200액티브
        "122090",   // PLUS 코스피50
        "237350",   // KODEX 코스피100
        "451060",   // 1Q K200액티브
        "310970",   // TIGER MSCI Korea TR
        "495230",   // KoAct 코리아밸류업액티브
        "495060",   // TIMEFOLIO 코리아밸류업액티브
        "292150",   // TIGER 코리아TOP10
        "315930",   // KODEX Top5PlusTR
        "325010",   // KODEX 성장주
        "0074K0",   // KoAct K수출핵심기업TOP30액티브
        "444200",   // SOL 코리아메가테크액티브
        // dividend
        "441800",   // (*)TIMEFOLIO Korea플러스배당액티브
        "161510",   // (*)PLUS 고배당주
        "0052D0",   // (*)TIGER 코리아배당다우존스
        "279530",   // KODEX 고배당주
        "315960",   // RISE 대형고배당10TR
        "104530",   // KIWOOM 고배당
        "266160",   // RISE 고배당
        "210780",   // TIGER 코스피고배당
        "476850",   // KoAct 배당성장액티브
        "211900",   // KODEX 코리아배당성장
        "325020",   // KODEX 배당가치
        "494330",   // ACE 라이프자산주주가치액티브
]

// 데이터베이스에 등록된 즐겨찾기 ETF 심볼 추가
if (favoriteEnabled) {
    List<Asset> favoriteEtfs = assetService.getAssets(AssetSearch.builder()
            .market(market)
            .type("ETF")
            .favorite(true)
            .build(), Pageable.unpaged())
            .getContent()
            .take(favoriteCount)
    log.info("Favorite ETF for market ${market}:")
    favoriteEtfs.each {
        log.info("[${it.symbol}] ${it.name}")
    }
    List<String> favoriteEtfSymbols = favoriteEtfs.collect{it.symbol}
    switch (market) {
        case "US":
            usEtfs.addAll(favoriteEtfSymbols)
            usEtfs.unique()
            break
        case "KR":
            krEtfs.addAll(favoriteEtfSymbols)
            krEtfs.unique()
            break
        default:
            throw new RuntimeException("Unsupported market: ${market}")
    }
}

/**
 * gets rank ETF items for the given market and ETF symbols
 * @param market
 * @param etfSymbols
 * @return
 */
def getRankEtfItems(market, etfSymbols) {
    List<Item> etfItems = []
    etfSymbols.each { etfSymbol ->
        def items = getEtfItems(market, etfSymbol)
        items.each { item ->
            if (item.etfs == null) item.etfs = new LinkedHashSet<>()
            item.etfs.add(etfSymbol)
        }
        etfItems.addAll(items)
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
                def weightSum = items*.weight.findAll().sum() ?: 0
                def mergedEtfs = new LinkedHashSet<>(items.collectMany { it.etfs ?: [] })
                new Item(
                        symbol: symbol,
                        name: items[0].name,
                        etfs: mergedEtfs as Set<String>,
                        count: mergedEtfs.size(),
                        weight: weightSum as BigDecimal
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
        // score
        item.score = item.weight
        // remark
        item.remark = "etfs:[${item.etfs.join(',')}], count:${item.count}, weight:${item.weight}%, Score:${item.score}"
        // returns
        return item
    }

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

