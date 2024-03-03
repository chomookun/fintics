import groovy.transform.ToString
import org.hibernate.annotations.common.reflection.XMember
import org.oopscraft.fintics.calculator.*
import org.oopscraft.fintics.model.BalanceAsset
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.trade.Tool

import java.time.LocalTime

class Score extends LinkedHashMap<String, BigDecimal> {
    def getAverage() {
        return this.collect{it.value}.average()
    }
}

class Analysis {
    List<Ohlcv> ohlcvs
    List<Ema> fastEmas
    List<Ema> slowEmas
    List<Bb> bbs
    List<Macd> macds
    List<Rsi> rsis
    List<Dmi> dmis
    List<Obv> obvs
    List<Co> cos

    Analysis(List<Ohlcv> ohlcvs) {
        this.ohlcvs = ohlcvs
        this.fastEmas = Tool.calculate(ohlcvs, EmaContext.of(10))
        this.slowEmas = Tool.calculate(ohlcvs, EmaContext.of(20))
        this.bbs = Tool.calculate(ohlcvs, BbContext.DEFAULT)
        this.macds = Tool.calculate(ohlcvs, MacdContext.DEFAULT)
        this.rsis = Tool.calculate(ohlcvs, RsiContext.DEFAULT)
        this.dmis = Tool.calculate(ohlcvs, DmiContext.DEFAULT)
        this.obvs = Tool.calculate(ohlcvs, ObvContext.DEFAULT)
        this.cos = Tool.calculate(ohlcvs, CoContext.DEFAULT)
    }

    def getMomentumScore() {
        def score = new Score()
        // ema
        def fastEma = this.fastEmas.first()
        def slowEma = this.slowEmas.first()
        score.emaValueFastOverSlow = fastEma.value > slowEma.value ? 100 : 0
        // macd
        def macd = this.macds.first()
        score.macdValue = macd.value > 0 ? 100 : 0
        score.macdSignal = macd.signal > 0 ? 100 : 0
        score.macdValueOverSignal = macd.value > 0 && macd.value > macd.signal ? 100 : 0
        score.macdOscillator = macd.oscillator > 0 ? 100 : 0
        // rsi
        def rsi = this.rsis.first()
        score.rsiValue = rsi.value > 50 ? 100 : 0
        score.rsiValueOverSignal = rsi.value > 50 && rsi.value > rsi.signal ? 100 : 0
        // return
        return score
    }

    def getOverboughtScore() {
        def score = new Score()
        // rsi
        def rsi = this.rsis.first()
        score.rsiValueOver70 = rsi.value > 70 ? 100 : 0
        // return
        return score
    }

    def getOversoldScore() {
        def score = new Score()
        // rsi
        def rsi = this.rsis.first()
        score.rsiValueUnder30 = rsi.value < 30 ? 100 : 0
        // return
        return score
    }
}

// defines
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = "${assetIndicator.getAssetName()}(${assetId})"
def ohlcvType = Ohlcv.Type.MINUTE
def ohlcvPeriod = 3

// ohlcv
List<Ohlcv> ohlcvs = assetIndicator.getOhlcvs(ohlcvType, ohlcvPeriod)
        //.findAll{it.dateTime.toLocalDate().equals(dateTime.toLocalDate())}
def prices = ohlcvs.collect{it.closePrice}
def price = prices.first()
List<Ema> priceMas = Tool.calculate(ohlcvs, EmaContext.of(60))
def priceMa = priceMas.first()

//def prices = ohlcvs.collect{it.closePrice}
//def price = prices.first()
//def priceZScore = Tool.zScore(prices.take(20))
//log.info("[{}] priceZScore: {}", assetName, priceZScore)
//if (priceZScore.abs() < 1.0) {
//    return null
//}

def analysis = new Analysis(ohlcvs)
def momentumScore = analysis.getMomentumScore()
//def longAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY, 1))
//def longMomentumScore = longAnalysis.getMomentumScore()
//def pastAnalysis = new Analysis(ohlcvs.drop(20))
//def pastMomentumScore = pastAnalysis.getMomentumScore()
def dailyAnalysis = new Analysis(assetIndicator.getOhlcvs(Ohlcv.Type.DAILY, 1))
def dailyMomentumScore = dailyAnalysis.getMomentumScore()

// 단기 지표 상승 시 매수
if (momentumScore.getAverage() > 70) {
    hold = 1
}

// 단기 지표 하락 시 매도
if (momentumScore.getAverage() < 50) {
    hold = 0
}

// 과매수 시 매도
if (analysis.getOverboughtScore().getAverage() > 70) {
    hold = 0
}

// 과매도 시 매수
if (analysis.getOversoldScore().getAverage() > 70) {
    hold = 1
}

// fallback. 가격 이동 평균 이상 으로는 매수 하지 않음
if (price > priceMa.value) {
    log.info("가격 이동 평균 이상")
    hold = null
}

// fallback. 장기 지표가 좋지 않을 경우 매도
if (dailyMomentumScore.getAverage() < 50) {
    hold = 0
}


//if (longMomentumScore.getAverage() > 70) {
//    if (price < priceMa.value) {
////        hold = 1
//    }
//}
//
//if (analysis.getOverboughtScore().getAverage() > 50) {
//    hold = 0
//}
//if (analysis.getOversoldScore().getAverage() > 50) {
//    hold = 1
//}
//
//if (longMomentumScore.getAverage() < 50) {
//    hold = 0
//}


//def diffScore = momentumScore.getAverage() - pastMomentumScore.getAverage()
//if (diffScore > 20) {
//    hold = 1
//}
//if (diffScore < -20) {
//    hold = 0
//}





//def fastAnalysis = new Analysis(ohlcvs)
//def fastMomentumScore = fastAnalysis.getMomentumScore()
//def slowAnalysis = new Analysis(ohlcvs.drop(10))
//def slowMomentumScore = slowAnalysis.getMomentumScore()
//
//
//
//if (fastMomentumScore.getAverage() > slowMomentumScore.getAverage()) {
//    hold = 1
//}
//
//if (fastMomentumScore.getAverage() < slowMomentumScore.getAverage()) {
//    hold = 0
//}

//def scoreAverage = [fastMomentumScore.getAverage(), slowMomentumScore.getAverage()].average()
//if (scoreAverage > 70) {
//    hold = 1
//}
//if (scoreAverage < 30) {
//    hold = 0
//}




// analysis
//def analysis = new Analysis(ohlcvs)
//def momentumScore = analysis.getMomentumScore()
//def directionScore = analysis.getDirectionScore()
//def overboughtScore = analysis.getOverboughtScore()
//def oversoldScore = analysis.getOversoldScore()

// default buy
//if (momentumScore.getAverage() > 70) {
//    if (directionScore.getAverage() > 70) {
//        hold = 1
//    }
//}

// buy
//if (directionScore.getAverage() > 50) {
//    hold = 1
//}

// sell
//if (directionScore.getAverage() < 50) {
//    hold = 0
//}


// default sell
//if (momentumScore.getAverage() < 30) {
//    hold = 0
//}

// return
return hold

