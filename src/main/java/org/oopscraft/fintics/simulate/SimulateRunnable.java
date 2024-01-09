package org.oopscraft.fintics.simulate;

import lombok.Builder;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.model.Simulate;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.trade.TradeExecutor;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Properties;

public class SimulateRunnable implements Runnable {

    private final Simulate simulate;

    private final ApplicationContext applicationContext;

    private final Logger log;

    @Builder
    public SimulateRunnable(Simulate simulate, ApplicationContext applicationContext, Logger log) {
        this.simulate = simulate;
        this.applicationContext = applicationContext;
        this.log = log;
    }

    @Override
    public void run() {
        Trade trade = simulate.getTrade();
        LocalDateTime dateTimeFrom = simulate.getDateTimeFrom();
        LocalDateTime dateTimeTo = simulate.getDateTimeTo();

        IndiceClient indiceClient = new SimulateIndiceClient();
        TradeClient tradeClient = new SimulateTradeClient(new Properties());

        for(LocalDateTime dateTime = dateTimeFrom.plusMinutes(1); dateTime.isBefore(dateTimeTo); dateTime = dateTime.plusMinutes(1)) {
            // check start and end time
            if(dateTime.toLocalTime().isBefore(trade.getStartAt()) || dateTime.toLocalTime().isAfter(trade.getEndAt())) {
                continue;
            }

            log.info("== dateTime:{}", dateTime);

            TradeExecutor tradeExecutor = TradeExecutor.builder()
                    .trade(trade)
                    .indiceClient(indiceClient)
                    .tradeClient(tradeClient)
                    .applicationContext(applicationContext)
                    .log(log)
                    .build();

            try {
                tradeExecutor.execute(dateTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

}
