import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.chomookun.fintics.indicator.Atr
import org.chomookun.fintics.indicator.AtrContext
import org.chomookun.fintics.indicator.BollingerBand
import org.chomookun.fintics.indicator.BollingerBandContext
import org.chomookun.fintics.indicator.Cci
import org.chomookun.fintics.indicator.CciContext
import org.chomookun.fintics.indicator.ChaikinOscillator
import org.chomookun.fintics.indicator.ChaikinOscillatorContext
import org.chomookun.fintics.indicator.Dmi
import org.chomookun.fintics.indicator.DmiContext
import org.chomookun.fintics.indicator.Ema
import org.chomookun.fintics.indicator.EmaContext
import org.chomookun.fintics.indicator.Macd
import org.chomookun.fintics.indicator.MacdContext
import org.chomookun.fintics.indicator.PriceChannel
import org.chomookun.fintics.indicator.PriceChannelContext
import org.chomookun.fintics.indicator.Rsi
import org.chomookun.fintics.indicator.RsiContext
import org.chomookun.fintics.indicator.Sma
import org.chomookun.fintics.indicator.SmaContext
import org.chomookun.fintics.indicator.StochasticSlow
import org.chomookun.fintics.indicator.StochasticSlowContext
import org.chomookun.fintics.indicator.WilliamsR
import org.chomookun.fintics.indicator.WilliamsRContext
import org.jetbrains.annotations.NotNull
import org.oopscraft.fintics.indicator.*
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.TradeAsset
import org.chomookun.fintics.strategy.StrategyResult
import org.chomookun.fintics.strategy.StrategyResult.Action
import org.chomookun.fintics.trade.Tools

import java.math.RoundingMode
/**
 * score
 */
class Score extends LinkedHashMap<String, BigDecimal> implements Comparable<Score> {
    Number getAverage() {
        return this.values().empty ? 0 : this.values().average() as Number
    }
    @Override
    int compareTo(@NotNull Score o) {
        return Double.compare(this.getAverage().doubleValue(), o.getAverage().doubleValue())
    }
    int compareTo(@NotNull Number o) {
        return Double.compare(this.getAverage().doubleValue(), o.doubleValue())
    }
    @Override
    String toString() {
        return this.getAverage() + ' ' + super.toString()
    }
}

/**
 * channel
 */
@ToString
class Channel {
    BigDecimal upper
    BigDecimal lower
    BigDecimal middle
    LinkedHashMap<String, Object> source = new LinkedHashMap<>()
}

/**
 * analyzer
 */
class Analyzer {
    List<Ohlcv> ohlcvs
    Ohlcv ohlcv
    List<Sma> smas
    Sma sma
    List<Ema> emas
    Ema ema
    List<Macd> macds
    Macd macd
    List<BollingerBand> bollingerBands
    BollingerBand bollingerBand
    List<Dmi> dmis
    Dmi dmi
    List<ChaikinOscillator> chaikinOscillators
    ChaikinOscillator chaikinOscillator
    List<Atr> atrs
    Atr atr
    List<Rsi> rsis
    Rsi rsi
    List<Cci> ccis
    Cci cci
    List<StochasticSlow> stochasticSlows
    StochasticSlow stochasticSlow
    List<WilliamsR> williamsRs
    WilliamsR williamsR

