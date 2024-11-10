import groovy.transform.ToString
import org.jetbrains.annotations.NotNull
import org.oopscraft.fintics.indicator.*
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.model.TradeAsset
import org.oopscraft.fintics.strategy.StrategyResult
import org.oopscraft.fintics.strategy.StrategyResult.Action
import org.oopscraft.fintics.trade.Tools

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

        // return
        return channel
    }

    /**
     * gets momentum score
     * @return momentum score
     */
    Score getMomentumScore() {
        def score = new Score()
        // sma
        score.smaPriceOverValue = ohlcv.close > sma.value ? 100 : 0
        // ema
        score.emaPriceOverValue = ohlcv.close > ema.value ? 100 : 0
        // macd
        score.macdValue = macd.value > 0 ? 100 : 0
        // bollinger band
        score.bollingerBandPriceOverMiddle = ohlcv.close > bollingerBand.middle ? 100 : 0
        // rsi
        score.rsiValue = rsi.value > 50 ? 100 : 0
        // cci
        score.cciValue = cci.value > 0 ? 100 : 0
        // dmi
        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValue = chaikinOscillator.value > 0 ? 100 : 0
        // stochastic slow
        score.stochasticSlowK = stochasticSlow.slowK > 50 ? 100 : 0
        // williams r
        score.williamsRValue = williamsR.value > -50 ? 100 : 0
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
                overboughtScore: "${this.getOverboughtScore()}"
        ].toString()
    }
}

//===============================
// config
//===============================
StrategyResult strategyResult = null
log.info("variables: {}", variables)
def orderEnabled = Boolean.parseBoolean(variables['orderEnabled'])
def tideOhlcvType = Ohlcv.Type.valueOf(variables['tideOhlcvType'])
def tideOhlcvPeriod = Integer.parseInt(variables['tideOhlcvPeriod'])
def waveOhlcvType = Ohlcv.Type.valueOf(variables['waveOhlcvType'])
def waveOhlcvPeriod = Integer.parseInt(variables['waveOhlcvPeriod'])
def rippleOhlcvType = Ohlcv.Type.valueOf(variables['rippleOhlcvType'])
def rippleOhlcvPeriod = Integer.parseInt(variables['rippleOhlcvPeriod'])
def basePosition = new BigDecimal(variables['basePosition'])
def sellProfitPercentageThreshold = new BigDecimal(variables['sellProfitPercentageThreshold'])
def splitIndex = Integer.parseInt(variables['splitIndex'] ?: '-1')

//===============================
// analysis
//===============================
def tideAnalyzer = new Analyzer(tradeAsset, tideOhlcvType, tideOhlcvPeriod)
def waveAnalyzer = new Analyzer(tradeAsset, waveOhlcvType, waveOhlcvPeriod)
def rippleAnalyzer = new Analyzer(tradeAsset, rippleOhlcvType, rippleOhlcvPeriod)

//===============================
// split ranges
//===============================
def splitPeriod = 100
def splitSize = 5
def dailyAnalyzer = new Analyzer(tradeAsset, Ohlcv.Type.DAILY, 1)
def channel =  dailyAnalyzer.getChannel(splitPeriod)
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
    if (rippleAnalyzer.getCurrentClose() > splitLimitPrice) {
        splitBuyLimited = true
    }
}

//===============================
// position
//===============================
def positionScore = (tideAnalyzer.getMomentumScore().getAverage() - 50).max(0)*2
def positionPerScore = (1.0 -basePosition)/100
def position = basePosition + (positionPerScore * positionScore) as BigDecimal

//===============================
// profit percentage
//===============================
def profitPercentage = balanceAsset?.getProfitPercentage() ?: 0.0

