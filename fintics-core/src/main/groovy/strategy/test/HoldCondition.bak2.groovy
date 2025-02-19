package org.oopscraft.fintics.trade.old

import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.TradeAsset
import org.oopscraft.fintics.indicator.*
import org.chomookun.fintics.indicator.ChaikinOscillatorContext
import org.chomookun.fintics.indicator.DmiContext
import org.chomookun.fintics.indicator.EmaContext
import org.chomookun.fintics.indicator.MacdContext
import org.chomookun.fintics.indicator.ObvContext
import org.chomookun.fintics.indicator.RsiContext
import org.oopscraft.fintics.model.*

def getIndicatorData(TradeAsset indicator, Ohlcv.Type ohlcvType, int ohlcvPeriod) {
    Map<String,Collection> result = [:]
    result.shortMas = indicator.calculate(ohlcvType, ohlcvPeriod, EmaContext.of(10))
    result.longMas = indicator.calculate(ohlcvType, ohlcvPeriod, EmaContext.of(30))
    result.macds = indicator.calculate(ohlcvType, ohlcvPeriod, MacdContext.DEFAULT)
    result.rsis = indicator.calculate(ohlcvType, ohlcvPeriod, RsiContext.DEFAULT)
    result.dmis = indicator.calculate(ohlcvType, ohlcvPeriod, DmiContext.DEFAULT)
    result.obvs = indicator.calculate(ohlcvType, ohlcvPeriod, ObvContext.DEFAULT)
    result.cos = indicator.calculate(ohlcvType, ohlcvPeriod, ChaikinOscillatorContext.DEFAULT)
    return result
}

def getMomentumScore(indicatorData) {
    def result = [:]

    // shortMa, longMa
    def shortMas = indicatorData.shortMas
    def longMas = indicatorData.longMas
    def shortMaValue = shortMas.first().value
    def longMaValue = longMas.first().value
    result.sortMaValueOverLongMaValue = shortMaValue > longMaValue ? 100 : 0

    // macd
    def macds = indicatorData.macds
    def macdValue = macds.first().value
    def macdSignal = macds.first().signal
    def macdOscillator = macds.first().oscillator
    result.macdValue = macdValue > 0 ? 100 : 0
    result.macdValueOverSignal = macdValue > macdSignal ? 100 : 0
    result.macdOscillator = macdOscillator > 0 ? 100 : 0

    // rsi
    def rsis = indicatorData.rsis
    def rsiValue = rsis.first().value
    def rsiSignal = rsis.first().signal
    result.rsiValueOver50 = rsiValue > 50 ? 100 : 0
    result.rsiValueOverSignal = rsiValue > rsiSignal ? 100 : 0

    // dmi
    def dmis = indicatorData.dmis
    def dmiPdi = dmis.first().pdi
    def dmiMdi = dmis.first().mdi
    def dmiAdx = dmis.first().adx
    result.dmiPdiOverMdi = dmiPdi > dmiMdi ? 100 : 0
    result.dmiAdx = dmiAdx > 20 && dmiPdi > dmiMdi ? 100 : 0

    // obv
    def obvs = indicatorData.obvs
    def obvValue = obvs.first().value
    def obvSignal = obvs.first().signal
    result.obvValueOverSignal = obvValue > obvSignal ? 100 : 0

    // co
    def cos = indicatorData.cos
    def coValue = cos.first().value
    def coSignal = cos.first().signal
    result.coValue = coValue > 0 ? 100 : 0
    result.coValueOverSignal = coValue > coSignal ? 100 : 0

    // return
    return result
}

def getDirectionScore(indicatorData) {
    def pctChangePeriod = 10;
    def result = [:]

    // shortMa, longMa
    def shortMas = indicatorData.shortMas
    def longMas = indicatorData.longMas
    def shortMaValues = shortMas.collect{it.value}
    def longMaValues = longMas.collect{it.value}
    def shortMaValuePctChange = tool.pctChange(shortMaValues.take(pctChangePeriod))
    def longMaValuePctChange = tool.pctChange(longMaValues.take(pctChangePeriod))
    result.shortMaValuePctChange = shortMaValuePctChange > 0 ? 100 : 0
    result.longMaValuePctChange = longMaValuePctChange > 0 ? 100 : 0

    // macd
    def macds = indicatorData.macds
    def macdValues = macds.collect{it.value}
    def macdSignals = macds.collect{it.signal}
    def macdValuePctChange = tool.pctChange(macdValues.take(pctChangePeriod))
    def macdSignalPctChange = tool.pctChange(macdSignals.take(pctChangePeriod))
    result.macdValuePctChange = macdValuePctChange > 0 ? 100 : 0
    result.macdSignalPctChange = macdSignalPctChange > 0 ? 100 : 0

    // rsi
    def rsis = indicatorData.rsis
    def rsiValues = rsis.collect{it.value}
    def rsiSignals = rsis.collect{it.signal}
    def rsiValuePctChange = tool.pctChange(rsiValues.take(pctChangePeriod))
    def rsiSignalPctChange = tool.pctChange(rsiSignals.take(pctChangePeriod))
    result.rsiValuePctChange = rsiValuePctChange > 0 ? 100 : 0
    result.rsiSignalPctChange = rsiSignalPctChange > 0 ? 100 : 0

    // dmi
    def dmis = indicatorData.dmis
    def dmiPdis = dmis.collect{it.pdi}
    def dmiMdis = dmis.collect{it.mdi}
    def dmiAdxs = dmis.collect{it.adx}
    def dmiPdiPctChange = tool.pctChange(dmiPdis.take(pctChangePeriod))
    def dmiMdiPctChange = tool.pctChange(dmiMdis.take(pctChangePeriod))
    def dmiAdxPctChange = tool.pctChange(dmiAdxs.take(pctChangePeriod))
    result.dmiPdiPctChange = dmiPdiPctChange > 0 ? 100 : 0
    result.dmiMdiPctChange = dmiMdiPctChange < 0 ? 100 : 0
    result.dmiAdx = dmiAdxPctChange > 0 && dmiPdiPctChange > 0 && dmiMdiPctChange < 0 ? 100 : 0

    // obvs
    def obvs = indicatorData.obvs
    def obvValues = obvs.collect{it.value}
    def obvSignals = obvs.collect{it.signal}
    def obvValuePctChange = tool.pctChange(obvValues.take(pctChangePeriod))
    def obvSignalPctChange = tool.pctChange(obvSignals.take(pctChangePeriod))
    result.obvValuePctChange = obvValuePctChange > 0 ? 100 : 0
    result.obvSignalPctChange = obvSignalPctChange > 0 ? 100 : 0

    // co
    def cos = indicatorData.cos
    def coValues = cos.collect{it.value}
    def coSignals = cos.collect{it.signal}
    def coValuePctChange = tool.pctChange(coValues.take(pctChangePeriod))
    def coSignalPctChange = tool.pctChange(coSignals.take(pctChangePeriod))
    result.coValuePctChange = coValuePctChange > 0 ? 100 : 0
    result.coSignalPctChange = coSignalPctChange > 0 ? 100 : 0

    // return
    return result
}

