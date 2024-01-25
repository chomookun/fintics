package org.oopscraft.fintics.simulate;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.fintics.dao.SimulateEntity;
import org.oopscraft.fintics.dao.SimulateRepository;
import org.oopscraft.fintics.model.Balance;
import org.oopscraft.fintics.model.Order;
import org.oopscraft.fintics.model.Simulate;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.trade.TradeExecutor;
import org.oopscraft.fintics.trade.TradeLogAppender;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

public class SimulateRunnable implements Runnable {

    private final Simulate simulate;

    private final ApplicationContext applicationContext;

    private final SimulateLogAppender simulateLogAppender;

    private final Logger log;

    private final SimulateIndiceClient simulateIndiceClient;

    private final SimulateTradeClient simulateTradeClient;

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    private final PlatformTransactionManager transactionManager;

    private final SimulateRepository simulateRepository;

    @Setter
    @Getter
    private boolean interrupted = false;

    private Runnable onComplete;

    @Builder
    public SimulateRunnable(Simulate simulate, SimulateIndiceClient simulateIndiceClient, SimulateTradeClient simulateTradeClient, ApplicationContext applicationContext, SimulateLogAppender simulateLogAppender) {
        this.simulate = simulate;
        this.simulateIndiceClient = simulateIndiceClient;
        this.simulateTradeClient = simulateTradeClient;
        this.applicationContext = applicationContext;
        this.simulateLogAppender = simulateLogAppender;
        this.messagingTemplate = applicationContext.getBean(SimpMessagingTemplate.class);
        this.objectMapper = applicationContext.getBean(ObjectMapper.class);
        this.transactionManager = applicationContext.getBean(PlatformTransactionManager.class);
        this.simulateRepository = applicationContext.getBean(SimulateRepository.class);

        // add log appender
        log = (Logger) LoggerFactory.getLogger(simulate.getSimulateId());
        if(this.simulateLogAppender != null) {
            log.addAppender(this.simulateLogAppender);
        }
    }

    @Override
    public void run() {
        simulate.setStatus(Simulate.Status.RUNNING);
        simulate.setStartedAt(LocalDateTime.now());
        saveSimulate(simulate);

        simulateLogAppender.start();
        try {
            Trade trade = simulate.getTrade();
            LocalDateTime dateTimeFrom = simulate.getDateTimeFrom();
            LocalDateTime dateTimeTo = simulate.getDateTimeTo();
            Integer interval = trade.getInterval();

            // invest amount, fee rate
            BigDecimal investAmount = simulate.getInvestAmount();
            BigDecimal feeRate = simulate.getFeeRate();
            simulateTradeClient.deposit(investAmount);
            simulateTradeClient.setFeeRate(feeRate);

            // add order listener
            simulateTradeClient.onOrder(order -> {
                sendMessage("order", order);
            });

            // trade executor
            TradeExecutor tradeExecutor = TradeExecutor.builder()
                    .applicationContext(applicationContext)
                    .log(log)
                    .build();

            // start
            for(LocalDateTime dateTime = dateTimeFrom.plusSeconds(interval); dateTime.isBefore(dateTimeTo); dateTime = dateTime.plusSeconds(interval)) {
                // check interrupted
                if(interrupted) {
                    log.info("SimulateRunnable is interrupted");
                    break;
                }
                // check start and end time
                if(dateTime.toLocalTime().isBefore(trade.getStartAt()) || dateTime.toLocalTime().isAfter(trade.getEndAt())) {
                    continue;
                }

                log.info("== dateTime:{}", dateTime);
                TransactionStatus transactionStatus = null;
                try {
                    simulateIndiceClient.setDateTime(dateTime);
                    simulateTradeClient.setDateTime(dateTime);

                    // check market open
                    if(!simulateTradeClient.isOpened(dateTime)) {
                        log.info("market not open:{}", dateTime);
                        continue;
                    }

                    // start transaction
                    DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
                    transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    transactionStatus = transactionManager.getTransaction(transactionDefinition);

                    // executes trade
                    tradeExecutor.execute(trade, dateTime, simulateIndiceClient, simulateTradeClient);

                    // send message
                    HashMap<String,String> status = new LinkedHashMap<>();
                    status.put("dateTime", dateTime.format(DateTimeFormatter.ISO_DATE_TIME));
                    sendMessage("status", status);
                    sendMessage("balance", simulateTradeClient.getBalance());

                } catch (Throwable e) {
                    log.warn(e.getMessage(), e);
                } finally {
                    if(transactionStatus != null) {
                        if(!transactionStatus.isCompleted()) {
                            transactionStatus.setRollbackOnly();
                            transactionManager.commit(transactionStatus);
                        }
                    }
                }
            }

            // save history
            simulate.setStatus(Simulate.Status.COMPLETED);

        }catch(Exception e) {
            log.error(e.getMessage(), e);
            simulate.setStatus(Simulate.Status.FAILED);
            throw new RuntimeException(e);
        }finally{
            simulateLogAppender.stop();
            this.onComplete.run();

            // save history
            simulate.setEndedAt(LocalDateTime.now());
            saveSimulate(simulate);
        }
    }

    private void sendMessage(String destinationSuffix, Object object) {
        String destination =  String.format("/simulate/%s/%s", simulate.getSimulateId(), destinationSuffix);
        String message = null;
        try {
            message = objectMapper.writeValueAsString(object);
            messagingTemplate.convertAndSend(destination, message);
        }catch( JsonProcessingException e) {
            log.error("== object:{}", object);
            log.error(e.getMessage(), e);
        }
    }

    public void onComplete(Runnable listener) {
        this.onComplete = listener;
    }

    public void saveSimulate(Simulate simulate) {
        SimulateEntity simulateEntity = simulateRepository.findById(simulate.getSimulateId())
                .orElse(null);
        if(simulateEntity == null) {
            simulateEntity = SimulateEntity.builder()
                    .simulateId(simulate.getSimulateId())
                    .status(simulate.getStatus())
                    .holdCondition(simulate.getTrade().getHoldCondition())
                    .dateTimeFrom(simulate.getDateTimeFrom())
                    .dateTimeTo(simulate.getDateTimeTo())
                    .investAmount(simulate.getInvestAmount())
                    .feeRate(simulate.getFeeRate())
                    .startedAt(LocalDateTime.now())
                    .build();
        }
        simulateEntity.setStartedAt(simulate.getStartedAt());
        simulateEntity.setEndedAt(simulate.getEndedAt());
        simulateEntity.setResult(simulate.getResult());

        simulateRepository.saveAndFlush(simulateEntity);
    }

}