//===============================
// message
//===============================
def message = """
channel:upper=${channel.upper}, lower=${channel.lower}
splitLimits:${splitLimitPrices}
splitIndex:${splitIndex}, splitLimit:${splitLimitPrice}
splitBuyLimited:${splitBuyLimited}
position:${position.toPlainString()}
tide.momentum:${tideAnalyzer.getMomentumScore().toString()}
tide.volatility:${tideAnalyzer.getVolatilityScore().toString()}
tide.oversold:${tideAnalyzer.getOversoldScore().toString()}
tide.overbought:${tideAnalyzer.getOverboughtScore().toString()}
- rsi:${tideAnalyzer.rsi.value}|sto:${tideAnalyzer.stochasticSlow.slowK}|cci:${tideAnalyzer.cci.value}|wil:${tideAnalyzer.williamsR.value}
wave.momentum:${waveAnalyzer.getMomentumScore().toString()}
wave.volatility:${waveAnalyzer.getVolatilityScore().toString()}
wave.oversold:${waveAnalyzer.getOversoldScore().toString()}
wave.overbought:${waveAnalyzer.getOverboughtScore().toString()}
- rsi:${waveAnalyzer.rsi.value}|sto:${waveAnalyzer.stochasticSlow.slowK}|cci:${waveAnalyzer.cci.value}|wil:${waveAnalyzer.williamsR.value}
ripple.momentum:${rippleAnalyzer.getMomentumScore().toString()}
"""
log.info("message: {}", message)
tradeAsset.setMessage(message)

//===============================
// trade
//===============================
// wave volatility
if (waveAnalyzer.getVolatilityScore() > 50) {
    // wave oversold
    if (waveAnalyzer.getOversoldScore() > 50) {
        // ripple bullish momentum
        if (rippleAnalyzer.getMomentumScore() > 50) {
            // buy
            def buyAveragePosition = waveAnalyzer.adjustAveragePosition(position)
            strategyResult = StrategyResult.of(Action.BUY, buyAveragePosition, "[WAVE OVERSOLD BUY] " + message)
        }
    }
    // wave overbought
    if (waveAnalyzer.getOverboughtScore() > 50) {
        // ripple bearish momentum
        if (rippleAnalyzer.getMomentumScore() < 50) {
            // sell
            def sellAveragePosition = waveAnalyzer.adjustAveragePosition(position)
            strategyResult = StrategyResult.of(Action.SELL, sellAveragePosition, "[WAVE OVERBOUGHT SELL] " + message)
        }
    }
}
// tide volatility
if (tideAnalyzer.getVolatilityScore() > 50) {
    // tide oversold
    if (tideAnalyzer.getOversoldScore() > 50) {
        // wave bullish momentum
        if (waveAnalyzer.getMomentumScore() > 50) {
            // buy
            def buyAveragePosition = tideAnalyzer.adjustAveragePosition(position)
            strategyResult = StrategyResult.of(Action.BUY, buyAveragePosition, "[TIDE OVERSOLD BUY] " + message)

        }
    }
    // tide overbought
    if (tideAnalyzer.getOverboughtScore() > 50) {
        // wave bearish momentum
        if (waveAnalyzer.getMomentumScore() < 50) {
            // sell
            def sellAveragePosition = tideAnalyzer.adjustAveragePosition(position)
            strategyResult = StrategyResult.of(Action.SELL, sellAveragePosition, "[TIDE OVERBOUGHT SELL] " + message)
        }
    }
}

//===============================
// check sell option
//===============================
if (strategyResult != null && strategyResult.action == Action.BUY) {
    // 현재 split limit 가 활성화 된 경우 매수 제외
    if (splitBuyLimited) {
        strategyResult = null
    }
}

//===============================
// check sell option
//===============================
if (strategyResult != null && strategyResult.action == Action.SELL) {
    // 목표 수익률 이하 매도 제한이 설정된 경우 매도 제외
    if (profitPercentage < sellProfitPercentageThreshold) {
        log.info("profitPercentage under {}", profitPercentage.toPlainString())
        strategyResult = null
    }
}

//================================
// order enabled
//================================
// orderEnabled 설정 이 true 가 아닐 경우는 실제 주문 제외
log.info("orderEnabled: {}", orderEnabled)
if (!orderEnabled) {
    log.info("override strategyResult to be null")
    strategyResult = null
}

//================================
// return
//================================
return strategyResult
