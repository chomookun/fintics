import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.model.StrategyResult
import org.oopscraft.fintics.model.StrategyResult.Action
import org.oopscraft.fintics.trade.Tools
import org.oopscraft.fintics.indicator.*

import java.math.RoundingMode
import java.time.LocalTime

interface Scorable {
    Number getAverage()
}

class Score extends LinkedHashMap<String, BigDecimal> implements Scorable {
    Number getAverage() {
        return this.values().empty ? 0 : this.values().average() as Number
    }
    @Override
    String toString() {
        return this.getAverage() + ' ' + super.toString()
    }
}

interface Analyzable {
    Scorable getMomentumScore()
    Scorable getVolatilityScore()
    Scorable getUnderestimateScore()
    Scorable getOverestimateScore()
}

class Analysis implements Analyzable {
    def pctChangePeriod = 10
    List<Ohlcv> ohlcvs
    Ohlcv ohlcv
    List<Macd> macds
    Macd macd
    List<Dmi> dmis
    Dmi dmi
    List<Rsi> rsis
    Rsi rsi
    List<Atr> atrs
    Atr atr
    List<BollingerBand> bollingerBands
    BollingerBand bollingerBand
    List<Obv> obvs
    Obv obv
    List<ChaikinOscillator> chaikinOscillators
    ChaikinOscillator chaikinOscillator
    List<Cci> ccis
    Cci cci
    List<StochasticSlow> stochasticSlows
    StochasticSlow stochasticSlow

