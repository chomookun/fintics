import org.oopscraft.fintics.indicator.Bb
import org.oopscraft.fintics.indicator.BbContext
import org.oopscraft.fintics.model.Ohlcv

// defines
def hold = null
def assetId = assetIndicator.getAssetId()
def assetName = "${assetIndicator.getAssetName()}(${assetId})"

// ohlcv
def ohlcvs = assetIndicator.getOhlcvs(Ohlcv.Type.MINUTE, 5)
def ohlcv = ohlcvs.first()
def closePrices = ohlcvs.collect{it.closePrice}
def closePricePctChange = tool.pctChange(closePrices.take(5))
log.info("[{}] closePrices: {}", assetName, closePrices.take(2))

// bb
List<Bb> bbs = tool.calculate(ohlcvs, BbContext.DEFAULT)
def bbMbbs = bbs.collect{it.mbb}
def bbMbbPctChange = tool.pctChange(bbMbbs.take(10))
def bbUbbs = bbs.collect{it.ubb}
def bbLbbs = bbs.collect{it.lbb}
def bbBandWidths = bbs.collect{it.bandWidth}
def bbPercentBs = bbs.collect{it.percentB}
log.info("[{}] Bb: {}", assetName, bbs.first())
log.info("[{}] bbBandWidth: {}", assetName, bbBandWidths.first())
log.info("[{}] bbMbbs: {}", assetName, bbMbbs.take(10))
log.info("[{}] bbMbbPctChange: {}", assetName, bbMbbPctChange)

// buy
if (closePrices.take(5).average() > bbMbbs.first()) {
    hold = 1
}

// sell
if (closePrices.take(5).average() < bbMbbs.first()) {
    hold = 0
}

//// golden cross
//if (closePrices.get(1) > bbMbbs.get(1)) {
//    if (closePrices.get(0) > closePrices.get(1)) {
//        hold = 1
//    }
//}
//// dead cross
//if (closePrices.get(1) < bbMbbs.get(1)) {
//    if (closePrices.get(0) < closePrices.get(1)) {
//        hold = 0
//    }
//}
//
//// touch upper band
//if (closePrices.get(1) > bbUbbs.get(1)) {
//    if (closePrices.get(0) < closePrices.get(1)) {
//        hold = 0
//    }
//}
//
//// touch lower band
//if (closePrices.get(1) < bbLbbs.get(1)) {
//    if (closePrices.get(0) < closePrices.get(1)) {
//        hold = 0
//    }
//}

// lower
//if (closePrices.get(1) < bbLbbs.get(1)) {
//    if (closePrices.get(0) > closePrices.get(1)) {
//        hold = 1
//    }
//}









// band width over 0.01
//if (bbBandWidths.first() >= 0.01) {
//    // buy
//    if (closePrices.first() >= bbUbbs.first()) {
//        hold = 1
//    }
//    // sell
//    if (closePrices.first() <= bbMbbs.first()) {
//        hold = 0
//    }
//}


//// band width over zero
//if (bbBandWidths.first() >= 0.01) {
//    log.info("[{}] bandWidth over zero: {}", assetName, bbBandWidths.first())
//    // middle band over zero
//    if (bbMbbPctChange > 0.0) {
//        // touch middle band
//        if (closePrices.first() <= bbMbbs.first()) {
//            hold = 1
//        }
//        // touch upper band
//        if (closePrices.first() >= bbUbbs.first()) {
//            hold = 0
//        }
//    }
//    // middle band under zero (force to sell)
//    if (bbMbbPctChange < 0.0) {
//        hold = 0
//    }
//}

// return
return hold