// defines
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = assetIndicator.getAssetName()
def assetIdName = "${assetName}(${assetId})"

// asset indicator data
def shortAssetIndicatorData = getIndicatorData(assetIndicator, Ohlcv.Type.MINUTE, 1)
def middleAssetIndicatorData = getIndicatorData(assetIndicator, Ohlcv.Type.MINUTE, 10)
def longAssetIndicatorData = getIndicatorData(assetIndicator, Ohlcv.Type.MINUTE, 60)

// asset momentum score
def shortAssetMomentumScore = getMomentumScore(shortAssetIndicatorData)
def middleAssetMomentumScore = getMomentumScore(middleAssetIndicatorData)
def longAssetMomentumScore = getMomentumScore(longAssetIndicatorData)
def shortAssetMomentumScoreAverage = shortAssetMomentumScore.values().average()
def middleAssetMomentumScoreAverage = middleAssetMomentumScore.values().average()
def longAssetMomentumScoreAverage = longAssetMomentumScore.values().average()
log.info("[{}] shortAssetMomentumScore[{}]: {}", assetIdName, shortAssetMomentumScoreAverage, shortAssetMomentumScore)
log.info("[{}] middleAssetMomentumScore[{}]: {}", assetIdName, middleAssetMomentumScoreAverage, middleAssetMomentumScore)
log.info("[{}] longAssetMomentumScore[{}]: {}", assetIdName, longAssetMomentumScoreAverage, longAssetMomentumScore)

// asset direction score
def shortAssetDirectionScore = getDirectionScore(shortAssetIndicatorData)
def middleAssetDirectionScore = getDirectionScore(middleAssetIndicatorData)
def longAssetDirectionScore = getDirectionScore(longAssetIndicatorData)
def shortAssetDirectionScoreAverage = shortAssetDirectionScore.values().average()
def middleAssetDirectionScoreAverage = middleAssetDirectionScore.values().average()
def longAssetDirectionScoreAverage = longAssetDirectionScore.values().average()
log.info("[{}] shortAssetDirectionScore[{}]: {}", assetIdName, shortAssetDirectionScoreAverage, shortAssetDirectionScore)
log.info("[{}] middleAssetDirectionScore[{}]: {}", assetIdName, middleAssetDirectionScoreAverage, middleAssetDirectionScore)
log.info("[{}] longAssetDirectionScore[{}]: {}", assetIdName, longAssetDirectionScoreAverage, longAssetDirectionScore)

// enable trade
if(longAssetMomentumScoreAverage > 70) {
    if(shortAssetDirectionScoreAverage > middleAssetDirectionScoreAverage) {
        hold = true
    }
    if(shortAssetDirectionScoreAverage < middleAssetDirectionScoreAverage) {
        hold = false
    }
}

// fallback
if(longAssetMomentumScoreAverage < 50) {
    hold = false
}

//// momentum score
//def momentumScores = []
//momentumScores.addAll(longAssetMomentumScore.values())
//momentumScores.addAll(middleAssetMomentumScore.values())
//def momentumScoreAverage = momentumScores.average()
//log.info("[{}] momentumScoreAverage[{}]: {}", assetIdName, momentumScoreAverage, momentumScores)
//
//// direction score
//def directionScores = []
//directionScores.addAll(middleAssetDirectionScore.values())
//directionScores.addAll(shortAssetDirectionScore.values())
//def directionScoreAverage = directionScores.average()
//log.info("[{}] directionScoreAverage[{}]: {}", assetIdName, directionScoreAverage, directionScores)
//
//// buy
//if(momentumScoreAverage > 70) {
//    if(directionScoreAverage > 70) {
//        hold = true
//    }
//    if(directionScoreAverage < 50) {
//        hold = false
//    }
//}
//
//// sell
//if(momentumScoreAverage < 50) {
//    hold = false
//}

// return
return hold
