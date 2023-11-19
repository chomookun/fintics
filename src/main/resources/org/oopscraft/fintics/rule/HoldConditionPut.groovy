import java.time.LocalTime;

Boolean hold;

// OHLCV
def ohlcvs = tool.resample(assetIndicator.getMinuteOhlcvs(), 5);
def ohlcv = ohlcvs.first();
log.info("[{}] ohlcv:{}", assetIndicator.getName(), ohlcv);

// price
def prices = ohlcvs.collect{it.closePrice};
def price = prices.first();
def priceSlope = tool.slope(prices.take(3));

// EMA
def emas = tool.ema(ohlcvs, 12);
def ema = emas.first();
def emaSlope = tool.slope(emas.take(3));

// MACD
def macds = tool.macd(ohlcvs, 12, 16, 9);
def macdValue = macds.first().value;
def macdSignal = macds.first().signal;
def macdValueSlope = tool.slope(macds.collect{it.value}.take(3));
def macdSignalSlope = tool.slope(macds.collect{it.signal}.take(3));

// RSI
def rsis = tool.rsi(ohlcvs, 14);
def rsi = rsis.first();
def rsiSlope = tool.slope(rsis.take(3));

// DMI
def dmis = tool.dmi(ohlcvs, 14);
def dmiPdi = dmis.first().pdi;
def dmiMdi = dmis.first().mdi;
def dmiAdx = dmis.first().adx;
def dmiPdiSlope = tool.slope(dmis.collect{it.pdi}.take(3));
def dmiMdiSlope = tool.slope(dmis.collect{it.mdi}.take(3));

// Kospi
def kospiIndicator = indiceIndicators['KOSPI'];
def kospiOhlcvs = tool.resample(kospiIndicator.getMinuteOhlcvs(), 30);
log.info("kospiOhlcv: {}", kospiOhlcvs.first());
def kospiPrices = kospiOhlcvs.collect{it.closePrice};
def kospiPrice = kospiPrices.first();
def kospiPriceZScore = tool.zScore(kospiPrices.take(10), kospiPrice);

//  USD/KRW
def usdKrwIndicator = indiceIndicators['USD_KRW'];
def usdKrwOhlcvs = tool.resample(usdKrwIndicator.getMinuteOhlcvs(), 30);
log.info("usdKrwOhlcv: {}", usdKrwOhlcvs.first());
def usdKrwPrices = usdKrwOhlcvs.collect{it.closePrice};
def usdKrwPrice = usdKrwPrices.first();
def usdKrwPriceZScore = tool.zScore(usdKrwPrices.take(10), usdKrwPrice);

// Nasdaq Future
def ndxFutureIndicator = indiceIndicators['NDX_FUTURE'];
def ndxFutureOhlcvs = tool.resample(ndxFutureIndicator.getMinuteOhlcvs(), 30);
log.info("ndxFutureOhlcv: {}", ndxFutureOhlcvs.first());
def ndxFuturePrices = ndxFutureOhlcvs.collect{it.closePrice};
def ndxFuturePrice = ndxFuturePrices.first();
def ndxFuturePriceZScore = tool.zScore(ndxFuturePrices.take(10), ndxFuturePrice);

// z-score
def priceZScore = tool.zScore(prices.take(10), price);
log.info("[{}] priceZScore:{}, price:{}, ema:{}", assetIndicator.getName(), priceZScore, price, ema);

def printTaInfo = {
    log.info("---------------------------------------------");
    log.info("- [Technical Analysis]");
    log.info("- symbol:{}", assetIndicator.getSymbol());
    log.info("- name:{}", assetIndicator.getName());
    log.info("- priceZSCore:{}", priceZScore);
    log.info("- price:{}", price);
    log.info("- ema:{}", ema);
    log.info("- emaSlope:{}", emaSlope);
    log.info("- macdValue:{}", macdValue);
    log.info("- macdSignal:{}", macdSignal);
    log.info("- macdValueSlope:{}", macdValueSlope);
    log.info("- macdSignalSlope:{}", macdSignalSlope);
    log.info("- rsi:{}", rsi);
    log.info("- rsiSlope:{}", rsiSlope);
    log.info("- dmiPdi:{}", dmiPdi);
    log.info("- dmiMdi:{}", dmiMdi);
    log.info("- dmiAdx:{}", dmiAdx);
    log.info("- dmiPdiSlope:{}", dmiPdiSlope);
    log.info("- dmiMdiSlope:{}", dmiMdiSlope);
    log.info("- kospiPriceZScore: {}", kospiPriceZScore);
    log.info("- usdKrwPriceZScore: {}", usdKrwPriceZScore);
    log.info("- ndxFuturePriceZScore: {}", ndxFuturePriceZScore);
    log.info("---------------------------------------------");
}

