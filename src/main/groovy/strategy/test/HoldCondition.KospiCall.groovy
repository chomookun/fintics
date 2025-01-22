package org.oopscraft.fintics.trade.old

import org.chomoo.fintics.model.Ohlcv
import org.chomoo.fintics.model.TradeAsset
import org.oopscraft.fintics.indicator.*
import org.chomoo.fintics.indicator.ChaikinOscillatorContext
import org.chomoo.fintics.indicator.DmiContext
import org.chomoo.fintics.indicator.EmaContext
import org.chomoo.fintics.indicator.MacdContext
import org.chomoo.fintics.indicator.ObvContext
import org.chomoo.fintics.indicator.RsiContext
import org.oopscraft.fintics.model.*

/**
 * analyze indicator
 * @param indicator indicator
 * @param ohlcvType ohlcvType
 * @param period period
 * @return result map
 */
def analyzeIndicator(TradeAsset indicator, Ohlcv.Type ohlcvType, int period) {
    // info
    def name = indicator.getProfileName() + ':' + ohlcvType + ':' + period
    def pctChangePeriod = 10

    // shortMa
    def shortMas = indicator.calculate(ohlcvType, period, EmaContext.of(10))
    def shortMa = shortMas.first()
    def shortMaValues = shortMas.collect{it.value}
    def shortMaValue = shortMaValues.first()
    def shortMaValuePctChange = tool.pctChange(shortMaValues.take(pctChangePeriod))
    log.debug("[{}] shortMa: {}", name, shortMa)

    // longMa
    def longMas = indicator.calculate(ohlcvType, period, EmaContext.of(30))
    def longMa = longMas.first()
    def longMaValues = longMas.collect{it.value}
    def longMaValue = longMaValues.first()
    def longMaValuePctChange = tool.pctChange(longMaValues.take(pctChangePeriod))
    log.debug("[{}] longMa: {}", name, longMa)

    // macd
    def macds = indicator.calculate(ohlcvType, period, MacdContext.DEFAULT)
    def macd = macds.first()
    def macdValues = macds.collect{it.value}
    def macdValue = macdValues.first()
    def macdValuePctChange = tool.pctChange(macdValues.take(pctChangePeriod))
    def macdSignals = macds.collect{it.signal}
    def macdSignal = macdSignals.first()
    def macdOscillators = macds.collect{it.oscillator}
    def macdOscillator = macdOscillators.first()
    log.debug("[{}] macd: {}", name, macd)

    // rsi
    def rsis = indicator.calculate(ohlcvType, period, RsiContext.DEFAULT)
    def rsi = rsis.first()
    def rsiValues = rsis.collect{it.value}
    def rsiValue = rsiValues.first()
    def rsiValuePctChange = tool.pctChange(rsiValues.take(pctChangePeriod))
    def rsiSignals = rsis.collect{it.signal}
    def rsiSignal = rsiSignals.first()
    log.debug("[{}] rsi: {}", name, rsi)

    // dmi
    def dmis = indicator.calculate(ohlcvType, period, DmiContext.DEFAULT)
    def dmi = dmis.first()
    def dmiPdis = dmis.collect{it.pdi}
    def dmiPdi = dmiPdis.first()
    def dmiPdiPctChange = tool.pctChange(dmiPdis.take(pctChangePeriod))
    def dmiMdis = dmis.collect{it.mdi}
    def dmiMdi = dmiMdis.first()
    def dmiMdiPctChange = tool.pctChange(dmiMdis.take(pctChangePeriod))
    def dmiAdxs = dmis.collect{it.adx}
    def dmiAdx = dmiAdxs.first()
    def dmiAdxPctChange = tool.pctChange(dmiAdxs.take(pctChangePeriod))
    log.debug("[{}] dmi: {}", name, dmi)

    // obv
    def obvs = indicator.calculate(ohlcvType, period, ObvContext.DEFAULT)
    def obv = obvs.first()
    def obvValues = obvs.collect{it.value}
    def obvValue = obvValues.first()
    def obvValuePctChange = tool.pctChange(obvValues.take(pctChangePeriod))
    def obvSignals = obv.collect{it.signal}
    def obvSignal = obvSignals.first()
    log.debug("[{}] obv:{}", name, obv)

    // co
    def cos = indicator.calculate(ohlcvType, period, ChaikinOscillatorContext.DEFAULT)
    def co = cos.first()
    def coValues = cos.collect{it.value}
    def coValue = coValues.first()
    def coValuePctChange = tool.pctChange(coValues.take(pctChangePeriod))
    def coSignals = cos.collect{it.signal}
    def coSignal = coSignals.first()
    log.debug("[{}] co: {}", name, co)

    // result
    def result = [:]
//    result.shortMaValuePctChange = (shortMaValuePctChange > 0 ? 100 : 0)
//    result.longMaValuePctChange = (longMaValuePctChange > 0 ? 100 : 0)
    result.shortMaValueOverLongMaValue = (shortMaValue > longMaValue ? 100 : 0)
//    result.macdValuePctChange = (macdValuePctChange > 0 ? 100 : 0)
    result.macdValue = (macdValue > 0 ? 100 : 0)
    result.macdValueOverSignal = (macdValue > macdSignal ? 100 : 0)
    result.macdOscillator = (macdOscillator > 0 ? 100 : 0)
//    result.rsiValuePctChange = (rsiValuePctChange > 0 ? 100 : 0)
    result.rsiValueOverSignal = (rsiValue > rsiSignal ? 100 : 0)
    result.rsiValue = (rsiValue > 50 ? 100 : 0)
//    result.dmiPdiPctChange = (dmiPdiPctChange > 0 ? 100 : 0)
//    result.dmiMdiPctChange = (dmiMdiPctChange < 0 ? 100 : 0)
    result.dmiPdiOverMdi = (dmiPdi > dmiMdi ? 100 : 0)
//    result.dmiAdxPctChange = (dmiAdxPctChange > 0 && dmiPdiPctChange > 0 ? 100 : 0)
    result.dmiAdx = (dmiAdx > 20 && dmiPdi > dmiMdi ? 100 : 0)
//    result.obvValuePctChange = (obvValuePctChange > 0 ? 100 : 0)
    result.obvValueOverSignal = (obvValue > obvSignal ? 100 : 0)
//    result.coValuePctChange = (coValuePctChange > 0 ? 100 : 0)
    result.coValueOverSignal = (coValue > coSignal ? 100 : 0)
    result.coValue = (coValue > 0 ? 100 : 0)

    // return
    return result
}