    /**
     * constructor
     * @param tradeAsset trade asset
     * @param ohlcvType ohlcv type
     * @param ohlcvPeriod ohlcv period
     */
    Analyzer(TradeAsset tradeAsset, Ohlcv.Type ohlcvType, int ohlcvPeriod) {
        this.ohlcvs = tradeAsset.getOhlcvs(ohlcvType, ohlcvPeriod)
        this.ohlcv = this.ohlcvs.first()
        this.smas = Tools.indicators(ohlcvs, SmaContext.DEFAULT)
        this.sma = smas.first()
        this.emas = Tools.indicators(ohlcvs, EmaContext.DEFAULT)
        this.ema = emas.first()
        this.macds = Tools.indicators(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
        this.bollingerBands = Tools.indicators(ohlcvs, BollingerBandContext.DEFAULT)
        this.bollingerBand = bollingerBands.first()
        this.dmis = Tools.indicators(ohlcvs, DmiContext.DEFAULT)
        this.dmi = this.dmis.first()
        this.atrs = Tools.indicators(ohlcvs, AtrContext.DEFAULT)
        this.atr = atrs.first()
        this.rsis = Tools.indicators(ohlcvs, RsiContext.DEFAULT)
        this.rsi = rsis.first()
        this.ccis = Tools.indicators(ohlcvs, CciContext.DEFAULT)
        this.cci = ccis.first()
        this.stochasticSlows = Tools.indicators(ohlcvs, StochasticSlowContext.DEFAULT)
        this.stochasticSlow = stochasticSlows.first()
        this.williamsRs = Tools.indicators(ohlcvs, WilliamsRContext.DEFAULT)
        this.williamsR = williamsRs.first()
        this.chaikinOscillators = Tools.indicators(ohlcvs, ChaikinOscillatorContext.DEFAULT)
        this.chaikinOscillator = chaikinOscillators.first()
    }

    /**
     * gets current close price
     * @return current close price
     */
    BigDecimal getCurrentClose() {
        return ohlcv.close
    }

    /**
     * gets average close price
     * @return average close price
     */
    BigDecimal getAverageClose() {
        return Tools.mean(ohlcvs.take(20).collect{it.close})
    }

    /**
     * adjust average position
     * @param position
     * @return average position
     */
    BigDecimal adjustAveragePosition(BigDecimal position) {
        def averagePrice = this.getAverageClose()
        def currentPrice = this.getCurrentClose()
        def averageWeight = averagePrice/currentPrice as BigDecimal
        def averagePosition = ((position * averageWeight) as BigDecimal)
                .setScale(2, RoundingMode.HALF_UP)
        return averagePosition
    }

    /**
     * gets channel
     * @param period period
     * @return channel
     */
    Channel getChannel(int period) {
        def channel = new Channel()
        def uppers = []
        def lowers = []

        // price channel
        List<PriceChannel> priceChannels = Tools.indicators(ohlcvs, PriceChannelContext.of(period))
        def priceChannel = priceChannels.first()
        channel.source.priceChannel = priceChannel
        uppers.add(priceChannel.upper)
        lowers.add(priceChannel.lower)

        // bollinger band
        List<BollingerBand> bollingerBands = Tools.indicators(ohlcvs, BollingerBandContext.of(period, 2))
        def bollingerBand = bollingerBands.first()
        channel.source.bollingerBand = bollingerBand
        uppers.add(bollingerBand.upper)
        lowers.add(bollingerBand.lower)

        // set channel value
        channel.upper = (uppers.average() as BigDecimal).setScale(4, RoundingMode.HALF_UP)
        channel.lower = (lowers.average() as BigDecimal).setScale(4, RoundingMode.HALF_UP)
        channel.middle = ((channel.upper + channel.lower) / 2).setScale(4, RoundingMode.HALF_UP)

        // return
        return channel
    }

    /**
     * gets momentum score
     * @return momentum score
     */
    Score getMomentumScore() {
        def score = new Score()
        // macd
        score.macdValueOverSignal = macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
        // dmi
        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
        // rsi
        score.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
        // cci
        score.cciValueOverSignal = cci.value > cci.signal ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValueOverSignal = chaikinOscillator.value > chaikinOscillator.signal ? 100 : 0
        // return
        return score
    }

    /**
     * gets volatility score
     * @return volatility score
     */
    Score getVolatilityScore() {
        def score = new Score()
        // dmi
        score.dmiAdx = dmi.adx >= 25 ? 100 : 0
        // return
        return score
    }

    /**
     * gets oversold score
     * @return oversold score
     */
    Score getOversoldScore() {
        def score = new Score()
        // rsi: 30 이하인 경우 과매도 판정
        score.rsi = rsis.take(3).any{it.value <= 30} ? 100 : 0
        // cci: -100 이하인 경우 과매도 판정
        score.cci = ccis.take(3).any{it.value <= -100} ? 100 : 0
        // stochastic slow: 20 이하인 경우 과매도 판정
        score.stochasticSlow = stochasticSlows.take(3).any{it.slowK <= 20} ? 100 : 0
        // williams r: -80 이하인 경우 과매도 판정
        score.williamsR = williamsRs.take(3).any{it.value <= -80} ? 100 : 0
        // return
        return score
    }

    /**
     * gets overbought score
     * @return overbought score
     */
    Score getOverboughtScore() {
        def score = new Score()
        // rsi: 70 이상인 경우 과매수 구간 판정
        score.rsi = rsis.take(3).any{it.value >= 70} ? 100 : 0
        // cci: 100 이상인 경우 과매수 판정
        score.cci = ccis.take(3).any{it.value >= 100} ? 100 : 0
        // stochastic slow: 80 이상인 경우 과매수 판정
        score.stochasticSlow = stochasticSlows.take(3).any{it.slowK >= 80} ? 100 : 0
        // williams r: -20 이상인 경우 과매수 판정
        score.williamsR = williamsRs.take(3).any{it.value >= -20} ? 100 : 0
        // return
        return score
    }

    /**
     * gets trailing stop score
     * @return trailing stop score
     */
    Score getTrailingStopScore() {
        def score = new Score()
        // atr
        def prevOhlcv = ohlcvs.get(1)
        def prevAtr = atrs.get(1)
        def stopPrice = prevOhlcv.high - (prevAtr.value * 2.0)
        score.atrAtr = ohlcv.close < stopPrice ? 100 : 0
        // return
        return score
    }

    @Override
    String toString() {
        return [
                momentumScore: "${this.getMomentumScore()}",
                volatilityScore: "${this.getVolatilityScore()}",
                oversoldScore: "${this.getOversoldScore()}",
                overboughtScore: "${this.getOverboughtScore()}",
                trailingStopScore: "${this.getTrailingStopScore()}"
        ].toString()
    }
}

/**
 * triple screen strategy
 */
class TripleScreenStrategy {

