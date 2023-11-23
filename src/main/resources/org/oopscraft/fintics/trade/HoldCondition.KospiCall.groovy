import java.time.LocalTime

Boolean hold;

// info
def name = assetIndicator.getName();

// OHLCV
def ohlcvs = tool.resample(assetIndicator.getMinuteOhlcvs(), 1);
def ohlcv = ohlcvs.first();

// price
def prices = ohlcvs.collect{it.closePrice};
def price = prices.first();
def pricePctChange = tool.sum(tool.pctChange(prices).take(5));

// shortMa
def shortMas = tool.ema(ohlcvs, 20);
def shortMa = shortMas.first();
def shortMaPctChange = tool.sum(tool.pctChange(shortMas).take(5));

// longMa
def longMas = tool.ema(ohlcvs, 60);
def longMa = longMas.first();
def longMaPctChange = tool.sum(tool.pctChange(longMas).take(5));

// macd
def macds = tool.macd(ohlcvs, 12, 26, 9);
def macd = macds.first();

// rsi
def rsis = tool.rsi(ohlcvs, 14);
def rsi = rsis.first();

// dmi
def dmis = tool.dmi(ohlcvs, 14);
def dmi = dmis.first();

// kospi indice
def kospiIndicator = indiceIndicators['KOSPI'];
def kospiOhlcvs = tool.resample(kospiIndicator.getMinuteOhlcvs(), 10);
def kospiMacd = tool.macd(kospiOhlcvs, 12, 16, 9).first();

// USD/KRW
def usdKrwIndicator = indiceIndicators['USD_KRW'];
def usdKrwOhlcvs = tool.resample(usdKrwIndicator.getMinuteOhlcvs(), 10);
def usdKrwMacd = tool.macd(usdKrwOhlcvs, 12, 16, 9).first();

// Nasdaq future
def ndxFutureIndicator = indiceIndicators['NDX_FUTURE'];
def ndxFutureOhlcvs = tool.resample(ndxFutureIndicator.getMinuteOhlcvs(), 10);
def ndxFutureMacd = tool.macd(ndxFutureOhlcvs, 12, 16, 9).first();

// logging
log.info("== [{}] orderBook:{}", name, orderBook);
log.info("== [{}] ohlcv:{}", name, ohlcv);
log.info("== [{}] price:{}({}%)", name, price, pricePctChange);
log.info("== [{}] shortMa:{}({}%)", name, shortMa, shortMaPctChange);
log.info("== [{}] longMa:{}({}%)", name, longMa, longMaPctChange);
log.info("== [{}] macd:{}", name, macd);
log.info("== [{}] rsi:{}", name, rsi);
log.info("== [{}] dmi:{}", name, dmi);
log.info("== [{}] kospiOhlcv:{}", kospiOhlcvs.first());
log.info("== [{}] kospiMacd:{}", kospiMacd);
log.info("== [{}] usdKrwOhlcv:{}", usdKrwOhlcvs.first());
log.info("== [{}] usdKrwMacd:{}", usdKrwMacd);
log.info("== [{}] ndxFutureOhlcv:{}", ndxFutureOhlcvs.first());
log.info("== [{}] ndxFutureMacd:{}", ndxFutureMacd);

// TODO 이상 거래 탐지

// 매수 여부 판단
if((price > shortMa && shortMa > longMa)
&& (pricePctChange > 0.0 && shortMaPctChange > 0.0 && longMaPctChange > 0.0)
){
    log.info("== [{}] buy vote.", name);
    def buyVotes = [];

    // technical analysis
    buyVotes.add(price > shortMa ? 100 : 0);
    buyVotes.add(price > longMa ? 100 : 0);
    buyVotes.add(shortMa > longMa ? 100 : 0);
    buyVotes.add(shortMaPctChange > 0.0 ? 100 : 0);
    buyVotes.add(longMaPctChange > 0.0 ? 100 : 0);
    buyVotes.add(macd.value > 0 ? 100 : 0);
    buyVotes.add(macd.oscillator > 0 ? 100 : 0);
    buyVotes.add(rsi > 50 ? 100 : 0);
    buyVotes.add(dmi.pdi > dmi.mdi ? 100 : 0);
    buyVotes.add(dmi.pdi - dmi.mdi > 10 && dmi.adx > 25 ? 100 : 0);

    // indice
    buyVotes.add(kospiMacd.value > 0 ? 100 : 0);
    buyVotes.add(usdKrwMacd.value < 0 ? 100 : 0);
    buyVotes.add(ndxFutureMacd.value > 0 ? 100 : 0);

    // buy result
    log.info("== [{}] buyVotes[{}]:{}", name, buyVotes.average(), buyVotes);
    if(buyVotes.average() > 70) {
        hold = true;
    }
}

// 매도 여부 판단
if((price < shortMa || shortMa < longMa)
|| (pricePctChange < 0.0 || shortMaPctChange < 0.0 || longMaPctChange < 0.0)
){
    log.info("== [{}] sell vote.", name);
    def sellVotes = [];

    // technical analysis
    sellVotes.add(price < shortMa ? 100 : 0);
    sellVotes.add(price < longMa ? 100 : 0);
    sellVotes.add(shortMa < longMa ? 100 : 0);
    sellVotes.add(shortMaPctChange < 0.0 ? 100 : 0);
    sellVotes.add(longMaPctChange < 0.0 ? 100 : 0);
    sellVotes.add(macd.value < 0 ? 100 : 0);
    sellVotes.add(macd.oscillator < 0 ? 100 : 0);
    sellVotes.add(rsi < 50 ? 100 : 0);
    sellVotes.add(dmi.mdi > dmi.pdi ? 100 : 0);
    sellVotes.add(dmi.mdi - dmi.pdi > 10 && dmi.adx > 25 ? 100 : 0);

    // indice
    sellVotes.add(kospiMacd.value < 0 ? 100 : 0);
    sellVotes.add(usdKrwMacd.value > 0 ? 100 : 0);
    sellVotes.add(ndxFutureMacd.value < 0 ? 100 : 0);

    // sell result
    log.info("== [{}] sellVotes[{}]:{}", name, sellVotes.average(), sellVotes);
    if(sellVotes.average() > 70) {
        hold = false;
    }
}

// 장종료 전 처리 - 15:00 부터는 매수는 하지 않음
if(dateTime.toLocalTime().isAfter(LocalTime.of(15,0))) {
    if(hold) {
        hold = false;
    }
}

// 장종료 전 처리 - 15:15 이후는 모두 매도(보유 하지 않음)
if(dateTime.toLocalTime().isAfter(LocalTime.of(15, 15))) {
    hold = false;
}

// return
log.info("== [{}] hold:{}", name, hold);
return hold;

