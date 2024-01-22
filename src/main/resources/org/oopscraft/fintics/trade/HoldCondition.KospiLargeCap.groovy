package org.oopscraft.fintics.trade

import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.*

def analyze(Indicator indicator, OhlcvType ohlcvType, int period) {
    // info
    def name = indicator.getIndicatorName() + ':' + ohlcvType + ':' + period
    def pctChangePeriod = 5

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
    def cos = indicator.calculate(ohlcvType, period, CoContext.DEFAULT)
    def co = cos.first()
    def coValues = cos.collect{it.value}
    def coValue = coValues.first()
    def coValuePctChange = tool.pctChange(coValues.take(pctChangePeriod))
    def coSignals = cos.collect{it.signal}
    def coSignal = coSignals.first()
    log.debug("[{}] co: {}", name, co)

    // result
    def result = [:]
    result.shortMaValuePctChange = (shortMaValuePctChange > 0 ? 100 : 0)
    result.longMaValuePctChange = (longMaValuePctChange > 0 ? 100 : 0)
    result.shortMaValueOverLongMaValue = (shortMaValue > longMaValue ? 100 : 0)
    result.macdValuePctChange = (macdValuePctChange > 0 ? 100 : 0)
    result.macdValue = (macdValue > 0 ? 100 : 0)
    result.macdValueOverSignal = (macdValue > macdSignal ? 100 : 0)
    result.macdOscillator = (macdOscillator > 0 ? 100 : 0)
    result.rsiValuePctChange = (rsiValuePctChange > 0 ? 100 : 0)
    result.rsiValueOverSignal = (rsiValue > rsiSignal ? 100 : 0)
    result.rsiValue = (rsiValue > 50 ? 100 : 0)
    result.dmiPdiPctChange = (dmiPdiPctChange > 0 ? 100 : 0)
    result.dmiMdiPctChange = (dmiMdiPctChange < 0 ? 100 : 0)
    result.dmiPdiOverMdi = (dmiPdi > dmiMdi ? 100 : 0)
    result.dmiAdxPctChange = (dmiAdxPctChange > 0 && dmiPdiPctChange > 0 ? 100 : 0)
    result.dmiAdx = (dmiAdx > 20 && dmiPdi > dmiMdi ? 100 : 0)
    result.obvValuePctChange = (obvValuePctChange > 0 ? 100 : 0)
    result.obvValueOverSignal = (obvValue > obvSignal ? 100 : 0)
    result.coValuePctChange = (coValuePctChange > 0 ? 100 : 0)
    result.coValueOverSignal = (coValue > coSignal ? 100 : 0)
    result.coValue = (coValue > 0 ? 100 : 0)

    // return
    return result
}

// defines
def assetName = assetIndicator.getAssetName()
def holdVotes = []
log.info("[{}] dateTime: {}", assetName, dateTime)

// minute
def resultOfMinute = analyze(assetIndicator, OhlcvType.MINUTE, 1)
def resultOfMinuteAverage = resultOfMinute.values().average()
holdVotes.addAll(resultOfMinute.values())
log.debug("[{}] resultOfMinute: {}", assetName, resultOfMinute)
log.info("[{}] resultOfMinuteAverage: {}", assetName, resultOfMinuteAverage)

// minute3
def resultOfMinute3 = analyze(assetIndicator, OhlcvType.MINUTE, 3)
def resultOfMinute3Average = resultOfMinute3.values().average()
holdVotes.addAll(resultOfMinute3.values())
log.debug("[{}] resultOfMinute3: {}", assetName, resultOfMinute3)
log.info("[{}] resultOfMinute3verage: {}", assetName, resultOfMinute3Average)

// minute5
def resultOfMinute5 = analyze(assetIndicator, OhlcvType.MINUTE, 5)
def resultOfMinute5Average = resultOfMinute5.values().average()
holdVotes.addAll(resultOfMinute5.values())
log.debug("[{}] resultOfMinute5: {}", assetName, resultOfMinute5)
log.info("[{}] resultOfMinute5Average: {}", assetName, resultOfMinute5Average)

// minute10
def resultOfMinute10 = analyze(assetIndicator, OhlcvType.MINUTE, 10)
def resultOfMinute10Average = resultOfMinute10.values().average()
holdVotes.addAll(resultOfMinute10.values())
log.debug("[{}] resultOfMinute10: {}", assetName, resultOfMinute10)
log.info("[{}] resultOfMinute10Average: {}", assetName, resultOfMinute10Average)

// minute15
def resultOfMinute15 = analyze(assetIndicator, OhlcvType.MINUTE, 15)
def resultOfMinute15Average = resultOfMinute15.values().average()
holdVotes.addAll(resultOfMinute15.values())
log.debug("[{}] resultOfMinute15: {}", assetName, resultOfMinute15)
log.info("[{}] resultOfMinute15Average: {}", assetName, resultOfMinute15Average)

// minute30
def resultOfMinute30 = analyze(assetIndicator, OhlcvType.MINUTE, 30)
def resultOfMinute30Average = resultOfMinute30.values().average()
holdVotes.addAll(resultOfMinute30.values())
log.debug("[{}] resultOfMinute30: {}", assetName, resultOfMinute30)
log.info("[{}] resultOfMinute30Average: {}", assetName, resultOfMinute30Average)

// minute60
def resultOfMinute60 = analyze(assetIndicator, OhlcvType.MINUTE, 60)
def resultOfMinute60Average = resultOfMinute60.values().average()
holdVotes.addAll(resultOfMinute60.values())
log.debug("[{}] resultOfMinute60: {}", assetName, resultOfMinute60)
log.info("[{}] resultOfMinute60Average: {}", assetName, resultOfMinute60Average)

// daily
def resultOfDaily = analyze(assetIndicator, OhlcvType.DAILY, 1)
def resultOfDailyAverage = resultOfDaily.values().average()
holdVotes.addAll(resultOfDaily.values())
log.debug("[{}] resultOfDaily: {}", assetName, resultOfDaily)
log.info("[{}] resultOfDailyAverage: {}", assetName, resultOfDailyAverage)

// KOSPI
def resultOfKospi = analyze(indiceIndicators['KOSPI'], OhlcvType.DAILY, 1)
def resultOfKospiAverage = resultOfKospi.values().average()
holdVotes.addAll(resultOfKospi.values())
log.debug("[{}] resultOfKospi: {}", assetName, resultOfKospi)
log.info("[{}] resultOfKospiAverage: {}", assetName, resultOfKospiAverage)

// decide hold
def hold = null
def holdVotesAverage = holdVotes.average()
log.debug("[{}] holdVotes: {}", assetName, holdVotes)
log.info("[{}] holdVotesAverage: {}", assetName, holdVotesAverage)

// check buy
if(holdVotesAverage > 60) {
    hold = true
}

// check sell
if(holdVotesAverage < 40) {
    hold = false
}

// return
return hold
