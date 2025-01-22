import org.chomookun.fintics.indicator.Atr
import org.chomookun.fintics.indicator.BollingerBand
import org.chomookun.fintics.indicator.Cci
import org.chomookun.fintics.indicator.ChaikinOscillator
import org.chomookun.fintics.indicator.Dmi
import org.chomookun.fintics.indicator.Macd
import org.chomookun.fintics.indicator.MacdContext
import org.chomookun.fintics.indicator.Obv
import org.chomookun.fintics.indicator.Rsi
import org.chomookun.fintics.indicator.StochasticSlow
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.oopscraft.fintics.trade.Tool
import org.oopscraft.fintics.indicator.*

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
        return super.toString()
    }
}

class ScoreGroup extends LinkedHashMap<String, Scorable> implements Scorable {
    @Override
    Number getAverage() {
        return this.values().collect{it.getAverage()}.average() as Number
    }
}

interface Analyzable {
    Scorable getBullishScore()
    Scorable getBearishScore()
}

class Analysis implements Analyzable {
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
        this.macds = Tool.indicators(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
//        this.dmis = Tool.indicators(ohlcvs, DmiContext.DEFAULT)
//        this.dmi = this.dmis.first()
//        this.rsis = Tool.indicators(ohlcvs, RsiContext.DEFAULT)
//        this.rsi = rsis.first()
//        this.atrs = Tool.indicators(ohlcvs, AtrContext.DEFAULT)
//        this.atr = atrs.first()
//        this.bollingerBands = Tool.indicators(ohlcvs, BollingerBandContext.DEFAULT)
//        this.bollingerBand = bollingerBands.first()
//        this.obvs = Tool.indicators(ohlcvs, ObvContext.DEFAULT)
//        this.obv = obvs.first()
//        this.chaikinOscillators = Tool.indicators(ohlcvs, ChaikinOscillatorContext.DEFAULT)
//        this.chaikinOscillator = chaikinOscillators.first()
//        this.ccis = Tool.indicators(ohlcvs, CciContext.DEFAULT)
//        this.cci = ccis.first()
//        this.stochasticSlows = Tool.indicators(ohlcvs, StochasticSlowContext.DEFAULT)
//        this.stochasticSlow = stochasticSlows.first()
    }

    @Override
    Scorable getBullishScore() {
        def score = new Score()
        // macd
        score.macdValueOverSignal = macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
//        // bollinger band
//        score.bollingerBandPriceOverMiddle = ohlcv.closePrice > bollingerBand.middle ? 100 : 0
//        // cci
//        score.cciValueOverSignal = cci.value > cci.signal ? 100 : 0
//        // rsi
//        score.rsiValueOverSignal = rsi.value > rsi.signal ? 100 : 0
//        // dmi
//        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
//        // obv
//        score.obvValueOverSignal = obv.value > obv.signal ? 100 : 0
//        // chaikin oscillator
//        score.chaikinOscillatorValueOverSignal = chaikinOscillator.value > chaikinOscillator.signal ? 100 : 0
//        // stochastic slow
//        score.stochasticSlowKUnderD = stochasticSlow.slowK > stochasticSlow.slowD ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getBearishScore() {
        def score = new Score()
        // macd
        score.macdValueUnderSignal = macd.value < macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator < 0 ? 100 : 0
//        // bollinger band
//        score.bollingerBandPriceUnderMiddle = ohlcv.closePrice < bollingerBand.middle ? 100 : 0
//        // cci
//        score.cciValueUnderSignal = cci.value < cci.signal ? 100 : 0
//        // rsi
//        score.rsiValueUnderSignal = rsi.value < rsi.signal ? 100 : 0
//        // dmi
//        score.dmiPidUnderMdi = dmi.pdi < dmi.mdi ? 100 : 0
//        // obv
//        score.obvValueUnderSignal = obv.value < obv.signal ? 100 : 0
//        // chaikin oscillator
//        score.chaikinOscillatorValueUnderSignal = chaikinOscillator.value < chaikinOscillator.signal ? 100 : 0
//        // stochastic slow
//        score.stochasticSlowKUnderD = stochasticSlow.slowK < stochasticSlow.slowD ? 100 : 0
        // return
        return score
    }

    @Override
    String toString() {
        return [
                bullishScore: "${this.getBullishScore()}",
                bearishScore: "${this.getBearishScore()}",
        ].toString()
    }
}

class AnalysisGroup extends LinkedHashMap<String, Analyzable> implements Analyzable {

    @Override
    Scorable getBullishScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getBullishScore())}
        return scoreGroup
    }

    @Override
    Scorable getBearishScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getBearishScore())}
        return scoreGroup
    }

}

//================================
// define
//================================
// config
def minuteOhlcvPeriod = variables['minuteOhlcvPeriod'] as Integer
log.info("minuteOhlcvPeriod: {}", minuteOhlcvPeriod)

// default
def hold = null
List<Ohlcv> ohlcvs = assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def priceZScore = Tool.zScore(ohlcvs.take(10).collect{it.getClose})

// analysis
def analysis = new AnalysisGroup(
        minute: new Analysis(assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, minuteOhlcvPeriod)),
        hourly: new Analysis(assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, 60)),
        daily: new Analysis(assetProfile.getOhlcvs(Ohlcv.Type.DAILY, 1))
)

//================================
// trade
//================================
// buy
if (priceZScore > 1.5) {
    if (analysis.getBullishScore().getAverage() > 75) {
        hold = 1
    }
}

// sell
if (priceZScore < -1.5) {
    if (analysis.getBearishScore().getAverage() > 75) {
        hold = 0
    }
}

//================================
// return
//================================
return hold
