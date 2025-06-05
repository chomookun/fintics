import groovy.json.JsonSlurper
import groovy.transform.ToString
import groovy.transform.builder.Builder
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
    String remark
    BigDecimal score
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
                    .remark(etfSymbol)
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
                    .remark(etfSymbol)
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
BigDecimal roeLimit = variables.roeLimit as BigDecimal
BigDecimal perLimit = variables.perLimit as BigDecimal
Integer maxAssetCount = variables.maxAssetCount as Integer
BigDecimal holdingWeightPerAsset = variables.holdingWeightPerAsset as BigDecimal
List<Item> candidateItems = []

//=======================================
// collect etf items
//=======================================
// ETF list
def etfSymbols = assetService.getAssets(AssetSearch.builder()
        .market(market)
        .type("ETF")
        .favorite(true)
        .build(), Pageable.unpaged())
        .getContent()
        .collect{it.getSymbol()};
etfSymbols.each{
    def etfItems = getEtfItems(market, it)
    println ("etfItems[${it}]: ${etfItems}")
    candidateItems.addAll(etfItems)
}

//========================================
// Favorite stocks
//========================================
def stockItems = assetService.getAssets(AssetSearch.builder()
        .market(market)
        .type("STOCK")
        .favorite(true)
        .build(), Pageable.unpaged())
        .getContent()
        .collect{
            Item.builder()
                    .symbol(it.getSymbol())
                    .name(it.getName())
                    .build()
        }
stockItems.each{
    println ("stockItems: ${it}")
    candidateItems.add(it)
}


//========================================
// distinct items
//========================================
candidateItems = candidateItems
        .groupBy { it.symbol }
        .collect { symbol, items ->
            def item = items[0]
            def remark = items*.remark.join(',')
            return Item.builder()
                .symbol(item.symbol)
                .name(item.name)
                .remark(remark)
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
    def assetId = "${market}.${it.symbol}"
    Asset asset = assetService.getAsset(assetId).orElse(null)
    if (asset == null) {
        return false
    }

    // STOCK 이 아니면 제외
    if (asset.getType() != "STOCK") {
        return false
    }

    //  ROE
    def roe = asset.getRoe() ?: 0.0
    if (roe < roeLimit) {    // ROE 가 금리 * 2 이하는 수익성 없는 회사로 제외
        return false
    }
    if (roe > 100) {        // ROE 가 100% 이상은 이상치 로 제외 (자사주 매입, 회계상 조정 등의 사유로 발생함)
        return false
    }

    // PER
    def per = asset.getPer() ?: 9999
    if (per > perLimit) {   // PER 가 perLimit 이상은 고 평가된 회사로 제외
        return false
    }
    if (per < 1) {      // PER 가 1 이하인 경우 회계상 이상 또는 다른 구조 적인 문제가 있는 경우 이므로 제외
        return false
    }

    // dividendYield 가 ROE 를 초과 하는 경우 자본 잠식 가능성 있음 으로 제외
    def dividendYield = asset.getDividendYield() ?: 0.0
    if (dividendYield >= roe) {
        return false
    }

    // score
    def score = BigDecimal.ZERO
    score += roe                // 기본 ROE
    score += dividendYield      // 배당률 에 가중치

    // score / PER 로 저평가 회사 우선
    it.score = (score / per).toBigDecimal().setScale(4, RoundingMode.HALF_UP)

    // adds remark
    it.remark = "ROE:${roe}, PER:${per}, dividendYield:${dividendYield}, etc:${it.remark}"

    // return
    return it
}
log.info("finalItems: ${finalItems}")

//=========================================
// sort by score
//=========================================
def fixedAssetCount = basket.getBasketAssets().findAll{it.enabled && it.fixed}.size()
def targetAssetCount = (maxAssetCount - fixedAssetCount) as Integer
finalItems = finalItems
        .sort{ -(it.score?:0)}
        .take(targetAssetCount)

//=========================================
// return
//=========================================
List<BasketRebalanceAsset> basketRebalanceResults = finalItems.collect{
    BasketRebalanceAsset.of(it.symbol, it.name, holdingWeightPerAsset, it.remark)
}
log.info("basketRebalanceResults: ${basketRebalanceResults}")
return basketRebalanceResults
