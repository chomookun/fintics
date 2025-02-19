import groovy.transform.ToString
import org.jetbrains.annotations.NotNull
import org.oopscraft.fintics.indicator.*
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.trade.Tool

import java.time.LocalTime

interface Scorable extends Comparable<Number> {
    Number getAverage()
    default int compareTo(@NotNull Number o) {
        return Double.compare(this.getAverage().doubleValue(), o.doubleValue())
    }
    default int compareTo(@NotNull Scorable o) {
        return Double.compare(this.getAverage().doubleValue(), o.getAverage().doubleValue())
    }
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
    Number getAverage() {
        return this.values().collect{it.getAverage()}.average() as Number
    }
    @Override
    String toString() {
        return super.toString()
    }
}

interface Analyzable {
    Scorable getMomentumScore()
    Scorable getOversoldScore()
    Scorable getOverboughtScore()
}

class Analysis implements Analyzable {
    List<Ohlcv> ohlcvs
    List<Ema> fastEmas
    List<Ema> slowEmas
    List<BollingerBand> bollingerBands
    List<Macd> macds
    List<Rsi> rsis
    List<Dmi> dmis
    List<Obv> obvs
    List<ChaikinOscillator> chaikinOscillators

    Analysis(List<Ohlcv> ohlcvs) {
        this.ohlcvs = ohlcvs
        this.fastEmas = Tool.calculate(ohlcvs, EmaContext.of(10))
        this.slowEmas = Tool.calculate(ohlcvs, EmaContext.of(20))
        this.bollingerBands = Tool.calculate(ohlcvs, BollingerBandContext.DEFAULT)
        this.macds = Tool.calculate(ohlcvs, MacdContext.DEFAULT)
        this.rsis = Tool.calculate(ohlcvs, RsiContext.DEFAULT)
        this.dmis = Tool.calculate(ohlcvs, DmiContext.DEFAULT)
        this.obvs = Tool.calculate(ohlcvs, ObvContext.DEFAULT)
        this.chaikinOscillators = Tool.calculate(ohlcvs, ChaikinOscillatorContext.DEFAULT)
    }

    @Override
    Scorable getMomentumScore() {
        def score = new Score()
        // define
        def ohlcv = this.ohlcvs.first()
        // ema
        def fastEma = this.fastEmas.first()
        def slowEma = this.slowEmas.first()
        score.emaValueFastOverSlow = fastEma.value > slowEma.value ? 100 : 0
        // bollinger band
        def bollingerBand = this.bollingerBands.first()
        score.bollingerBandPriceOverMiddle = ohlcv.closePrice > bollingerBand.middle ? 100 : 0
        // macd
        def macd = this.macds.first()
        score.macdValue = macd.value > 0 ? 100 : 0
        score.macdValueOverSignal = macd.value > 0 && macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
        // rsi
        def rsi = this.rsis.first()
        score.rsiValue = rsi.value > 50 ? 100 : 0
        score.rsiValueOverSignal = rsi.value > 50 && rsi.value > rsi.signal ? 100 : 0
        // dmi
        def dmi = this.dmis.first()
        score.dmiPdiOverMdi = dmi.pdi > dmi.mdi ? 100 : 0
        score.dmiAdx = dmi.adx > 25 && dmi.pdi > dmi.mdi ? 100 : 0
        // obv
        def obv = this.obvs.first()
        def obvValuePctChange = Tool.pctChange(this.obvs.take(10).collect{it.value})
        score.obvValuePctChange = obvValuePctChange > 0 ? 100 : 0
        score.obvValueOverSignal = obv.value > obv.signal ? 100 : 0
        // co
        def chaikinOscillator = this.chaikinOscillators.first()
        score.coValue = chaikinOscillator.value > 0 ? 100 : 0
        score.coValueOverSignal = chaikinOscillator.value > chaikinOscillator.signal ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOversoldScore() {
        def score = new Score()
        // rsi
        def rsi = this.rsis.first()
        def rsiValues = this.rsis.collect{it.value}
        def rsiValueOversold = rsiValues.take(10).any{it <= 30}
        score.rsiValue = rsiValueOversold && rsi.value > 30 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverboughtScore() {
        def score = new Score()
        // rsi
        def rsi = this.rsis.first()
        def rsiValues = this.rsis.collect{it.value}
        def rsiValueOverbought = rsiValues.take(10).any{it >= 70}
        score.rsiValue = rsiValueOverbought && rsi.value < 70 ? 100 : 0
        // return
        return score
    }

    @Override
    String toString() {
        return [momentumScore: "${this.getMomentumScore().getAverage()}",
                oversoldScore: "${this.getOversoldScore().getAverage()}",
                overboughtScore: "${this.getOverboughtScore().getAverage()}"
        ].toString()
    }
}

class AnalysisGroup extends LinkedHashMap<String,Analysis> implements Analyzable {

    AnalysisGroup(Map map) {
        super(map)
    }

    @Override
    Scorable getMomentumScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getMomentumScore())}
        return scoreGroup
    }

    @Override
    Scorable getOversoldScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getOversoldScore())}
        return scoreGroup
    }

    @Override
    Scorable getOverboughtScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getOverboughtScore())}
        return scoreGroup
    }

    @Override
    String toString() {
        return [momentumScore: "${this.getMomentumScore().getAverage()}",
                oversoldScore: "${this.getOversoldScore().getAverage()}",
                overboughtScore: "${this.getOverboughtScore().getAverage()}"
        ].toString() + super.toString()
    }
}

//==========================
// defines
//==========================
def hold = null

// default
List<Ohlcv> ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def priceZScore = Tool.zScore(ohlcvs.take(20).collect{it.closePrice})
if (priceZScore.abs() < 1.0) {
    log.info("skip - price z-score is under 1.0")
    return null
}
def analysis = new Analysis(ohlcvs)

// wave
def waveAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 5))

// tide
def tideAnalysis = new AnalysisGroup(
        hourly: new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 60)),
        daily: new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY, 1))
)

//=============================
// logging
//=============================
log.info("analysis: {}", analysis)
log.info("waveAnalysis: {}", waveAnalysis)
log.info("tideAnalysis: {}", tideAnalysis)

//=============================
// trade
//=============================
// buy
if (analysis.getMomentumScore() >= 75) {
    // overbought
    if (waveAnalysis.getOversoldScore() >= 50) {
        log.info("waveAnalysis.oversoldScore: {}", waveAnalysis.getOversoldScore())
        hold = 1
    }
}
// sell
if (analysis.getMomentumScore() <= 25) {
    // oversold
    if (waveAnalysis.getOverboughtScore() >= 50) {
        log.info("waveAnalysis.overboughtScore: {}", waveAnalysis.getOverboughtScore())
        hold = 0
    }
}

//==============================
// fallback
//==============================
if (tideAnalysis.getMomentumScore() <= 25) {
    log.info("fallback - tideMomentumScore under {}", tideAnalysis.getMomentumScore())
    hold = 0
}

//==============================
// return
//==============================
return hold

