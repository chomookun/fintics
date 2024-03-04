import groovy.transform.ToString
import org.hibernate.annotations.common.reflection.XMember
import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.BalanceAsset
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.trade.Tool

import java.time.LocalTime

class Score extends LinkedHashMap<String, BigDecimal> {
    def getAverage() {
        return this.collect{it.value}.average()
    }
}

class Analysis {
    List<Ohlcv> ohlcvs
    List<Ema> fastEmas
    List<Ema> slowEmas
    List<Bb> bbs
    List<Macd> macds
    List<Rsi> rsis
    List<Dmi> dmis
    List<Obv> obvs
    List<Co> cos

    Analysis(List<Ohlcv> ohlcvs) {
        this.ohlcvs = ohlcvs
        this.fastEmas = Tool.calculate(ohlcvs, EmaContext.of(10))
        this.slowEmas = Tool.calculate(ohlcvs, EmaContext.of(20))
        this.bbs = Tool.calculate(ohlcvs, BbContext.DEFAULT)
        this.macds = Tool.calculate(ohlcvs, MacdContext.DEFAULT)
        this.rsis = Tool.calculate(ohlcvs, RsiContext.DEFAULT)
        this.dmis = Tool.calculate(ohlcvs, DmiContext.DEFAULT)
        this.obvs = Tool.calculate(ohlcvs, ObvContext.DEFAULT)
        this.cos = Tool.calculate(ohlcvs, CoContext.DEFAULT)
    }

    def getMomentumScore() {
        def score = new Score()
        // ema
        def fastEma = this.fastEmas.first()
        def slowEma = this.slowEmas.first()
        score.emaValueFastOverSlow = fastEma.value > slowEma.value ? 100 : 0
        // macd
        def macd = this.macds.first()
        score.macdValue = macd.value > 0 ? 100 : 0
        score.macdSignal = macd.signal > 0 ? 100 : 0
        score.macdValueOverSignal = macd.value > 0 && macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
        // rsi
        def rsi = this.rsis.first()
        score.rsiValue = rsi.value > 50 ? 100 : 0
        score.rsiValueOverSignal = rsi.value > 50 && rsi.value > rsi.signal ? 100 : 0
        // return
        return score
    }

    def getOverboughtScore() {
        def score = new Score()
        // rsi
        def rsi = this.rsis.first()
        score.rsiValue = rsi.value >= 70 ? 100 : 0
        // bb
        def bb = this.bbs.first()
//        score.bbPercentB = bb.percentB >= 100 ? 100 : 0
        // return
        return score
    }

    def getOversoldScore() {
        def score = new Score()
        // rsi
        def rsi = this.rsis.first()
        score.rsiValue = rsi.value <= 30 ? 100 : 0
        // bb
        def bb = this.bbs.first()
//        score.bbPercentB = bb.percentB <= 0 ? 100 : 0
        // return
        return score
    }
}

//==========================
// defines
//==========================
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = "${assetIndicator.getAssetName()}(${assetId})"

// analysis
def fastAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1))
def slowAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 5))
def dailyAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY, 1))

// range of price change
def minuteOhlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def dailyOhlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.DAILY, 1)
def pricePctChange = Tool.pctChange([
        minuteOhlcvs.first().closePrice,
        dailyOhlcvs.first().openPrice
])
def highPricePctChangeAverage = dailyOhlcvs.take(14)
        .collect{(it.highPrice-it.openPrice)/it.closePrice*100}
        .average()
def lowPricePctChangeAverage = dailyOhlcvs.take(14)
        .collect{(it.lowPrice-it.openPrice)/it.closePrice*100}
        .average()
log.info("[{}] pricePctChange: {}", assetName, pricePctChange)
log.info("[{}] highPricePctChangeAverage: {}", assetName, highPricePctChangeAverage)
log.info("[{}] lowPricePctChangeAverage: {}", assetName, lowPricePctChangeAverage)

//==============================================
// 단기 상승 시작 시
//==============================================
if (fastAnalysis.getMomentumScore().getAverage() > 70) {
    // 평균 하락 폭 보다 더 하락한 경우 매수
    if (pricePctChange < lowPricePctChangeAverage) {
        hold = 1
    }
    // 중기 과매도 구간 이면 매수
    if (slowAnalysis.getOversoldScore().getAverage() > 50) {
        hold = 1
    }
}

//===============================================
// 단기 하락 시작 시
//===============================================
if (fastAnalysis.getMomentumScore().getAverage() < 30) {
    // 평균 상승 폭 더 상승한 경우 경우 매도
    if (pricePctChange > highPricePctChangeAverage) {
        hold = 0
    }
    // 중기 과매수 구간 이면 매도
    if (slowAnalysis.getOverboughtScore().getAverage() > 50) {
        hold = 0
    }
}

//==============================================
// fallback - daily momentum 미달인 경우 모두 매도
//==============================================
if (dailyAnalysis.getMomentumScore().getAverage() < 50) {
    log.info("[{}] fallback - daily momentum is under 50", assetName)
    hold = 0
}

//==============================================
// return
//==============================================
return hold

