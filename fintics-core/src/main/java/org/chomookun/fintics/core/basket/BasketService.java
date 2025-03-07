package org.chomookun.fintics.core.basket;

import lombok.RequiredArgsConstructor;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.arch4j.core.common.pbe.PbePropertiesUtil;
import org.chomookun.fintics.core.basket.entity.BasketAssetEntity;
import org.chomookun.fintics.core.basket.entity.BasketDividerEntity;
import org.chomookun.fintics.core.basket.entity.BasketEntity;
import org.chomookun.fintics.core.basket.model.BasketDivider;
import org.chomookun.fintics.core.basket.repository.BasketRepository;
import org.chomookun.fintics.core.trade.repository.TradeRepository;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.basket.model.BasketAsset;
import org.chomookun.fintics.core.basket.model.BasketSearch;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class BasketService {

    private final BasketRepository basketRepository;

    private final TradeRepository tradeRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * gets baskets
     * @param basketSearch basket search
     * @param pageable pageable
     * @return page of basket
     */
    public Page<Basket> getBaskets(BasketSearch basketSearch, Pageable pageable) {
        Page<BasketEntity> basketEntityPage = basketRepository.findAll(basketSearch, pageable);
        List<Basket> baskets = basketEntityPage.getContent().stream()
                .map(Basket::from)
                .collect(Collectors.toList());
        long total = basketEntityPage.getTotalElements();
        return new PageImpl<>(baskets, pageable, total);
    }

    /**
     * gets specified basket
     * @param basketId basket id
     * @return basket
     */
    public Optional<Basket> getBasket(String basketId) {
        return basketRepository.findById(basketId)
                .map(Basket::from);
    }

    /**
     * saves basket
     * @param basket basket
     * @return saved basket
     */
    @Transactional
    public Basket saveBasket(Basket basket) {
        BasketEntity basketEntity;
        if (basket.getBasketId() == null) {
            basketEntity = BasketEntity.builder()
                    .basketId(IdGenerator.uuid())
                    .build();
        } else {
            basketEntity = basketRepository.findById(basket.getBasketId()).orElseThrow();
        }
        basketEntity.setName(basket.getName());
        basketEntity.setMarket(basket.getMarket());
        basketEntity.setRebalanceEnabled(basket.isRebalanceEnabled());
        basketEntity.setRebalanceSchedule(basket.getRebalanceSchedule());
        basketEntity.setLanguage(basket.getLanguage());
        basketEntity.setVariables(Optional.ofNullable(basket.getVariables())
                .map(PbePropertiesUtil::encodePropertiesString)
                .orElse(null));
        basketEntity.setScript(basket.getScript());
        // basket assets
        basketEntity.getBasketAssets().clear();
        IntStream.range(0, basket.getBasketAssets().size()).forEach(i -> {
            BasketAsset basketAsset = basket.getBasketAssets().get(i);
            BasketAssetEntity basketAssetEntity = BasketAssetEntity.builder()
                    .basketId(basketEntity.getBasketId())
                    .assetId(basketAsset.getAssetId())
                    .sort(i)
                    .fixed(basketAsset.isFixed())
                    .enabled(basketAsset.isEnabled())
                    .holdingWeight(basketAsset.getHoldingWeight())
                    .variables(Optional.ofNullable(basketAsset.getVariables())
                            .map(PbePropertiesUtil::encodePropertiesString)
                            .orElse(null))
                    .build();
            basketEntity.getBasketAssets().add(basketAssetEntity);
        });
        // basket dividers
        basketEntity.getBasketDividers().clear();
        for (BasketDivider basketDivider : basket.getBasketDividers()) {
            BasketDividerEntity basketDividerEntity = BasketDividerEntity.builder()
                    .basketId(basketEntity.getBasketId())
                    .dividerId(IdGenerator.uuid())
                    .sort(basketDivider.getSort())
                    .name(basketDivider.getName())
                    .build();
            basketEntity.getBasketDividers().add(basketDividerEntity);
        }
        // save
        BasketEntity savedBasketEntity = basketRepository.saveAndFlush(basketEntity);
        entityManager.refresh(savedBasketEntity);
        return Basket.from(savedBasketEntity);
    }

    @Transactional
    public void deleteBasket(String basketId) {
        BasketEntity basketEntity = basketRepository.findById(basketId).orElseThrow();

        // checks referenced by trade
        if (!tradeRepository.findAllByBasketId(basketEntity.getBasketId()).isEmpty()) {
            throw new DataIntegrityViolationException("Referenced by existing trade");
        }

        // deletes
        basketRepository.delete(basketEntity);
        basketRepository.flush();
    }

}