    Analysis(List<Ohlcv> ohlcvs) {
        this.ohlcvs = ohlcvs
        this.ohlcv = this.ohlcvs.first()
        this.macds = Tools.indicators(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
        this.dmis = Tools.indicators(ohlcvs, DmiContext.DEFAULT)
        this.dmi = this.dmis.first()
        this.rsis = Tools.indicators(ohlcvs, RsiContext.DEFAULT)
        this.rsi = rsis.first()
        this.atrs = Tools.indicators(ohlcvs, AtrContext.DEFAULT)
        this.atr = atrs.first()
        this.bollingerBands = Tools.indicators(ohlcvs, BollingerBandContext.DEFAULT)
        this.bollingerBand = bollingerBands.first()
        this.obvs = Tools.indicators(ohlcvs, ObvContext.DEFAULT)
        this.obv = obvs.first()
        this.chaikinOscillators = Tools.indicators(ohlcvs, ChaikinOscillatorContext.DEFAULT)
        this.chaikinOscillator = chaikinOscillators.first()
        this.ccis = Tools.indicators(ohlcvs, CciContext.DEFAULT)
        this.cci = ccis.first()
        this.stochasticSlows = Tools.indicators(ohlcvs, StochasticSlowContext.DEFAULT)
        this.stochasticSlow = stochasticSlows.first()
    }

    @Override
    Scorable getMomentumScore() {
        def score = new Score()
        // macd
        score.macdValueOverSignal = macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
        score.macdValue = macd.value > 0 ? 100 : 0
        // rsi
        score.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
        score.rsiValue = rsi.value > 50 ? 100 : 0
        // cci
        score.cciValueOverSignal = cci.value > cci.signal ? 100 : 0
        score.cciValue = cci.value > 0 ? 100 : 0
        // dmi
        score.dmiPdiPctChange = Tools.pctChange(dmis.take(pctChangePeriod).collect{it.pdi}) > 0 ? 100 : 0
        score.dmiMdiPctChange = Tools.pctChange(dmis.take(pctChangePeriod).collect{it.mdi}) < 0 ? 100 : 0
        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
        // obv
        score.obvValueOverSignal = obv.value > obv.signal ? 100 : 0
        score.obvPctChange = Tools.pctChange(obvs.take(pctChangePeriod).collect{it.value}) > 0 ? 100 : 0
        // chaikin oscillator
        score.chaikinOscillatorValueOverSignal = chaikinOscillator.value > chaikinOscillator.signal ? 100 : 0
        score.chaikinOscillatorValue = chaikinOscillator.value > 0 ? 100 : 0
        // stochastic slow
        score.stochasticSlowKOverD = stochasticSlow.slowK > stochasticSlow.slowD ? 100 : 0
        score.stochasticSlowK = stochasticSlow.slowK > 50 && stochasticSlow.slowD > 50 ? 100 : 0
        // bollinger band
        score.bollinerBandPriceOverMiddle = ohlcv.closePrice > bollingerBand.middle ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getVolatilityScore() {
        def score = new Score()
        // dmi
        score.dmiAdx = dmi.adx > 25 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getUnderestimateScore() {
        def score = new Score()
        // macd
        score.macdValue = macd.value < 0 ? 100 : 0
        score.macdSignal = macd.signal < 0 ? 100 : 0
        // rsi
        score.rsiValue = rsi.value < 50 ? 100 : 0
        score.rsiSignal = rsi.signal < 50 ? 100 : 0
        // cci
        score.cciValue = cci.value < 0 ? 100 : 0
        score.cciSignal = cci.signal < 0 ? 100 : 0
        // stochastic slow
        score.stochasticSlowK = stochasticSlow.slowK < 50 ? 100 : 0
        score.stochasticSlowD = stochasticSlow.slowD < 50 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverestimateScore() {
        def score = new Score()
        // macd
        score.macdValue = macd.value > 0 ? 100 : 0
        score.macdSignal = macd.signal > 0 ? 100 : 0
        // rsi
        score.rsiValue = rsi.value > 50 ? 100 : 0
        score.rsiSignal = rsi.signal > 50 ? 100 : 0
        // cci
        score.cciValue = cci.value > 0 ? 100 : 0
        score.cciSignal = cci.signal > 0 ? 100 : 0
        // stochastic slow
        score.stochasticSlowK = stochasticSlow.slowK > 50 ? 100 : 0
        score.stochasticSlowD = stochasticSlow.slowD > 50 ? 100 : 0
        // return
        return score
    }

    @Override
    String toString() {
        return [
                momentumScore: "${this.getMomentumScore()}",
                volatilityScore: "${this.getVolatilityScore()}",
                underestimateScore: "${this.getUnderestimateScore()}",
                overestimateScore: "${this.getOverestimateScore()}"
        ].toString()
    }
}

//================================
// define
//================================
// config
log.info("variables: {}", variables)
def waveOhlcvType = variables['waveOhlcvType'] as Ohlcv.Type
def waveOhlcvPeriod = variables['waveOhlcvPeriod'] as Integer
def tideOhlcvType = variables['tideOhlcvType'] as Ohlcv.Type
def tideOhlcvPeriod = variables['tideOhlcvPeriod'] as Integer

// default
StrategyResult strategyResult = null
List<Ohlcv> ohlcvs = assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, 1)

// ripple
def analysis = new Analysis(ohlcvs)

// wave
def waveAnalysis = new Analysis(assetProfile.getOhlcvs(waveOhlcvType, waveOhlcvPeriod))

// tide
def tideAnalysis = new Analysis(assetProfile.getOhlcvs(tideOhlcvType, tideOhlcvPeriod))

// multiplier
def multiplier = (tideAnalysis.getMomentumScore().getAverage()/100).setScale(1, RoundingMode.HALF_UP)

// logging
log.info("analysis.momentum: {}", analysis.getMomentumScore());
log.info("waveAnalysis.volatility: {}", waveAnalysis.getVolatilityScore())
log.info("waveAnalysis.underestimate: {}", waveAnalysis.getUnderestimateScore())
log.info("waveAnalysis.overestimate: {}", waveAnalysis.getOverestimateScore())
log.info("tideAnalysis.momentum: {}", tideAnalysis.getMomentumScore())
log.info("multiplier: {}", multiplier)

//================================
// trade
//================================
if (tideAnalysis.getMomentumScore().getAverage() > 50) {
    // buy
    if (analysis.getMomentumScore().getAverage() > 80) {
        // default
        strategyResult = StrategyResult.of(Action.BUY, 1.0 * multiplier, "analysis.momentum: ${analysis.getMomentumScore()}")
        // filter - volatility
        if (waveAnalysis.getVolatilityScore().getAverage() < 50) {
            strategyResult = null
        }
        // filter - overestimate
        if (waveAnalysis.getOverestimateScore().getAverage() > 50) {
            strategyResult = null
        }
    }
    // sell
    if (analysis.getMomentumScore().getAverage() < 20) {
        // default
        strategyResult = StrategyResult.of(Action.SELL, 1.0 * multiplier, "analysis.momentum: ${analysis.getMomentumScore()}")
        // filter - volatility
        if (waveAnalysis.getVolatilityScore().getAverage() < 50) {
            strategyResult = null
        }
        // filter - underestimate
        if (waveAnalysis.getUnderestimateScore().getAverage() > 50) {
            strategyResult = null
        }
    }
}

//================================
// fallback
//================================
// tide direction and momentum
if (tideAnalysis.getMomentumScore().getAverage() < 50) {
    // sell
    if (analysis.getMomentumScore().getAverage() < 20) {
        // default
        strategyResult = StrategyResult.of(Action.SELL, 0.0, "tideAnalysis.momentum: ${tideAnalysis.getMomentumScore()}")
    }
}

//================================
// return
//================================
return strategyResult
