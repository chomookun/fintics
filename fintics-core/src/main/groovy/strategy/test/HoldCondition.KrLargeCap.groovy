import groovy.transform.ToString
import org.oopscraft.fintics.indicator.*
import org.chomookun.fintics.indicator.BollingerBand
import org.chomookun.fintics.indicator.BollingerBandContext
import org.chomookun.fintics.indicator.ChaikinOscillator
import org.chomookun.fintics.indicator.ChaikinOscillatorContext
import org.chomookun.fintics.indicator.Dmi
import org.chomookun.fintics.indicator.DmiContext
import org.chomookun.fintics.indicator.Ema
import org.chomookun.fintics.indicator.EmaContext
import org.chomookun.fintics.indicator.Macd
import org.chomookun.fintics.indicator.MacdContext
import org.chomookun.fintics.indicator.Obv
import org.chomookun.fintics.indicator.ObvContext
import org.chomookun.fintics.indicator.Rsi
import org.chomookun.fintics.indicator.RsiContext
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.BalanceAsset
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv

import java.time.LocalTime

@ToString(includeNames = true)
class Analysis {
    List<Ohlcv> ohlcvs
    List<Ema> emas
    List<BollingerBand> bbs
    List<Macd> macds
    List<Rsi> rsis
    List<Dmi> dmis
    List<Obv> obvs
    List<ChaikinOscillator> cos
}

def getAnalysis(List<Ohlcv> ohlcvs) {
    def analysis = new Analysis()
    analysis.ohlcvs = ohlcvs
    analysis.emas = tool.calculate(ohlcvs, EmaContext.DEFAULT)
    analysis.bbs = tool.calculate(ohlcvs, BollingerBandContext.DEFAULT)
    analysis.macds = tool.calculate(ohlcvs, MacdContext.DEFAULT)
    analysis.rsis = tool.calculate(ohlcvs, RsiContext.DEFAULT)
    analysis.dmis = tool.calculate(ohlcvs, DmiContext.DEFAULT)
    analysis.obvs = tool.calculate(ohlcvs, ObvContext.DEFAULT)
    analysis.cos = tool.calculate(ohlcvs, ChaikinOscillatorContext.DEFAULT)
    return analysis
}

@ToString(includeNames = true)
class Score {
    def pricePctChange
    def emaValuePctChange
    def emaPriceOverValue
    def bbMbbPctChange
    def bbPriceOverMbb
    def macdValue
    def macdValuePctChange
    def macdValueOverSignal
    def macdOscillator
    def macdOscillatorPctChange
    def rsiValue
    def rsiValuePctChange
    def rsiValueOverSignal
    def dmiPdiPctChange
    def dmiMdiPctChange
    def dmiPdiOverMdi
    def dmiAdxPctChange
    def obvValuePctChange
    def obvValueOverSignal
    def coValue
    def coValuePctChange
    def coValueOverSignal

    def getAverage() {
        return this.properties.values()
                .findAll{it instanceof Number}
                .average()
    }
}

