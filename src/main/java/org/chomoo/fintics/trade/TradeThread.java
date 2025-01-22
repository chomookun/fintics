package org.chomoo.fintics.trade;

import lombok.Getter;

/**
 * trade thread
 */
public class TradeThread extends Thread {

    @Getter
    private final TradeRunnable tradeRunnable;

    /**
     * constructor
     * @param threadGroup thread group
     * @param tradeRunnable thread runnable
     * @param tradeId trade id
     */
    public TradeThread(ThreadGroup threadGroup, TradeRunnable tradeRunnable, String tradeId) {
        super(threadGroup, tradeRunnable, tradeId);
        this.tradeRunnable = tradeRunnable;
    }

    /**
     * interrupt thread
     */
    public void interrupt() {
        super.interrupt();
        tradeRunnable.setInterrupted(true);
    }

}