// buy condition
if(priceZScore > 1.5 && price > ema) {
    printTaInfo();
    def buyVotes = [];

    // technical analysis
    buyVotes.add(price > ema ? 100 : 0);
    buyVotes.add(priceSlope > 0 ? 100 : 0);
    buyVotes.add(emaSlope > 0 ? 100 : 0);
    buyVotes.add(macdValue > 0 ? 100 : 0);
    buyVotes.add(macdValue > macdSignal ? 100 : 0);
    buyVotes.add(macdValueSlope > 0 ? 100 : 0);
    buyVotes.add(rsi > 50 ? 100 : 0);
    buyVotes.add(rsiSlope > 0 ? 100 : 0);
    buyVotes.add(dmiPdi > dmiMdi ? 100 : 0);
    buyVotes.add(dmiPdiSlope > 0 ? 100 : 0);
    buyVotes.add(dmiMdiSlope < 0 ? 100 : 0);

    // 코스피 하락시 매수(인버스)
    buyVotes.add(kospiPriceZScore < 0.0 ? 100 : 0);

    // 달러환율 상승시 매수(인버스)
    buyVotes.add(usdKrwPriceZScore > 0.0 ? 100 : 0);

    // 나스닥선물 하락시 매수(인버스)
    buyVotes.add(ndxFuturePriceZScore < 0.0 ? 100 : 0);

    // buy result
    log.info("buyVotes[{}] - {}/{}", assetIndicator.getName(), buyVotes.average(), buyVotes);
    if(buyVotes.average() > 80) {
        hold = true;
    }
}

// sell condition
if(priceZScore < -1.5 && price < ema) {
    printTaInfo();
    def sellVotes = [];

    // technical analysis
    sellVotes.add(price < ema ? 100 : 0);
    sellVotes.add(priceSlope < 0 ? 100 : 0);
    sellVotes.add(emaSlope < 0 ? 100 : 0);
    sellVotes.add(macdValue < 0 ? 100 : 0);
    sellVotes.add(macdValue < macdSignal ? 100 : 0);
    sellVotes.add(macdValueSlope < 0 ? 100 : 0);
    sellVotes.add(rsi < 50 ? 100 : 0);
    sellVotes.add(rsiSlope < 0 ? 100 : 0);
    sellVotes.add(dmiPdi < dmiMdi ? 100 : 0);
    sellVotes.add(dmiPdiSlope < 0 ? 100 : 0);
    sellVotes.add(dmiMdiSlope > 0 ? 100 : 0);

    // 코스피 상승시 매도(인버스)
    sellVotes.add(kospiPriceZScore > 0.0 ? 100 : 0);

    // 달러환율 하락시 매도(인버스)
    sellVotes.add(usdKrwPriceZScore < 0 ? 100 : 0);

    // 나스닥선물 상승시 매도(인버스)
    sellVotes.add(ndxFuturePriceZScore > 0.0 ? 100 : 0);

    // buy result
    log.info("sellVotes[{}] - {}/{}", assetIndicator.getName(), sellVotes.average(), sellVotes);
    if(sellVotes.average() > 80) {
        hold = false;
    }
}

// 장종료 전 보유여부 체크
if(dateTime.toLocalTime().isAfter(LocalTime.of(15,15))) {
    if(kospiPriceZScore > 0.0
    && usdKrwPriceZScore < 0.0
    && ndxFuturePriceZScore > 0.0
    ) {
        hold = false;
    }
}

// return
return hold;
