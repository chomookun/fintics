import org.chomookun.fintics.indicator.BollingerBand
import org.chomookun.fintics.indicator.BollingerBandContext
import org.chomookun.fintics.indicator.Cci
import org.chomookun.fintics.indicator.CciContext
import org.chomookun.fintics.indicator.Dmi
import org.chomookun.fintics.indicator.DmiContext
import org.chomookun.fintics.indicator.Ema
import org.chomookun.fintics.indicator.Macd
import org.chomookun.fintics.indicator.MacdContext
import org.chomookun.fintics.indicator.Rsi
import org.chomookun.fintics.indicator.RsiContext
import org.chomookun.fintics.indicator.StochasticSlow
import org.chomookun.fintics.indicator.StochasticSlowContext
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.strategy.StrategyResult
import org.chomookun.fintics.strategy.StrategyResult.Action
import org.chomookun.fintics.trade.Tools
import org.oopscraft.fintics.indicator.*

import java.math.RoundingMode

interface Scorable {
    Number getAverage()
}

class ScoreGroup extends LinkedHashMap<String, Scorable> implements Scorable {
    @Override
    Number getAverage() {
        return this.values().collect{it.getAverage()}.average() as Number
    }
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
    Scorable getDirectionScore()
    Scorable getMomentumScore()
    Scorable getVolatilityScore()
    Scorable getOversoldScore()
    Scorable getOverboughtScore()
}

class Analysis implements Analyzable {
    def period = 20
    List<Ohlcv> ohlcvs
    Ohlcv ohlcv
    List<Ema> emas
    Ema ema
    List<Macd> macds
    Macd macd
    List<BollingerBand> bollingerBands
    BollingerBand bollingerBand
    List<Dmi> dmis
    Dmi dmi
    List<Rsi> rsis
    Rsi rsi
    List<Cci> ccis
    Cci cci
    List<StochasticSlow> stochasticSlows
    StochasticSlow stochasticSlow

    Analysis(List<Ohlcv> ohlcvs) {
        this.ohlcvs = ohlcvs
        this.ohlcv = this.ohlcvs.first()
        this.macds = Tools.indicators(ohlcvs, MacdContext.DEFAULT)
        this.macd = this.macds.first()
        this.bollingerBands = Tools.indicators(ohlcvs, BollingerBandContext.DEFAULT)
        this.bollingerBand = bollingerBands.first()
        this.dmis = Tools.indicators(ohlcvs, DmiContext.DEFAULT)
        this.dmi = this.dmis.first()
        this.rsis = Tools.indicators(ohlcvs, RsiContext.DEFAULT)
        this.rsi = rsis.first()
        this.ccis = Tools.indicators(ohlcvs, CciContext.DEFAULT)
        this.cci = ccis.first()
        this.stochasticSlows = Tools.indicators(ohlcvs, StochasticSlowContext.DEFAULT)
        this.stochasticSlow = stochasticSlows.first()
    }

    @Override
    Scorable getDirectionScore() {
        def score = new Score()
        // macd
        score.macdValueOverSignal = macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 100 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getMomentumScore() {
        def score = new Score()
        // macd
        score.macdValue = macd.value > 0 ? 100 : 0
        // bollinger band
        score.bollingerBandPriceOverMiddle = ohlcv.getClose > bollingerBand.middle ? 100 : 0
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
    Scorable getOversoldScore() {
        def score = new Score()
        // rsi
        score.rsiValue = rsi.value < 30 || rsi.signal < 30 ? 100 : 0
        // cci
        score.cciValue = cci.value < -100 || rsi.signal < -100 ? 100 : 0
        // stochastic slow
        score.stochasticSlowK = stochasticSlow.slowK < 20 || stochasticSlow.slowD < 20 ? 100 : 0
        // return
        return score
    }

    @Override
    Scorable getOverboughtScore() {
        def score = new Score()
        // rsi
        score.rsiValue = rsi.value > 70 || rsi.signal > 70 ? 100 : 0
        // cci
        score.cciValue = cci.value > 100 || cci.signal > 100 ? 100 : 0
        // stochastic slow
        score.stochasticSlowK = stochasticSlow.slowK > 80 || stochasticSlow.slowD > 80 ? 100 : 0
        // return
        return score
    }

    @Override
    String toString() {
        return [
                directionScore: "${this.getDirectionScore()}",
                momentumScore: "${this.getMomentumScore()}",
                volatilityScore: "${this.getVolatilityScore()}",
                oversoldScore: "${this.getOversoldScore()}",
                overboughtScore: "${this.getOverboughtScore()}"
        ].toString()
    }
}

class AnalysisGroup extends LinkedHashMap<String, Analyzable> implements Analyzable {

    @Override
    Scorable getDirectionScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getDirectionScore())}
        return scoreGroup
    }

    @Override
    Scorable getMomentumScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getMomentumScore())}
        return scoreGroup
    }

    @Override
    Scorable getVolatilityScore() {
        def scoreGroup = new ScoreGroup()
        this.each{it -> scoreGroup.put(it.key, it.value.getVolatilityScore())}
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

}

