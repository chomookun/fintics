import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.chomookun.fintics.core.ohlcv.indicator.ema.Ema
import org.chomookun.fintics.core.ohlcv.indicator.ema.EmaContext
import org.chomookun.fintics.core.ohlcv.model.Ohlcv
import org.chomookun.fintics.core.ohlcv.indicator.Tools
import org.chomookun.fintics.core.ohlcv.indicator.bolangerband.BollingerBandContext
import org.chomookun.fintics.core.ohlcv.indicator.pricechannel.PriceChannelContext

import java.math.RoundingMode

/**
 * score
 */
class Score extends LinkedHashMap<String, BigDecimal> implements Comparable<Score> {
    Number getAverage() {
        return this.values().empty ? 0 : this.values().average() as Number
    }
    @Override
    int compareTo(Score o) {
        return Double.compare(this.getAverage().doubleValue(), o.getAverage().doubleValue())
    }
    int compareTo(Number o) {
        return Double.compare(this.getAverage().doubleValue(), o.doubleValue())
    }
    @Override
    String toString() {
        return this.getAverage() + ' ' + super.toString()
    }
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
        // score.rsiValue = rsi.value > 50 ? 100 : 0
        score.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
        // cci
        score.cciValueOverSignal = cci.value > cci.signal ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValueOverSignal = chaikinOscillator.value > chaikinOscillator.signal ? 100 : 0
        // stochastic slow
        score.stochasticSlowKOverD = stochasticSlow.slowK > stochasticSlow.slowD ? 100 : 0
        score.stochasticSlowOverbought = stochasticSlow.slowK > 50 ? 100 : 0
        // williams r
        score.williamsR = williamsR.value > -50 ? 100 : 0
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
        score.dmiAdx = dmis.take(2)*.adx.average() >= 20 ? 100 : 0
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
        score.rsi = rsis.take(2)*.value.average() <= 30 ? 100 : 0
        // cci: -100 이하인 경우 과매도 판정
        score.cci = ccis.take(2)*.value.average() <= -100 ? 100 : 0
        // stochastic slow: 20 이하인 경우 과매도 판정
        score.stochasticSlow = stochasticSlows.take(2)*.slowK.average() <= 20 ? 100 : 0
        // williams r: -80 이하인 경우 과매도 판정
        score.williamsR = williamsRs.take(2)*.value.average() <= -80 ? 100 : 0
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
        score.rsi = rsis.take(2)*.value.average() >= 70 ? 100 : 0
        // cci: 100 이상인 경우 과매수 판정
        score.cci = ccis.take(2)*.value.average() >= 100 ? 100 : 0
        // stochastic slow: 80 이상인 경우 과매수 판정
        score.stochasticSlow = stochasticSlows.take(2)*.slowK.average() >= 80 ? 100 : 0
        // williams r: -20 이상인 경우 과매수 판정
        score.williamsR = williamsRs.take(2)*.value.average() >= -20 ? 100 : 0
        // return
        return score
    }

    @Override
    String toString() {
        return [
                momentumScore: "${this.getMomentumScore()}",
                volatilityScore: "${this.getVolatilityScore()}",
                oversoldScore: "${this.getOversoldScore()}",
                overboughtScore: "${this.getOverboughtScore()}"
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
     * 과매도 임계치 - tide 모멘텀 가중치 기준 가변 적용
     * @return
     */
    BigDecimal getWaveOversoldThreshold() {
        def value = (100 - tideAnalyzer.getMomentumScore().getAverage()) as BigDecimal
        return value.max(25).min(75)
    }

    /**
     * 과매수 임계치 - tide 모멘텀 가중치 기준 가변 적용
     * @return
     */
    BigDecimal getWaveOverboughtThreshold() {
        def value = (0 + tideAnalyzer.getMomentumScore().getAverage()) as BigDecimal
        return value.max(25).min(75)
    }

    /**
     * is wave oversold
     * @return whether wave is oversold or not
     */
    boolean isWaveOversold() {
        if (waveAnalyzer.getVolatilityScore() >= 50 && waveAnalyzer.getOversoldScore() >= this.getWaveOversoldThreshold()) {
            return true
        }
        return false
    }

    /**
     * is wave overbought
     * @return whether wave is overbought or not
     */
    boolean isWaveOverbought() {
        if (waveAnalyzer.getVolatilityScore() >= 50 && waveAnalyzer.getOverboughtScore() >= this.getWaveOverboughtThreshold()) {
            return true
        }
        return false
    }

    /**
     * is ripple bullish
     * @return whether ripple is bullish or not
     */
    boolean isRippleBullish() {
        if (rippleAnalyzer.getVolatilityScore() >= 50 && rippleAnalyzer.getMomentumScore() >= 70) {
            return true
        }
        return false
    }

    /**
     * is ripple bearish
     * @return whether ripple is bearish or not
     */
    boolean isRippleBearish() {
        if (rippleAnalyzer.getVolatilityScore() >= 50 && rippleAnalyzer.getMomentumScore() <= 30) {
            return true
        }
        return false
    }

    /**
     * 모멘텀 Score 기준 position 산출
     * @return
     */
    BigDecimal calculatePosition(BigDecimal minPosition, BigDecimal maxPosition) {
        // 모멘텀 점수 계산 (momentum score 1%당 포지션 1% 증가)
        def positionScore = tideAnalyzer.getMomentumScore().getAverage() as BigDecimal
        // clamp 0~100
        positionScore = positionScore.max(BigDecimal.ZERO).min(new BigDecimal("100"))
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
     * adjust average position
     * @param position
     * @return average position
     */
    BigDecimal adjustAveragePosition(BigDecimal position) {
        def averagePrice = waveAnalyzer.getAverageClose()
        def currentPrice = waveAnalyzer.getCurrentClose()
        def averageWeight = averagePrice/currentPrice as BigDecimal
        def averagePosition = ((position * averageWeight) as BigDecimal)
                .setScale(2, RoundingMode.HALF_UP)
        return averagePosition
    }

    /**
     * gets strategy result
     * @return strategy result
     */
    Optional<StrategyResult> getResult(BigDecimal position) {
        StrategyResult strategyResult = null

        // wave 과매도 시
        if (this.isWaveOversold()) {
            // ripple 상승 모멘텀
            if (this.isRippleBullish()) {
                // wave 평균가 기준 매수 포지션
                def buyPosition = this.adjustAveragePosition(position)
                strategyResult = StrategyResult.of(Action.BUY, buyPosition, "WAVE OVERSOLD BUY[position:${position}, buyPosition:${buyPosition}]: ${this.toString()}")
            }
        }
        // wave 과매수 시
        if (this.isWaveOverbought()) {
            // ripple 하락 모멘텀
            if (this.isRippleBearish()) {
                // wave 평균가 기준 매도 포지션
                def sellPosition = this.adjustAveragePosition(position)
                strategyResult = StrategyResult.of(Action.SELL, sellPosition, "WAVE OVERBOUGHT SELL[position:${position},sellPosition:${sellPosition}]: ${this.toString()}")
            }
        }
        // returns
        return Optional.ofNullable(strategyResult)
    }

    @Override
    String toString() {
        return """- name: ${this.name}
                - tide.momentum:${tideAnalyzer.getMomentumScore().getAverage()}
                - tide.oversold:${tideAnalyzer.getOversoldScore().getAverage()}
                - tide.overbought:${tideAnalyzer.getOverboughtScore().getAverage()}
                - wave.volatility:${waveAnalyzer.getVolatilityScore().getAverage()} (adx:${waveAnalyzer.dmis.take(3).collect{it.adx.intValue()}})
                - wave.oversold(threshold):${waveAnalyzer.getOversoldScore().getAverage()}(${this.getWaveOversoldThreshold()})
                - wave.overbought(threshold):${waveAnalyzer.getOverboughtScore().getAverage()}(${this.getWaveOverboughtThreshold()})
                - ripple.volatility:${rippleAnalyzer.getVolatilityScore().getAverage()} (adx:${rippleAnalyzer.dmis.take(3).collect{it.adx.intValue()}})
                - ripple.momentum:${rippleAnalyzer.getMomentumScore().getAverage()}"""
                .split('\n').collect { it.trim() }.join('\n')
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
 * calculate channel
 * @param ohlcvs
 * @param period
 * @return channel
 */
static def calculateChannel(List<Ohlcv> ohlcvs, int period) {
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

//===============================
// config
//===============================
log.info("variables: {}", variables)
// micro strategy
def microTideOhlcvType = variables['micro.tide.ohlcv.type'] as Ohlcv.Type
def microTideOhlcvPeriod = variables['micro.tide.ohlcv.period'] as Integer
def microWaveOhlcvType = variables['micro.wave.ohlcv.type'] as Ohlcv.Type
def microWaveOhlcvPeriod = variables['micro.wave.ohlcv.period'] as Integer
def microRippleOhlcvType = variables['micro.ripple.ohlcv.type'] as Ohlcv.Type
def microRippleOhlcvPeriod = variables['micro.ripple.ohlcv.period'] as Integer
// meso strategy
def mesoTideOhlcvType = variables['meso.tide.ohlcv.type'] as Ohlcv.Type
def mesoTideOhlcvPeriod = variables['meso.tide.ohlcv.period'] as Integer
def mesoWaveOhlcvType = variables['meso.wave.ohlcv.type'] as Ohlcv.Type
def mesoWaveOhlcvPeriod = variables['meso.wave.ohlcv.period'] as Integer
def mesoRippleOhlcvType = variables['meso.ripple.ohlcv.type'] as Ohlcv.Type
def mesoRippleOhlcvPeriod = variables['meso.ripple.ohlcv.period'] as Integer
// macro strategy
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
def splitPeriod = 200
def splitSize = 10
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
// micro strategy
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
// meso strategy
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
// macro strategy
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
def channel =  calculateChannel(tradeAsset.getOhlcvs(Ohlcv.Type.DAILY, 1), splitPeriod)
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
def microPosition = microTripleScreenStrategy.calculatePosition(basePosition, 1.0)
def mesoPosition  = mesoTripleScreenStrategy.calculatePosition(basePosition, 1.0)
def macroPosition = macroTripleScreenStrategy.calculatePosition(basePosition, 1.0)
// effective position (상위 scale position 과의 평균값)
def microEffectivePosition = ([microPosition, mesoPosition, macroPosition].average() as BigDecimal).setScale(2, RoundingMode.HALF_UP)
def mesoEffectivePosition = ([mesoPosition, macroPosition].average() as BigDecimal).setScale(2, RoundingMode.HALF_UP)
def macroEffectivePosition = macroPosition

//===============================
// message
//===============================
def message = """
splitSize:${splitSize}, splitIndex:${splitIndex}
splitLimits:${splitLimitPrices}
splitLimitPrice:${splitLimitPrice}, splitBuyLimited:${splitBuyLimited}
sellProfitPercentageThreshold:${sellProfitPercentageThreshold}
position:(micro:${microPosition}, meso:${mesoPosition}, macro:${macroPosition})
effectivePosition:(micro:${microEffectivePosition}, meso:${mesoEffectivePosition}, macro:${macroEffectivePosition})
microTripleScreen:${microTripleScreenStrategy}
mesoTripleScreen:${mesoTripleScreenStrategy}
macroTripleScreen:${macroTripleScreenStrategy}
"""
log.info("message: {}", message)
tradeAsset.setMessage(message)

//===================================================================================
// execute strategy
//===================================================================================
// micro strategy (overrides meso, macro)
microTripleScreenStrategy.getResult(microEffectivePosition).ifPresent {
    log.info("micro strategy result: {}", it)
    strategyResult = it
}
// meso strategy (overrides macro)
mesoTripleScreenStrategy.getResult(mesoEffectivePosition).ifPresent {
    log.info("meso strategy result: {}", it)
    strategyResult = it
}
// macro strategy
macroTripleScreenStrategy.getResult(macroEffectivePosition).ifPresent {
    log.info("macro strategy result: {}", it)
    strategyResult = it
}

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