    String name
    Analyzer tideAnalyzer
    Analyzer waveAnalyzer
    Analyzer rippleAnalyzer

    /**
     * constructor
     * @param name name
     * @param tradeAsset trade asset
     * @param maxPosition maximum position
     * @param minPosition minimum position
     * @param tideOhlcvType tide ohlcv type
     * @param tideOhlcvPeriod tide ohlcv period
     * @param waveOhlcvType wave ohlcv type
     * @param waveOhlcvPeriod wave ohlcv period
     * @param rippleOhlcvType ripple ohlcv type
     * @param rippleOhlcvPeriod ripple ohlcv period
     */
    @Builder
    TripleScreenStrategy(String name, TradeAsset tradeAsset, Ohlcv.Type tideOhlcvType, int tideOhlcvPeriod, Ohlcv.Type waveOhlcvType, int waveOhlcvPeriod, Ohlcv.Type rippleOhlcvType, int rippleOhlcvPeriod) {
        this.name = name
        this.tideAnalyzer = new Analyzer(tradeAsset, tideOhlcvType, tideOhlcvPeriod)
        this.waveAnalyzer = new Analyzer(tradeAsset, waveOhlcvType, waveOhlcvPeriod)
        this.rippleAnalyzer = new Analyzer(tradeAsset, rippleOhlcvType, rippleOhlcvPeriod)
    }

    /**
     * 모멘텀 Score 기준 position 산출
     * @return
     */
    BigDecimal getPosition(BigDecimal maxPosition, BigDecimal minPosition) {
        // 모멘텀 점수 계산 (50 이상일 때 포지션 증가)
        def positionScore = (tideAnalyzer.getMomentumScore().getAverage() - 50).max(0)*2
        // 포지션 1%당 변화량 계산
        def positionPerScore = (maxPosition - minPosition)/100
        // 최종 포지션 계산
        def position = minPosition + (positionPerScore * positionScore) as BigDecimal
        // 소수점 2자리로 제한
        position = position.setScale(2, RoundingMode.HALF_UP)
        // return
        return position
    }