def getScore(Analysis analysis) {
    def score = new Score()
    def period = 10

    // ohlcv
    def ohlcvs = analysis.ohlcvs.take(period)
    def ohlcv = ohlcvs.first()
    def prices = ohlcvs.collect{it.getClose}
    def pricePctChange =  tool.pctChange(prices)
    score.pricePctChange = pricePctChange > 0 ? 100 : 0

    // ema
    def emas = analysis.emas.take(period)
    def ema = emas.first()
    def emaValues = emas.collect{it.value}
    def emaValuePctChange = tool.pctChange(emaValues)
    score.emaPriceOverValue = ohlcv.getClose > ema.value ? 100 : 0
    score.emaValuePctChange = emaValuePctChange > 0 ? 100 : 0

    // bb
    def bbs = analysis.bbs.take(period)
    def bb = bbs.first()
    def bbMbbs = bbs.collect{it.getMiddle}
    def bbMbbPctChange = tool.pctChange(bbMbbs)
    score.bbMbbPctChange = bbMbbPctChange > 0 ? 100 : 0
    score.bbPriceOverMbb = ohlcv.getClose > bb.getMiddle ? 100 : 0

    // macd
    def macds = analysis.macds.take(period)
    def macdValues = macds.collect{it.value}
    def macdValue = macdValues.first()
    def macdSignals = macds.collect{it.signal}
    def macdSignal = macdSignals.first()
    def macdOscillators = macds.collect{it.oscillator}
    def macdOscillator = macdOscillators.first()
    def macdValuePctChange = tool.pctChange(macdValues)
    def macdOscillatorPctChange = tool.pctChange(macdOscillators)
    score.macdValue = macdValue > 0 ? 100 : 0
    score.macdValuePctChange = macdValuePctChange > 0 ? 100 : 0
    score.macdValueOverSignal = macdValue > macdSignal ? 100 : 0
    score.macdOscillator = macdOscillator > 0 ? 100 : 0
    score.macdOscillatorPctChange = macdOscillatorPctChange > 0 ? 100 : 0

    // rsi
    def rsis = analysis.rsis.take(period)
    def rsiValues = rsis.collect{it.value}
    def rsiValue = rsiValues.first()
    def rsiSignals = rsis.collect{it.signal}
    def rsiSignal = rsiSignals.first()
    score.rsiValue = rsiValue > 50 ? 100 : 0
    score.rsiValuePctChange = tool.pctChange(rsiValues)
    score.rsiValueOverSignal = rsiValue > rsiSignal ? 100 : 0

    // dmi
    def dmis = analysis.dmis.take(period)
    def dmiPdis = dmis.collect{it.pdi}
    def dmiPdi = dmiPdis.first()
    def dmiPdiPctChange = tool.pctChange(dmiPdis)
    def dmiMdis = dmis.collect{it.mdi}
    def dmiMdi = dmiMdis.first()
    def dmiMdiPctChange = tool.pctChange(dmiMdis)
    def dmiAdxs = dmis.collect{it.adx}
    def dmiAdxPctChange = tool.pctChange(dmiAdxs)
    score.dmiPdiPctChange = dmiPdiPctChange > 0 ? 100 : 0
    score.dmiMdiPctChange = dmiMdiPctChange < 0 ? 100 : 0
    score.dmiPdiOverMdi = dmiPdi > dmiMdi ? 100 : 0
    score.dmiAdxPctChange = dmiPdiPctChange > 0 && dmiAdxPctChange > 0 ? 100 : 0

    // co
    def obvs = analysis.obvs.take(period)
    def obvValues = obvs.collect{it.value}
    def obvValue = obvValues.first()
    def obvSignals = obvs.collect{it.signal}
    def obvSignal = obvSignals.first()
    def obvValuePctChange = tool.pctChange(obvValues)
    score.obvValuePctChange = obvValuePctChange > 0 ? 100 : 0
    score.obvValueOverSignal = obvValue > obvSignal ? 100 : 0

    // co
    def cos = analysis.cos.take(period)
    def coValues = cos.collect{it.value}
    def coValue = coValues.first()
    def coValuePctChange = tool.pctChange(coValues)
    def coSignals = cos.collect{it.signal}
    def coSignal = coSignals.first()
    score.coValue = coValue > 0 ? 100 : 0
    score.coValuePctChange = coValuePctChange > 0 ? 100 : 0
    score.coValueOverSignal = coValue > coSignal ? 100 : 0

    // return
    return score
}

// defines
def ohlcvPeriod = 1
def zScorePeriod = 20
def fastMaPeriod = 20
def slowMaPeriod = 120
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = "${assetIndicator.getAssetName()}(${assetId})"

// ohlcv
List<Ohlcv> ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, ohlcvPeriod)
def prices = ohlcvs.collect{it.getClose}
def price = prices.first()

// zScore
def priceZScore = tool.zScore(prices.take(zScorePeriod))
log.info("[{}] priceZScore: {}", assetName, priceZScore)
if (priceZScore.abs() < 1.0) {
    return null
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
        assetScores.hourly.getScore(),
        assetScores.daily.getScore(),
//        indiceScores.kospi.getAverage()
]
def totalScoreAverage = totalScores.average()
if (totalScoreAverage < 50) {
    hold = 0
}

// return
return hold