//================================
// define
//================================
// config
log.info("variables: {}", variables)
def ohlcvPeriod = variables['ohlcvPeriod'] as Integer
def waveOhlcvType = variables['waveOhlcvType'] as Ohlcv.Type
def waveOhlcvPeriod = variables['waveOhlcvPeriod'] as Integer
def tideOhlcvType = variables['tideOhlcvType'] as Ohlcv.Type
def tideOhlcvPeriod = variables['tideOhlcvPeriod'] as Integer

// default
StrategyResult strategyResult = null
List<Ohlcv> ohlcvs = assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, ohlcvPeriod)

// 현재 수익률
def profitPercentage = balanceAsset?.getProfitPercentage() ?: 0.0

// ripple
def analysis = new Analysis(ohlcvs)

// wave
def waveAnalysis = new Analysis(assetProfile.getOhlcvs(waveOhlcvType, waveOhlcvPeriod))

// tide
def tideAnalysis = new Analysis(assetProfile.getOhlcvs(tideOhlcvType, tideOhlcvPeriod))

// logging
log.info("analysis.momentum: {}", analysis.getMomentumScore())
log.info("wave.momentum: {}", waveAnalysis.getMomentumScore())
log.info("wave.volatility: {}", waveAnalysis.getVolatilityScore())
log.info("wave.oversold: {}", waveAnalysis.getOversoldScore())
log.info("wave.overbought: {}", waveAnalysis.getOverboughtScore())
log.info("tide.momentum: {}", tideAnalysis.getMomentumScore())
log.info("tide.oversold: {}", tideAnalysis.getOversoldScore())
log.info("tide.overbought: {}", tideAnalysis.getOverboughtScore())
log.info("profitPercentage: {}", profitPercentage)

//================================
// position
//================================
// 기본 포지션
def position = 1.0
// 장기 하락 시 비중 조절
if (tideAnalysis.getDirectionScore().getAverage() < 50) {
    position = 0.5
}

//================================
// rebalance
//================================
// 과매도 상태 시
if (waveAnalysis.getOversoldScore().getAverage() > 50) {
    // 과매도 반등 시
    if (analysis.getMomentumScore().getAverage() > 75) {
        // 하락분 매수
        strategyResult = StrategyResult.of(Action.BUY, position, "wave.oversold: ${waveAnalysis.getOversoldScore()}")
    }
}
// 과매수 상태 시
if (waveAnalysis.getOverboughtScore().getAverage() > 50) {
    // 과매도 반전 시
    if (analysis.getMomentumScore().getAverage() < 25) {
        // 상승분 매도
        strategyResult = StrategyResult.of(Action.SELL, position, "wave.overbought: ${waveAnalysis.getOverboughtScore()}")
    }
}
// filter - 중기 변동성 이 없을 경우 제외
if (waveAnalysis.getVolatilityScore().getAverage() < 50) {
    strategyResult = null
}

//================================
// return
//================================
return strategyResult