    /**
     * gets strategy result
     * @return strategy result
     */
    Optional<StrategyResult> getResult(BigDecimal maxPosition, BigDecimal minPosition) {
        StrategyResult strategyResult = null

        // tide 모멘텀 기준 포지션 산출
        def position = this.getPosition(maxPosition, minPosition)

        // 과매도, 과매수 임계치 - 기본 50
        def waveOversoldThreshold = 50
        def waveOverboughtThreshold = 50
        // tide 상승 추세 인 경우 과매도 판정 민감도 추가
        if (tideAnalyzer.getMomentumScore() >= 75) {
            waveOversoldThreshold = 25
            waveOverboughtThreshold = 75
        }
        // tide 하락 추세 인 경우 과매수 판정 민감도 증가
        if (tideAnalyzer.getMomentumScore() <= 25) {
            waveOverboughtThreshold = 25
            waveOversoldThreshold = 75
        }

        // wave 변동성 구간
        if (waveAnalyzer.getVolatilityScore() >= 50) {
            // wave 과매도 시
            if (waveAnalyzer.getOversoldScore() >= waveOversoldThreshold) {
                // ripple 상승 모멘텀
                if (rippleAnalyzer.getMomentumScore() >= 50) {
                    // wave 평균가 기준 매수 포지션
                    def waveAveragePosition = waveAnalyzer.adjustAveragePosition(position)
                    strategyResult = StrategyResult.of(Action.BUY, waveAveragePosition, "[WAVE OVERSOLD BUY] ${this.toString()}")
                    // filter - tide 가 과매수 상태인 경우 매수 제한
                    if (tideAnalyzer.getOverboughtScore() >= 50) {
                        strategyResult = null
                    }
                }
            }
            // wave 과매수 시
            if (waveAnalyzer.getOverboughtScore() >= waveOverboughtThreshold) {
                // ripple 하락 모멘텀
                if (rippleAnalyzer.getMomentumScore() <= 50) {
                    // wave 평균가 기준 매도 포지션
                    def waveAveragePosition = waveAnalyzer.adjustAveragePosition(position)
                    strategyResult = StrategyResult.of(Action.SELL, waveAveragePosition, "[WAVE OVERBOUGHT SELL] ${this.toString()}")
                    // filter - tide 가 과매도 상태인 경우 매도 제한
                    if (tideAnalyzer.getOversoldScore() >= 50) {
                        strategyResult = null
                    }
                }
            }
        }

        // 무한 매수/매도 빠지는 경우 발생 으로 일단 제외 (생각 좀 해보자)
//        // if all bearish momentum trailing stop
//        if (tideAnalyzer.getMomentumScore() < 25) {
//            if (waveAnalyzer.getTrailingStopScore() > 50) {
//                def tideAveragePosition = tideAnalyzer.adjustAveragePosition(position)
//                strategyResult = StrategyResult.of(Action.SELL, tideAveragePosition, "[TRAILING STOP] ${this.toString()}")
//            }
//        }

        // returns
        return Optional.ofNullable(strategyResult)
    }

