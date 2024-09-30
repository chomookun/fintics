package org.oopscraft.fintics.basket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.alarm.service.AlarmService;
import org.oopscraft.fintics.FinticsProperties;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.model.BasketSearch;
import org.oopscraft.fintics.service.BasketService;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class BasketRebalanceScheduler {

    private final Map<String, Basket> scheduledBaskets = new HashMap<>();

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    private final TaskScheduler taskScheduler;

    private final BasketService basketService;

    private final BasketRebalanceTaskFactory basketRebalanceTaskFactory;

    private final FinticsProperties finticsProperties;

    private final AlarmService alarmService;

    private final ObjectMapper objectMapper;

    /**
     * synchronizes scheduler with database
     */
    @Scheduled(initialDelay = 10_000, fixedDelay = 10_000)
    public void synchronize() {
        List<Basket> baskets = basketService.getBaskets(BasketSearch.builder().build(), Pageable.unpaged()).getContent();
        for (Basket basket : baskets) {
            try {
                if (basket.isRebalanceEnabled() && basket.getRebalanceSchedule() != null) {
                    Basket previousBasket = getScheduledBasket(basket.getBasketId());
                    // new
                    if (previousBasket == null) {
                        startScheduledTask(basket);
                        continue;
                    }
                    // changed
                    if (!Objects.equals(basket.getRebalanceSchedule(), previousBasket.getRebalanceSchedule())
                            || !Objects.equals(basket.getLanguage(), previousBasket.getLanguage())
                            || !Objects.equals(basket.getScript(), previousBasket.getScript())
                    ) {
                        startScheduledTask(basket);
                    }
                } else {
                    stopScheduledTask(basket);
                }
            } catch (Throwable e) {
                String errorMessage = String.format("basket schedule error[%s] - %s", basket.getName(), e.getMessage());
                log.warn(errorMessage);
            }
        }
    }

    /**
     * gets scheduled basket
     * @param basketId basket id
     * @return basket
     */
    public Basket getScheduledBasket(String basketId) {
        return scheduledBaskets.get(basketId);
    }

    /**
     * starts scheduled task
     * @param basket basket
     */
    public void startScheduledTask(Basket basket) {
        // removes task if already exist
        if (scheduledTasks.containsKey(basket.getBasketId())) {
            stopScheduledTask(basket);
        }
        // adds new schedule
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
                () -> executeTask(basket.getBasketId()),
                new CronTrigger(basket.getRebalanceSchedule())
        );
        scheduledBaskets.put(basket.getBasketId(), basket);
        scheduledTasks.put(basket.getBasketId(), scheduledFuture);
    }

    /**
     * stops scheduled task
     * @param basket basket
     */
    public void stopScheduledTask(Basket basket) {
        scheduledBaskets.remove(basket.getBasketId());
        ScheduledFuture<?> scheduledFuture = scheduledTasks.get(basket.getBasketId());
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledTasks.remove(basket.getBasketId());
        }
    }

    /**
     * executes task
     * @param basketId basket id
     */
    private synchronized void executeTask(String basketId) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Basket basket = basketService.getBasket(basketId).orElseThrow();
            stringBuilder.append(String.format("Basket rebalance completed - %s", basket.getName())).append('\n');
            BasketRebalanceTask basketRebalanceTask = basketRebalanceTaskFactory.getObject(basket);
            BasketRebalanceResult basketRebalanceResult = basketRebalanceTask.execute();
            stringBuilder.append(objectMapper.writeValueAsString(basketRebalanceResult));
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
            stringBuilder.append(e.getMessage());
        } finally {
            sendSystemAlarm(stringBuilder.toString());
        }
    }

    /**
     * send system alarm
     * @param content message content
     */
    protected void sendSystemAlarm(String content) {
        alarmService.sendAlarm(finticsProperties.getSystemAlarmId(), this.getClass().getSimpleName(), content);
    }

}