//=============================
// defines
//=============================
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = assetIndicator.getAssetName()
def assetAlias = "${assetName}(${assetId})"
def analysisAverages = []

//=============================
// analyze asset
//=============================
def assetAnalysisMap = [:]
def assetAnalysisAverages = []
//assetAnalysisMap.minute3 = analyzeIndicator(assetIndicator, OhlcvType.MINUTE, 3)
//assetAnalysisMap.minute10 = analyzeIndicator(assetIndicator, OhlcvType.MINUTE, 10)
assetAnalysisMap.minute30 = analyzeIndicator(assetIndicator, Ohlcv.Type.MINUTE, 30)
assetAnalysisMap.minute60 = analyzeIndicator(assetIndicator, Ohlcv.Type.MINUTE, 60)
assetAnalysisMap.daily = analyzeIndicator(assetIndicator, Ohlcv.Type.DAILY, 1)
assetAnalysisMap.each { key, value ->
    def average = value.values().getAverage()
    log.debug("[{}] assetAnalysisMap.{}: {}", assetAlias, key, average)
    analysisAverages.add(average)
    assetAnalysisAverages.add(average)
}

//=============================
// analyze indice
//=============================
def indiceAnalysisMap = [:]
def indiceAnalysisAverages = []

// USD/KRW (inverse)
//indiceAnalysisMap.usdKrw = analyzeIndicator(indiceIndicators['USD_KRW'], OhlcvType.MINUTE, 60)
//analysisAverages.add(100 - (indiceAnalysisMap.usdKrw.values().average() as Number))

// KOSPI
//indiceAnalysisMap.kospi = analyzeIndicator(indiceIndicators['KOSPI'], OhlcvType.MINUTE, 60)
//analysisAverages.add(indiceAnalysisMap.kospi.values().average())

// Nasdaq Future
//indiceAnalysisMap.ndxFuture = analyzeIndicator(indiceIndicators['NDX_FUTURE'], OhlcvType.MINUTE, 60)
//analysisAverages.add(indiceAnalysisMap.ndxFuture.values().average())

// logging
indiceAnalysisMap.each { key, value ->
    def average = value.values().getAverage()
    log.debug("[{}] indiceAnalysisMap.{}: {}", assetAlias, key, average)
    indiceAnalysisAverages.add(average)
}

//=============================
// decide hold
//=============================
def analysisAverage = analysisAverages.average()
log.info("[{}] analysisAverages:{}", assetAlias, analysisAverages)
log.info("[{}] analysisAverage:{}", assetAlias, analysisAverage)

// 1. default fallback
if(analysisAverage > 70) {
    log.info("[{}] analysisAverage over 70", assetAlias)
    hold = true
}
if(analysisAverage < 50) {
    log.info("[{}] analysisAverage under 50", assetAlias)
    hold = false
}

//==============================
// return
//==============================
return hold