    @Override
    String toString() {
        return  "${this.name}:[" +
                "tide.momentum:${tideAnalyzer.getMomentumScore().getAverage()}," +
                "wave.oversold:${waveAnalyzer.getOversoldScore().getAverage()}," +
                "wave.overbought:${waveAnalyzer.getOverboughtScore().getAverage()}," +
                "ripple.momentum:${rippleAnalyzer.getMomentumScore().getAverage()}" +
                "(" +
                "tide.oversold:${tideAnalyzer.getOversoldScore().getAverage()}," +
                "tide.overbought:${tideAnalyzer.getOverboughtScore().getAverage()}," +
                "wave.momentum:${waveAnalyzer.getMomentumScore().getAverage()}" +
                ")" +
                "]"
    }

}

//===============================
// config
//===============================
log.info("variables: {}", variables)
// macro
def microTideOhlcvType = variables['micro.tide.ohlcv.type'] as Ohlcv.Type
def microTideOhlcvPeriod = variables['micro.tide.ohlcv.period'] as Integer
def microWaveOhlcvType = variables['micro.wave.ohlcv.type'] as Ohlcv.Type
def microWaveOhlcvPeriod = variables['micro.wave.ohlcv.period'] as Integer
def microRippleOhlcvType = variables['micro.ripple.ohlcv.type'] as Ohlcv.Type
def microRippleOhlcvPeriod = variables['micro.ripple.ohlcv.period'] as Integer
// meso
def mesoTideOhlcvType = variables['meso.tide.ohlcv.type'] as Ohlcv.Type
def mesoTideOhlcvPeriod = variables['meso.tide.ohlcv.period'] as Integer
def mesoWaveOhlcvType = variables['meso.wave.ohlcv.type'] as Ohlcv.Type
def mesoWaveOhlcvPeriod = variables['meso.wave.ohlcv.period'] as Integer
def mesoRippleOhlcvType = variables['meso.ripple.ohlcv.type'] as Ohlcv.Type
def mesoRippleOhlcvPeriod = variables['meso.ripple.ohlcv.period'] as Integer
// macro
def macroTideOhlcvType = variables['macro.tide.ohlcv.type'] as Ohlcv.Type
def macroTideOhlcvPeriod = variables['macro.tide.ohlcv.period'] as Integer
def macroWaveOhlcvType = variables['macro.wave.ohlcv.type'] as Ohlcv.Type
def macroWaveOhlcvPeriod = variables['macro.wave.ohlcv.period'] as Integer
def macroRippleOhlcvType = variables['macro.ripple.ohlcv.type'] as Ohlcv.Type
def macroRippleOhlcvPeriod = variables['macro.ripple.ohlcv.period'] as Integer
// etc
def basePosition = variables['basePosition'] as BigDecimal
def sellProfitPercentageThreshold = variables['sellProfitPercentageThreshold'] as BigDecimal

//===============================
// defines
//===============================
StrategyResult strategyResult = null
List<Ohlcv> ohlcvs = tradeAsset.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def ohlcv = ohlcvs.first()
def splitPeriod = 100
def splitSize = 5
def splitIndex = -1
if (variables['splitIndex']) {
    splitIndex = variables['splitIndex'] as Integer
}

//===============================
// basket asset variable
//===============================
// basket asset variables 에 개별 설정 시 해당 split size, index 적용
if (basketAsset.getVariable('splitSize')) {
    log.info('override splitSize by basket asset variable')
    splitSize = basketAsset.getVariable('splitSize') as Integer
}
if (basketAsset.getVariable('splitIndex')) {
    log.info('override splitIndex by basket asset variable')
    splitIndex = basketAsset.getVariable('splitIndex') as Integer
}

//===============================
// strategy
//===============================
// micro
def microTripleScreenStrategy = TripleScreenStrategy.builder()
        .name("micro")
        .tradeAsset(tradeAsset)
        .tideOhlcvType(microTideOhlcvType)
        .tideOhlcvPeriod(microTideOhlcvPeriod)
        .waveOhlcvType(microWaveOhlcvType)
        .waveOhlcvPeriod(microWaveOhlcvPeriod)
        .rippleOhlcvType(microRippleOhlcvType)
        .rippleOhlcvPeriod(microRippleOhlcvPeriod)
        .build()
// meso
def mesoTripleScreenStrategy = TripleScreenStrategy.builder()
        .name("meso")
        .tradeAsset(tradeAsset)
        .tideOhlcvType(mesoTideOhlcvType)
        .tideOhlcvPeriod(mesoTideOhlcvPeriod)
        .waveOhlcvType(mesoWaveOhlcvType)
        .waveOhlcvPeriod(mesoWaveOhlcvPeriod)
        .rippleOhlcvType(mesoRippleOhlcvType)
        .rippleOhlcvPeriod(mesoRippleOhlcvPeriod)
        .build()
// macro
def macroTripleScreenStrategy = TripleScreenStrategy.builder()
        .name("macro")
        .tradeAsset(tradeAsset)
        .tideOhlcvType(macroTideOhlcvType)
        .tideOhlcvPeriod(macroTideOhlcvPeriod)
        .waveOhlcvType(macroWaveOhlcvType)
        .waveOhlcvPeriod(macroWaveOhlcvPeriod)
        .rippleOhlcvType(macroRippleOhlcvType)
        .rippleOhlcvPeriod(macroRippleOhlcvPeriod)
        .build()

//===============================
// split limit
//===============================
def channel =  mesoTripleScreenStrategy.tideAnalyzer.getChannel(splitPeriod)
def splitMaxPrice = channel.upper
def splitMinPrice = channel.lower
def splitInterval = ((splitMaxPrice - splitMinPrice)/splitSize as BigDecimal).setScale(4, RoundingMode.HALF_UP)
def splitLimitPrices = (0..splitSize-1).collect {
    splitMaxPrice - (it * splitInterval) as BigDecimal
}
def splitLimitPrice = null
def splitBuyLimited = false
// splitIndex 가 0 이상 설정된 경우
if (splitIndex >= 0) {
    splitLimitPrice = splitLimitPrices[splitIndex]
    // 현제 가격이 split limit 이상인 경우 분할 매수 제한
    if (ohlcv.close > splitLimitPrice) {
        splitBuyLimited = true
    }
}

//===============================
// profit percentage
//===============================
def profitPercentage = balanceAsset?.getProfitPercentage() ?: 0.0

//===============================
// position
//===============================
def maxPosition = 1.0
def minPosition = basePosition

//===============================
// message
//===============================
def message = """
splitSize:${splitSize}, splitIndex:${splitIndex}
splitLimits:${splitLimitPrices}
splitLimitPrice:${splitLimitPrice}
splitBuyLimited:${splitBuyLimited}
positon:${microTripleScreenStrategy.getPosition(maxPosition, minPosition)}, ${mesoTripleScreenStrategy.getPosition(maxPosition, minPosition)}, ${macroTripleScreenStrategy.getPosition(maxPosition, minPosition)}
${microTripleScreenStrategy}
${mesoTripleScreenStrategy}
${macroTripleScreenStrategy}
"""
log.info("message: {}", message)
tradeAsset.setMessage(message)

//===============================
// execute strategy
//===============================
// micro
microTripleScreenStrategy.getResult(maxPosition, minPosition).ifPresent(it -> {
    log.info("macroStrategyResult: {}", it)
    strategyResult = it
})
// meso
mesoTripleScreenStrategy.getResult(maxPosition, minPosition).ifPresent(it -> {
    log.info("mesoStrategyResult: {}", it)
    strategyResult = it
})
// macro
macroTripleScreenStrategy.getResult(maxPosition, minPosition).ifPresent(it -> {
    log.info("macroStrategyResult: {}", it)
    strategyResult = it
})

//===============================
// check split limit
//===============================
if (strategyResult != null && strategyResult.action == Action.BUY) {
    // 현재 split limit 가 활성화 된 경우 매수 제외
    if (splitBuyLimited) {
        strategyResult = null
    }
}

//===============================
// check profit percentage
//===============================
if (strategyResult != null && strategyResult.action == Action.SELL) {
    // 목표 수익률 이하 매도 제한이 설정된 경우 매도 제외
    if (profitPercentage < sellProfitPercentageThreshold) {
        log.info("profitPercentage under {}", profitPercentage.toPlainString())
        strategyResult = null
    }
}

//================================
// return
//================================
log.info("strategyResult: {}", strategyResult)
tradeAsset.setMessage(tradeAsset.getMessage() + "strategyResult:${strategyResult}")
return strategyResult
