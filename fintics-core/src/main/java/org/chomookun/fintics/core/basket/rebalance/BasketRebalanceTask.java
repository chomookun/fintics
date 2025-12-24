package org.chomookun.fintics.core.basket.rebalance;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.balance.BalanceService;
import org.chomookun.fintics.core.balance.model.Balance;
import org.chomookun.fintics.core.balance.model.BalanceAsset;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.basket.model.BasketAsset;
import org.chomookun.fintics.core.basket.BasketService;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.core.trade.model.TradeSearch;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
@Slf4j
public class BasketRebalanceTask {

    @Getter
    private final Basket basket;

    private final BasketService basketService;

    private final TradeService tradeService;

    private final BalanceService balanceService;

    private final BasketScriptRunnerFactory basketScriptRunnerFactory;

    /**
     * Executes basket rebalance task
     * @return basket rebalance result
     */
    public BasketRebalanceResult execute() {
        // defines
        List<BasketAsset> addedBasketAssets = new ArrayList<>();
        List<BasketAsset> removedBasketAssets = new ArrayList<>();
        // executes basket script runner
        BasketScriptRunner basketRebalanceRunner = basketScriptRunnerFactory.getObject(basket);
        basketRebalanceRunner.setLog((Logger)log);
        List<BasketRebalanceAsset> basketRebalanceAssets = basketRebalanceRunner.run();
        log.info("basketRebalanceAssets: {}", basketRebalanceAssets);
        //================================================
        // 0. 베스켓 사용 중인 트레이드 + 잔고 조회
        //================================================
        List<Trade> trades = tradeService.getTrades(TradeSearch.builder().build(), Pageable.unpaged()).stream()
                .filter(trade -> Objects.equals(trade.getBasketId(), basket.getBasketId()))
                .toList();
        List<Balance> balances = trades.stream()
                .map(trade -> {
                    try {
                        return balanceService.getBalance(trade.getBrokerId()).orElseThrow();
                    } catch (Throwable e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                })
                .toList();
        // retrieve latest basket info
        Basket basket = basketService.getBasket(this.basket.getBasketId()).orElseThrow();
        //===========================================
        // 1. 신규 리밸런싱 종목 추가
        //===========================================
        for (BasketRebalanceAsset basketRebalanceAsset : basketRebalanceAssets) {
            String market = basket.getMarket();
            String symbol = basketRebalanceAsset.getSymbol();
            BigDecimal holdingWeight = basketRebalanceAsset.getHoldingWeight();
            String assetId = String.format("%s.%s", market, symbol);
            // 동일 종목 베스켓 에서 조회
            BasketAsset basketAsset = basket.getBasketAssets().stream()
                    .filter(it -> Objects.equals(it.getAssetId(), assetId))
                    .findFirst()
                    .orElse(null);
            // 신규 종목인 경우 추가
            if (basketAsset == null) {
                basketAsset = BasketAsset.builder()
                        .assetId(assetId)
                        .enabled(true)
                        .holdingWeight(holdingWeight)
                        .build();
                basket.getBasketAssets().add(basketAsset);
                addedBasketAssets.add(basketAsset);
            } else {
                // 이미 존재 하는 종목인 경우 고정 종목이 아니 라면 보유 비중 수정
                if (!basketAsset.isFixed()) {
                    basketAsset.setHoldingWeight(holdingWeight);
                }
            }
        }
        //===========================================
        // 2. 기존 종목 삭제
        //===========================================
        for (int i = basket.getBasketAssets().size()-1; i >= 0; i --) {
            BasketAsset basketAsset = basket.getBasketAssets().get(i);
            // 고정 종목은 리벨런싱 제외
            if (basketAsset.isFixed()) {
                continue;
            }
            // 교체 종목 인지 여부 확인
            boolean existInBasketChanges = basketRebalanceAssets.stream().anyMatch(it ->
                    Objects.equals(it.getSymbol(), basketAsset.getSymbol()));
            // 교체 종목이 아닌 경우 (삭제 대상)
            if (!existInBasketChanges) {
                // 현재 매수 상태 인지 여부 확인
                boolean ownedAsset = isOwnedAsset(basketAsset.getAssetId(), balances);
                // 매수 상태인 경우 보유 비중만 0으로 설정 (매도 후 추가 매수는 되지 않고 다음 차 리밸런싱 시 삭제됨)
                if (ownedAsset) {
                    basketAsset.setHoldingWeight(BigDecimal.ZERO);
                }
                // 매수 하지 않은 종목인 경우 바로 삭제
                else {
                    basket.getBasketAssets().remove(i);
                    removedBasketAssets.add(basketAsset);
                }
            }
        }
        //===========================================
        // 3. (예외처리)
        // 증권사 장애 시 종목 제외되는 증상 있음으로
        // 장애 복구 시 다시 추가되도록 누락분 추가
        //===========================================
        for (Balance balance : balances) {
               for (BalanceAsset balanceAsset : balance.getBalanceAssets()) {
                    String assetId = balanceAsset.getAssetId();
                    // 현재 보유중인 종목이 최종 베스켓에 존재하는지 확인
                    boolean existInBasket = basket.getBasketAssets().stream()
                            .anyMatch(it -> Objects.equals(it.getAssetId(), assetId));
                    // 베스켓에서 누락된 경우 추가
                    if (!existInBasket) {
                        BasketAsset basketAsset = BasketAsset.builder()
                                .assetId(assetId)
                                .enabled(true)
                                .holdingWeight(BigDecimal.ZERO)
                                .build();
                        basket.getBasketAssets().add(basketAsset);
                        addedBasketAssets.add(basketAsset);
                    }
                }
        }
        //=============================================
        // 99. 최종 변경 사항 저장 처리
        //=============================================
        basketService.saveBasket(basket);
        // returns
        return BasketRebalanceResult.builder()
                .basketRebalanceAssets(basketRebalanceAssets)
                .addedBasketAssets(addedBasketAssets)
                .removedBasketAssets(removedBasketAssets)
                .build();
    }

    /**
     * Checks owned asset
     * @param assetId asset id
     * @param balances balances
     * @return whether owned or not
     */
    boolean isOwnedAsset(String assetId, List<Balance> balances) {
        for (Balance balance : balances) {
            BalanceAsset balanceAsset = balance.getBalanceAsset(assetId).orElse(null);
            if (balanceAsset != null) {
                return true;
            }
        }
        return false;
    }

}
