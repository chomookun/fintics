import groovy.transform.ToString
import org.oopscraft.fintics.indicator.*
import org.chomoo.fintics.indicator.BollingerBand
import org.chomoo.fintics.indicator.BollingerBandContext
import org.chomoo.fintics.indicator.ChaikinOscillator
import org.chomoo.fintics.indicator.ChaikinOscillatorContext
import org.chomoo.fintics.indicator.Dmi
import org.chomoo.fintics.indicator.DmiContext
import org.chomoo.fintics.indicator.Ema
import org.chomoo.fintics.indicator.EmaContext
import org.chomoo.fintics.indicator.Macd
import org.chomoo.fintics.indicator.MacdContext
import org.chomoo.fintics.indicator.Obv
import org.chomoo.fintics.indicator.ObvContext
import org.chomoo.fintics.indicator.Rsi
import org.chomoo.fintics.indicator.RsiContext
import org.chomoo.fintics.model.Ohlcv
import org.chomoo.fintics.model.BalanceAsset
import org.chomoo.fintics.model.Ohlcv
import org.chomoo.fintics.model.Ohlcv
import org.chomoo.fintics.model.Ohlcv

import java.time.LocalTime

class Score extends LinkedHashMap<String, BigDecimal> {
    def getAverage() {
        return this.collect{it.value}.average()
    }
}

@ToString(includeNames = true)
class Analysis {
    List<Ohlcv> ohlcvs
    List<Ema> fastEmas
    List<Ema> slowEmas
    List<BollingerBand> bbs
    List<Macd> macds
    List<Rsi> rsis
    List<Dmi> dmis
    List<Obv> obvs
    List<ChaikinOscillator> cos

    Analysis(List<Ohlcv> ohlcvs) {
        this.ohlcvs = ohlcvs
        this.fastEmas = tool.calculate(ohlcvs, EmaContext.of(5))
        this.slowEmas = tool.calculate(ohlcvs, EmaContext.of(10))
        this.bbs = tool.calculate(ohlcvs, BollingerBandContext.DEFAULT)
        this.macds = tool.calculate(ohlcvs, MacdContext.DEFAULT)
        this.rsis = tool.calculate(ohlcvs, RsiContext.DEFAULT)
        this.dmis = tool.calculate(ohlcvs, DmiContext.DEFAULT)
        this.obvs = tool.calculate(ohlcvs, ObvContext.DEFAULT)
        this.cos = tool.calculate(ohlcvs, ChaikinOscillatorContext.DEFAULT)
    }

    def getMomentumScore() {
        def score = new Score()
        // macd
        def macd = this.macds.first()
        score.macdValue = macd.value > 0 ? 100 : 0
        score.macdSignal = macd.signal > 0 ? 100 : 0
        score.macdValueOverSignal = macd.value > 0 && macd.value > macd.signal ? 100 : 0
        // rsi
        def rsi = this.rsis.first()
        score.rsiValue = rsi.value > 50 ? 100 : 0
        score.rsiValueOverSignal = rsi.value > 50 && rsi.value > rsi.signal ? 100 : 0
        // return
        return score
    }

    def getDirectionScore() {
        def score = new Score()
        // ema
        def fastEma = this.fastEmas.first()
        def slowEma = this.slowEmas.first()
        score.emaValueFastOverSlow = fastEma.value > slowEma.value ? 100 : 0
        // return
        return score
    }

    def isOverboughtScore() {
        def score = new Score()
        // rsi
        def rsi = this.rsis.first()
        score.rsiValueOver70 = rsi.value > 70 ? 100 : 0
        // return
        return score
    }

    def isOversoldScore() {
        def score = new Score()
        // rsi
        def rsi = this.rsis.first()
        score.rsiValueUnder30 = rsi.value < 30 ? 100 : 0
        // return
        return score
    }
}




// defines
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = "${assetIndicator.getAssetName()}(${assetId})"

// ohlcv
List<Ohlcv> ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def prices = ohlcvs.collect{it.getClose}
def price = prices.first()
def priceZScore = tool.zScore(prices.take(zScorePeriod))
log.info("[{}] priceZScore: {}", assetName, priceZScore)
if (priceZScore.abs() < 1.0) {
    return null
}

// analysis
def analysis = new Analysis(ohlcvs)

if (analysis.getDirectionScore()) {

}







// ma
List<Ema> fastMas = tool.calculate(ohlcvs, EmaContext.of(fastMaPeriod))
def fastMa = fastMas.first()
def fastMaValues = fastMas.take(fastMaPeriod).collect{it.value}
List<Ema> slowMas = tool.calculate(ohlcvs, EmaContext.of(slowMaPeriod))
def slowMaValues = slowMas.take(slowMaPeriod).collect{it.value}
def slowMa = slowMas.first()

// default buy
if (fastMa.value > slowMa.value) {
    hold = 1
}

// default sell
if (fastMa.value < slowMa.value) {
    hold = 0
}

// analyze asset
def assetAnalysises = [
        hourly: getAnalysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 60)),
        daily: getAnalysis(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY,1))
]
def assetScores = [
        hourly: getScore(assetAnalysises.hourly),
        daily: getScore(assetAnalysises.daily)
]

// analysis indice
def indiceAnalysises = [
//        kospi: getAnalysis(indiceIndicators['KOSPI'].getOhlcvs(Ohlcv.Type.DAILY, 1)),
//        ndxFuture: getAnalysis(indiceIndicators['NDX_FUTURE'].getOhlcvs(Ohlcv.Type.DAILY, 1))
]
def indiceScores = [
//        kospi: getScore(indiceAnalysises.kospi),
//        ndxFuture: getScore(indiceAnalysises.ndxFuture)
]

// total score average
def totalScores = [
        assetScores.hourly.getAverage(),
        assetScores.daily.getAverage(),
//        indiceScores.kospi.getAverage()
]
def totalScoreAverage = totalScores.average()
if (totalScoreAverage < 50) {
    hold = 0
}

// return
return hold

