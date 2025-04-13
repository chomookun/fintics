package org.chomookun.fintics.daemon.trade;

import lombok.Getter;

@Getter
public class TradeThread extends Thread {

    private final TradeRunnable tradeRunnable;

    public TradeThread(ThreadGroup threadGroup, TradeRunnable tradeRunnable, String tradeId) {
        super(threadGroup, tradeRunnable, tradeId);
        this.tradeRunnable = tradeRunnable;
    }

    public void interrupt() {
        super.interrupt();
        tradeRunnable.setInterrupted(true);
    }

}
