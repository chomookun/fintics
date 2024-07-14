package org.oopscraft.fintics.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.dao.BasketAssetEntity;
import org.oopscraft.fintics.dao.BasketAssetRepository;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.model.BasketAsset;
import org.oopscraft.fintics.service.BasketService;
import org.oopscraft.fintics.trade.basket.BasketChange;
import org.oopscraft.fintics.trade.basket.BasketRebalance;
import org.oopscraft.fintics.trade.basket.BasketRebalanceContext;
import org.oopscraft.fintics.trade.basket.BasketRebalanceFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class BasketRebalanceScheduler {

    private final Map<String, Basket> scheduledBaskets = new HashMap<>();

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    private final TaskScheduler taskScheduler;

    private final BasketRebalanceFactory basketRebalanceFactory;

    private final BasketService basketService;

    private final BasketAssetRepository basketAssetRepository;

    public Basket getScheduledBasket(String basketId) {
        return scheduledBaskets.get(basketId);
    }

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

    public void stopScheduledTask(Basket basket) {
        scheduledBaskets.remove(basket.getBasketId());
        ScheduledFuture<?> scheduledFuture = scheduledTasks.get(basket.getBasketId());
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledTasks.remove(basket.getBasketId());
        }
    }

    private void executeTask(String basketId) {
        Basket basket = basketService.getBasket(basketId).orElseThrow();
        BasketRebalanceContext context = BasketRebalanceContext.builder()
                .basket(basket)
                .build();
        BasketRebalance basketRebalance = basketRebalanceFactory.getObject(context);
        List<BasketChange> basketChanges = basketRebalance.getChanges();
        log.info("basketChanges: {}", basketChanges);

        // clears holdWeights
        for (BasketAsset basketAsset : basket.getBasketAssets()) {
            if (!basketAsset.isFixed()) {
                BasketAssetEntity.Pk pk = BasketAssetEntity.Pk.builder()
                        .basketId(basketAsset.getBasketId())
                        .assetId(basketAsset.getAssetId())
                        .build();
                BasketAssetEntity basketAssetEntity = basketAssetRepository.findById(pk).orElseThrow();
                basketAssetEntity.setHoldingWeight(BigDecimal.ZERO);
                basketAssetRepository.saveAndFlush(basketAssetEntity);
            }
        }

        // adds basket assets
        int sort = basket.getBasketAssets().stream()
                .mapToInt(BasketAsset::getSort)
                .max()
                .orElse(0) + 1;
        for (BasketChange basketChange : basketChanges) {
            String market = basket.getMarket();
            String symbol = basketChange.getSymbol();
            String assetId = String.format("%s.%s", market, symbol);
            BasketAssetEntity.Pk pk = BasketAssetEntity.Pk.builder()
                    .basketId(basketId)
                    .assetId(assetId)
                    .build();
            BasketAssetEntity basketAssetEntity = basketAssetRepository.findById(pk).orElse(null);
            if (basketAssetEntity == null) {
                basketAssetEntity = BasketAssetEntity.builder()
                        .basketId(basketId)
                        .assetId(assetId)
                        .sort(sort ++)
                        .enabled(true)
                        .build();
            }
            basketAssetEntity.setHoldingWeight(basketChange.getHoldingWeight());
            basketAssetRepository.saveAndFlush(basketAssetEntity);
        }

        // TODO clear holdWeight zero + not holding

    }

}
