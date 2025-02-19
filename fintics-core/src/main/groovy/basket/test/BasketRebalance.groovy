import groovy.json.JsonSlurper
import org.chomookun.fintics.client.ohlcv.OhlcvClient
import org.chomookun.fintics.indicator.Ema
import org.chomookun.fintics.indicator.EmaContext
import org.chomookun.fintics.indicator.Macd
import org.chomookun.fintics.indicator.MacdContext
import org.chomookun.fintics.model.Asset
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.trade.Tools
import org.chomookun.fintics.basket.BasketRebalanceAsset
import org.oopscraft.fintics.model.*
import org.oopscraft.fintics.indicator.*

import java.time.LocalDateTime

import static java.time.LocalDateTime.*

class Item {
    String symbol
    String name
    @Override
    String toString() {
        return "Item{symbol=${this.symbol},name=${this.name}"
    }
    static Item of(symbol, name) {
        Item item = new Item()
        item.symbol = symbol
        item.name = name
        return item
    }
}

class ItemScore {
    Item item
    BigDecimal score
    String toString() {
        return "ItemScore{item=${this.item},score=${this.score}}"
    }
    static ItemScore of(item, score) {
        def itemScore = new ItemScore()
        itemScore.item = item
        itemScore.score = score
        return itemScore
    }
}

def getChartScore(item) {
    def score = [:]
    // get asset
    def assetId = "${basket.getMarket()}.${item.symbol}"
    Asset asset = assetService.getAsset(assetId).orElse(null);
    if (asset == null) {
        return 0.0
    }
    // get ohlcvs
    def dateTimeFrom = LocalDateTime.now().minusMonths(3)
    def dateTimeTo = LocalDateTime.now()
    List<Ohlcv> ohlcvs = ohlcvClient.getOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo)
    def ohlcv = ohlcvs.first()
    // ema50
    List<Ema> ema50s = Tools.indicators(ohlcvs, EmaContext.of(50))
    def ema50 = ema50s.first()
    score.ema50PriceOverValue = ohlcv.close > ema50.value ? 100 : 0
    // macd
    List<Macd> macds = Tools.indicators(ohlcvs, MacdContext.DEFAULT)
    def macd = macds.first()
    score.macdValue = macd.value > 0 ? 100 : 0
    // return
    return score.values().average()
}

/**
 * get symbol from fnguid
 * @param url
 * @return
 * @see https://comp.fnguide.com/SVO/WooriRenewal/new_overview.asp
 */
static List<Item> getFnguideItems(String url) {
    def response = new URL(url).getText()
    if (response.charAt(0) == '\uFEFF') {
        response = response.substring(1)
    }
    def responseJson = new JsonSlurper().parseText(response)
    def compArray = responseJson.comp as List<Map>
    return compArray.collect{Item.of(it.GICODE.replace('A',''), it.ITEMABBRNM)}
}

def itemScores = new ArrayList<ItemScore>();

// FNGUID 성장성 - 고ROE
def fnguidHighRoeItems = getFnguideItems("https://comp.fnguide.com/SVO2/json/data/NH/HIGH_ROE.json")
println("fnguidHighRoeItems: ${fnguidHighRoeItems}")
itemScores.addAll(fnguidHighRoeItems.collect{ItemScore.of(it,1.0)})

// FNGUID 성장성 - 성장률
def fnguidGrowthSalesItems = getFnguideItems("https://comp.fnguide.com/SVO2/json/data/NH/GROWHT_SALES.json")
println("fnguidGrowthSalesItems: ${fnguidGrowthSalesItems}")
itemScores.addAll(fnguidGrowthSalesItems.collect{ItemScore.of(it, 1.0)})

// FNGUID 성장성 - 이익수정비율
def fnguidRateEpsItems = getFnguideItems("https://comp.fnguide.com/SVO2/json/data/NH/RATE_EPS.json")
println("fnguidRateEpsItems: ${fnguidRateEpsItems}")
itemScores.addAll(fnguidRateEpsItems.collect{ItemScore.of(it, 1.0)})

// FNGUID 수급 - 외국인/기관 동반 순매수
def fnguidTrendWithBuyItems = getFnguideItems("https://comp.fnguide.com/SVO2/json/data/NH/SUPPLY_TREND_WITH_BUY.json")
println("fnguidTrendWithBuyItems: ${fnguidTrendWithBuyItems}")
itemScores.addAll(fnguidTrendWithBuyItems.collect{ItemScore.of(it, 1.0)})

// FNGUID 수급 - 외국인/기관 동반 순매수 전환
def fnguidTrendFirstBuyItems = getFnguideItems("https://comp.fnguide.com/SVO2/json/data/NH/SUPPLY_TREND_FIRST_BUY.json")
println("fnguidTrendFirstBuyItems: ${fnguidTrendFirstBuyItems}")
itemScores.addAll(fnguidTrendFirstBuyItems.collect{ItemScore.of(it, 1.0)})

// FNGUID 수급 - 연속순매수 (외국인)
def fnguidContinuousBuyFrg3Items = getFnguideItems("https://comp.fnguide.com/SVO2/json/data/NH/CONTINUOUS_BUY_FRG_3.json")
println("fnguidContinuousBuyFrg3Items: ${fnguidContinuousBuyFrg3Items}")
itemScores.addAll(fnguidContinuousBuyFrg3Items.collect{ItemScore.of(it, 1.0)})

// FNGUID 수급 - 연속순매수 (기관)
def fnguidContinuousBuyOrg3Items = getFnguideItems("https://comp.fnguide.com/SVO2/json/data/NH/CONTINUOUS_BUY_ORG_3.json")
println("fnguidContinuousBuyOrg3Items: ${fnguidContinuousBuyOrg3Items}")
itemScores.addAll(fnguidContinuousBuyOrg3Items.collect{ItemScore.of(it, 1.0)})

// FNGUID 수급 - 연속순매수 (외국인/기관 동반)
def fnguidContinuousBuyWith3Items = getFnguideItems("https://comp.fnguide.com/SVO2/json/data/NH/CONTINUOUS_BUY_WITH_3.json")
println("fnguidContinuousBuyWith3Items: ${fnguidContinuousBuyWith3Items}")
itemScores.addAll(fnguidContinuousBuyWith3Items.collect{ItemScore.of(it, 1.0)})

// chart score
itemScores = itemScores.findAll{getChartScore(it.item) > 80}

// group by sum
def aggregatedItemScore = itemScores
        .groupBy {it.item}
        .collect { item, scores ->
            ItemScore.of(item, scores.sum { it.score })}
        .sort { -it.score }
println("aggregatedItemScore: ${aggregatedItemScore}")

// top10 item
def top10ItemScores = aggregatedItemScore
        .sort { -it.score }
        .take(30)
println("top10ItemScores: ${top10ItemScores}")

// return
List<BasketRebalanceAsset> basketChanges = top10ItemScores.collect{
    BasketRebalanceAsset.of(it.item.symbol, "", 2.0)
}
println("basketChanges: ${basketChanges}")
return basketChanges
