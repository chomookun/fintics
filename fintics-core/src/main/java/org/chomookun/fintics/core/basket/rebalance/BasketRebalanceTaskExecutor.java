package org.chomookun.fintics.core.basket.rebalance;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.notification.NotificationService;
import org.chomookun.fintics.core.FinticsCoreProperties;
import org.chomookun.fintics.core.basket.model.Basket;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Lazy(false)
@RequiredArgsConstructor
@Slf4j
public class BasketRebalanceTaskExecutor {

    private final FinticsCoreProperties finticsCoreProperties;

    private final NotificationService notificationService;

    private final BlockingQueue<BasketRebalanceTask> taskQueue = new LinkedBlockingQueue<>();

    @PostConstruct
    public void initialize() {
        Thread worker = new Thread(() -> {
            BasketRebalanceTask task = null;
            while (true) {
                try {
                    task = taskQueue.take();
                    BasketRebalanceResult result = task.execute();
                    log.info("== Basket Rebalance Result ==\n{}", result);
                    sendCompleteMessage(task, result);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.warn(e.getMessage());
                    if (task != null) {
                        sendErrorMessage(task, e);
                    }
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    public void submitTask(BasketRebalanceTask task) {
        taskQueue.add(task);
    }

    void sendCompleteMessage(BasketRebalanceTask task, BasketRebalanceResult result) {
        String notifierId = finticsCoreProperties.getSystemNotifierId();
        String subject = "Basket rebalance task completed.[Basket: " + task.getBasket().getName() + "]";
        String content = result.toFormattedString();
        notificationService.sendNotification(notifierId, subject, content, null, false);
    }

    void sendErrorMessage(BasketRebalanceTask task, Exception exception) {
        String notifierId = finticsCoreProperties.getSystemNotifierId();
        String subject = "Basket rebalance task failed.";
        String content = "Basket Name: " + task.getBasket() + "\nError: " + exception.getMessage();
        notificationService.sendNotification(notifierId, subject, content, null, false);
    }

}
