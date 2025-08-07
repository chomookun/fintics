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
        return []
    }
}


//=======================================
// defines
//=======================================
def market = variables.market
def growthHoldingCount = variables.growthHoldingCount as Integer
def growthHoldingWeight = variables.growthHoldingWeight as BigDecimal
def dividendHoldingCount = variables.dividendHoldingCount as Integer
def dividendHoldingWeight = variables.dividendHoldingWeight as BigDecimal

// US Growth ETFs
def usGrowthEtfs = [
        // my selection
        "JEPQ", // JPMorgan Nasdaq Equity Premium Income ETF
        "GPIQ", // Goldman Sachs Nasdaq-100 Premium Income ETF
        "QDVO", // Amplify ETF Trust Amplify CWP Growth & Income ETF
        // growth
        "QQQ",  // Invesco QQQ Trust
        "SPY",  // SPDR S&P 500 ETF Trust
        "VUG",  // Vanguard Growth ETF
        "IUSG", // iShares Core S&P U.S. Growth ETF
        "IVW",  // iShares S&P 500 Growth ETF
        "XLK",  // Technology Select Sector SPDR Fund
        "IYW",  // iShares U.S. Technology ETF
        "VGT",  // Vanguard Information Tech ETF
        // sector
        "SMH",  // VanEck Vectors Semiconductor ETF
        "SOXX", // iShares PHLX Semiconductor ETF
        "ARTY", // iShares Future AI & Tech ETF
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

// KR Growth ETFs
def krGrowthEtfs = [
        // my selection
        "472150",   // TIGER 배당커버드콜액티브
        "498400",   // KODEX 200타겟위클리커버드콜
        "496080",   // TIGER 코리아밸류업
        // growth
        "069500",   // KODEX 200
        "494890",   // KODEX 200액티브
        "451060",   // 1Q K200액티브
        "495230",   // KoAct 코리아밸류업액티브
        "495060",   // TIMEFOLIO 코리아밸류업액티브
        "325010",   // KODEX 성장주
        "0074K0",   // KoAct K수출핵심기업TOP30액티브
        "280920",   // PLUS 주도업종
        "226380",   // ACE Fn성장소비주도주
        "395760",   // PLUS ESG성장주액티브
        "373490",   // KODEX 코리아혁신성장액티브
        // sector
        "455850",   // SOL AI반도체소부장
        "395160",   // KODEX AI반도체
        "396500",   // TIGER Fn반도체TOP10
]

// KR dividend ETFs
def krDividendEtfs = [
        // my selection
        "441800",   // TIMEFOLIO Korea플러스배당액티브
        "161510",   // PLUS 고배당주
        "0018C0",   // PLUS 고배당주위클리고정커버드콜
        "0052D0",   // TIGER 코리아배당다우존스
        // dividend
        "279530",   // KODEX 고배당주
        "104530",   // KIWOOM 고배당
        "266160",   // RISE 고배당
        "210780",   // TIGER 코스피고배당
        "322410",   // HANARO 고배당
        "211900",   // KODEX 배당성장
        "476850",   // KoAct 배당성장액티브
        "251590",   // PLUS 고배당저변동50
        // 한국 시장에는 지배구조 문제(예시:LG/GS 계열사)로 주주환원 테마 ETF 추가
        "447430",   // ACE 주주환원가치주액티브
        "494330",   // ACE 라이프자산주주가치액티브
        // sector
        "139280",   // TIGER 경기방어
        "266410",   // KODEX 필수소비재
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

        // Asset 정보 조회
        def assetId = "${market}.${item.symbol}"
        Asset asset = assetService.getAsset(assetId).orElse(null)
        def roe = asset.getRoe() ?: 0.0
        def per = asset.getPer() ?: 9999
        def dividendYield = asset.getDividendYield() ?: 0.0

        // 가격 score
        def totalYield = BigDecimal.ZERO
        totalYield += roe                            // 기본 ROE
        totalYield += (dividendYield * 0.5)         // 배당률 가중치 (50%만 적용)
        def valuationScore = BigDecimal.ZERO
        valuationScore = totalYield / per
        valuationScore = Math.min((valuationScore / 2.0).doubleValue(), 1.0) * 100
        valuationScore = valuationScore * 0.33   // 100점 만점은 너무 과도하게 반영됨으로 score factor 적용해서 반영(현재 factor 3임으로 0.33 적용)

        // score
        item.score = [
                countScore,
                weightScore,
                valuationScore
        ].average() as BigDecimal

        // remark
        item.remark = "Count: ${item.count}(${countScore}), Weight: ${item.weight}%(${weightScore}), Price: ${valuationScore}, Score: ${item.score}"

        // returns
        return item
    }

    // sort by weight
    List<Item> topEtfItems = etfItems.sort { -(it.score ?: 0) }
    return topEtfItems
}

//=========================================
// collect all etf assets
//=========================================
def rankGrowthItems = []
def rankDividendItems = []
switch (market) {
    case "US":
        rankGrowthItems = getRankEtfItems(market, usGrowthEtfs)
        rankDividendItems = getRankEtfItems(market, usDividendEtfs)
        break
    case "KR":
        rankGrowthItems = getRankEtfItems(market, krGrowthEtfs)
        rankDividendItems = getRankEtfItems(market, krDividendEtfs)
        break
    default:
        throw new RuntimeException("Unsupported market: ${market}")
}

// Top growth items
def topGrowthItems = rankGrowthItems.take(growthHoldingCount)

// Top dividend items
rankDividendItems.removeIf(dividendItem -> topGrowthItems.any {it.symbol == dividendItem.symbol})
def topDividendItems = rankDividendItems.take(dividendHoldingCount)

//=========================================
// return
//=========================================
List<BasketRebalanceAsset> basketRebalanceAssets = []
topGrowthItems.forEach {
    BasketRebalanceAsset asset = BasketRebalanceAsset.builder()
            .symbol(it.symbol)
            .name(it.name)
            .holdingWeight(growthHoldingWeight)
            .remark("Growth ETF - ${it.remark}")
            .build()
    basketRebalanceAssets.add(asset)
}
topDividendItems.forEach {
    BasketRebalanceAsset asset = BasketRebalanceAsset.builder()
            .symbol(it.symbol)
            .name(it.name)
            .holdingWeight(dividendHoldingWeight)
            .remark("Dividend ETF - ${it.remark}")
            .build()
    basketRebalanceAssets.add(asset)
}

log.info("basketRebalanceAssets: ${basketRebalanceAssets}")
return basketRebalanceAssets

