package org.chomookun.fintics.daemon.common;

import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.execution.model.Execution;
import org.chomookun.arch4j.core.execution.ExecutionService;
import org.chomookun.arch4j.core.notification.NotificationService;
import org.chomookun.fintics.core.FinticsCoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;

@Slf4j
public abstract class AbstractTask {

    @Autowired
    private FinticsCoreProperties finticsCoreProperties;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ExecutionService executionService;

    protected final Execution startExecution(String schedulerId) {
        return executionService.start(schedulerId);
    }

    protected final void updateExecution(Execution execution) {
        executionService.update(execution);
    }

    protected final void successExecution(Execution execution) {
        executionService.success(execution);
    }

    protected final void failExecution(Execution execution, Throwable e) {
        executionService.fail(execution, e);
    }

    protected final void sendSystemNotification(Execution execution) {
        String alarmId = finticsCoreProperties.getSystemNotifierId();
        String subject = String.format("%s[%s]", execution.getTaskName(), execution.getExecutionId());
        String content = execution.toString();
        if (alarmId != null) {
            notificationService.sendNotification(alarmId, subject, content, null, false);
        }
    }

    /**
     * chunk save entities via specified repository
     * @param unitName unit name
     * @param entities entities
     * @param transactionManager transaction manager
     * @param jpaRepository jpa repository
     * @param <T> entity type
     * @param <P> id class type
     */
    protected final <T, P> void saveEntities(String unitName, List<T> entities, PlatformTransactionManager transactionManager, JpaRepository<T,P> jpaRepository) {
        if (entities.isEmpty()) {
            return;
        }
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = transactionManager.getTransaction(definition);
        try {
            int count = 0;
            for (T ohlcvEntity : entities) {
                count++;
                jpaRepository.saveAndFlush(ohlcvEntity);
                // middle commit
                if (count % 10 == 0) {
                    log.debug("- {} chunk commit[{}]", unitName, count);
                    transactionManager.commit(status);
                    status = transactionManager.getTransaction(definition);
                }
            }
            // final commit
            log.debug("- {} final commit[{}]", unitName, count);
            transactionManager.commit(status);
            log.debug("- {} saved[{}]", unitName, count);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            transactionManager.rollback(status);
        } finally {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
        }
    }

    protected final void runWithTransaction(PlatformTransactionManager transactionManager, Runnable runnable) {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = transactionManager.getTransaction(definition);
        try {
            // execute
            runnable.run();
            // commit
            transactionManager.commit(status);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            transactionManager.rollback(status);
        } finally {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
        }
    }


}
