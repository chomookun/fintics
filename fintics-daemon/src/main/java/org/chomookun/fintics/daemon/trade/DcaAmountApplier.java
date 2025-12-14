package org.chomookun.fintics.daemon.trade;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.execution.model.Execution;
import org.chomookun.fintics.core.trade.entity.TradeEntity;
import org.chomookun.fintics.core.trade.repository.TradeRepository;
import org.chomookun.fintics.daemon.common.AbstractTask;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DcaAmountApplier extends AbstractTask {

    private final static String TASK_NAME = "DcaAmountApplier";

    private final TradeRepository tradeRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void applyDcaAmounts() {
        Execution execution = startExecution(TASK_NAME);
        try {
            List<TradeEntity> tradeEntities = tradeRepository.findAll();
            for (TradeEntity tradeEntity : tradeEntities) {
                // skip disabled trade
                if (!tradeEntity.isEnabled()) {
                    continue;
                }
                // apply DCA amount
                try {
                    applyDcaAmount(tradeEntity);
                } catch (Throwable t) {
                    log.error("Failed to apply DCA amount for trade: {}", tradeEntity.getTradeId(), t);
                }
            }
        } catch (Throwable t) {
            log.error("Failed to apply DCA amount", t);
            failExecution(execution, t);
            sendSystemNotification(execution);
        }
    }

    void applyDcaAmount(TradeEntity tradeEntity) {
        if (tradeEntity.isDcaEnabled()) {
            // checks DCA frequency
            LocalDate localDate = LocalDate.now();
            boolean accepted = switch (tradeEntity.getDcaFrequency()) {
                case DAILY -> true;
                case WEEKLY -> localDate.getDayOfWeek() == DayOfWeek.MONDAY;
                case MONTHLY -> localDate.getDayOfMonth() == 1;
                default -> throw new RuntimeException("invalid DCA frequency: " + tradeEntity.getDcaFrequency());
            };
            // applies DCA amount if accepted
            if (accepted) {
                BigDecimal appliedInvestAmount = tradeEntity.getInvestAmount().add(tradeEntity.getDcaAmount());
                tradeRepository.updateInvestAmount(tradeEntity.getTradeId(), appliedInvestAmount);
                entityManager.flush();
            }
        }
    }

}
