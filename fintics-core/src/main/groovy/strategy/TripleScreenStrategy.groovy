import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.chomookun.fintics.core.ohlcv.model.Ohlcv
import org.chomookun.fintics.core.indicator.Tools
import org.chomookun.fintics.core.indicator.bolangerband.BollingerBandContext
import org.chomookun.fintics.core.indicator.pricechannel.PriceChannelContext

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
        // score.chaikinOscillatorValue = chaikinOscillator.value > 0 ? 100 : 0
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
    TripleScreenStrategy(TradeAsset tradeAsset, Ohlcv.Type tideOhlcvType, int tideOhlcvPeriod, Ohlcv.Type waveOhlcvType, int waveOhlcvPeriod, Ohlcv.Type rippleOhlcvType, int rippleOhlcvPeriod) {
        this.tideAnalyzer = new Analyzer(tradeAsset, tideOhlcvType, tideOhlcvPeriod)
        this.waveAnalyzer = new Analyzer(tradeAsset, waveOhlcvType, waveOhlcvPeriod)
        this.rippleAnalyzer = new Analyzer(tradeAsset, rippleOhlcvType, rippleOhlcvPeriod)
    }

    /**
     * 모멘텀 Score 기준 position 산출
     * @return
     */
    BigDecimal calculatePosition(BigDecimal maxPosition, BigDecimal minPosition) {
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
    StrategyResult getResult(BigDecimal maxPosition, BigDecimal minPosition) {
        StrategyResult strategyResult = null

        // tide 모멘텀 기준 포지션 산출
        def position = this.calculatePosition(maxPosition, minPosition)

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
            waveOversoldThreshold = 75
            waveOverboughtThreshold = 25
        }

        // wave 변동성 구간
        if (waveAnalyzer.getVolatilityScore() >= 50) {
            // wave 과매도 시
            if (waveAnalyzer.getOversoldScore() >= waveOversoldThreshold) {
                // ripple 상승 모멘텀
                if (rippleAnalyzer.getMomentumScore() > 50) {
                    // wave 평균가 기준 매수 포지션
                    def buyPosition = this.adjustAveragePosition(position)
                    strategyResult = StrategyResult.of(Action.BUY, buyPosition, "[WAVE OVERSOLD BUY] ${this.toString()}")
                    // tide 과매수 시 매수 보류
                    if (tideAnalyzer.getOverboughtScore() >= 50) {
                        strategyResult = null
                    }
                }
            }
            // wave 과매수 시
            if (waveAnalyzer.getOverboughtScore() >= waveOverboughtThreshold) {
                // ripple 하락 모멘텀
                if (rippleAnalyzer.getMomentumScore() < 50) {
                    // wave 평균가 기준 매도 포지션
                    def sellPosition = this.adjustAveragePosition(position)
                    strategyResult = StrategyResult.of(Action.SELL, sellPosition, "[WAVE OVERBOUGHT SELL] ${this.toString()}")
                    // tide 과매도 시 매도 보류
                    if (tideAnalyzer.getOversoldScore() >= 50) {
                        strategyResult = null
                    }
                }
            }
        }

        // returns
        return strategyResult
    }

    @Override
    String toString() {
        return """- tide.momentum:${tideAnalyzer.getMomentumScore().getAverage()}
                - tide.oversold:${tideAnalyzer.getOversoldScore().getAverage()}
                - tide.overbought:${tideAnalyzer.getOverboughtScore().getAverage()}
                - wave.volatility:${waveAnalyzer.getVolatilityScore().getAverage()}
                - wave.oversold:${waveAnalyzer.getOversoldScore().getAverage()}
                - wave.overbought:${waveAnalyzer.getOverboughtScore().getAverage()}
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
def tideOhlcvType = variables['tide.ohlcv.type'] as Ohlcv.Type
def tideOhlcvPeriod = variables['tide.ohlcv.period'] as Integer
def waveOhlcvType = variables['wave.ohlcv.type'] as Ohlcv.Type
def waveOhlcvPeriod = variables['wave.ohlcv.period'] as Integer
def rippleOhlcvType = variables['ripple.ohlcv.type'] as Ohlcv.Type
def rippleOhlcvPeriod = variables['ripple.ohlcv.period'] as Integer
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
def tripleScreenStrategy = TripleScreenStrategy.builder()
        .tradeAsset(tradeAsset)
        .tideOhlcvType(tideOhlcvType)
        .tideOhlcvPeriod(tideOhlcvPeriod)
        .waveOhlcvType(waveOhlcvType)
        .waveOhlcvPeriod(waveOhlcvPeriod)
        .rippleOhlcvType(rippleOhlcvType)
        .rippleOhlcvPeriod(rippleOhlcvPeriod)
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
positon:${tripleScreenStrategy.calculatePosition(maxPosition, minPosition)}
${tripleScreenStrategy}
"""
log.info("message: {}", message)
tradeAsset.setMessage(message)

//===============================
// execute strategy
//===============================
strategyResult = tripleScreenStrategy.getResult(maxPosition, minPosition)

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
