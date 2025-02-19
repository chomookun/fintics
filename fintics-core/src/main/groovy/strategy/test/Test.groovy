import org.chomookun.fintics.indicator.Dmi
import org.chomookun.fintics.indicator.DmiContext
import org.chomookun.fintics.indicator.Ema
import org.chomookun.fintics.indicator.EmaContext
import org.chomookun.fintics.indicator.Macd
import org.chomookun.fintics.indicator.MacdContext
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.model.Ohlcv
import org.chomookun.fintics.strategy.StrategyResult
import org.chomookun.fintics.strategy.StrategyResult.Action
import org.chomookun.fintics.trade.Tools
import org.oopscraft.fintics.indicator.*


List<Ohlcv> ohlcvs = assetProfile.getOhlcvs(Ohlcv.Type.MINUTE, 5)
def ohlcv = ohlcvs.first()
List<Ema> emas = Tools.indicators(ohlcvs, EmaContext.of(60))
def ema = emas.first()
List<Macd> macds = Tools.indicators(ohlcvs, MacdContext.DEFAULT)
def macd = macds.first()
List<Dmi> dmis = Tools.indicators(ohlcvs, DmiContext.DEFAULT)
def dmi = dmis.first()

StrategyResult strategyResult = null

if (dmi.adx > 25) {
    if (macd.oscillator > 0) {
        strategyResult = StrategyResult.of(Action.BUY, 1.0, "")
    }

    if (macd.oscillator < 0) {
        strategyResult = StrategyResult.of(Action.SELL, 0.0, "")
    }
}


//if (macd.value < 0 && macd.value > macd.signal) {
//    if (dmi.adx > 25) {
//    }
//}
//
//if (macd.value > 0 && macd.value < macd.signal) {
//    if (dmi.adx > 25) {
//    }
//}
//
////if (ohlcv.closePrice < ema.value) {
////    strategyResult = StrategyResult.of(Action.SELL, 0.0, "")
////}
//
//if (Tools.pctChange(emas.take(60).collect{it.value}) < 0.0) {
//    strategyResult = StrategyResult.of(Action.SELL, 0.0, "")
//}


// return
return strategyResult
