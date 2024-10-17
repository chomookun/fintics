package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import org.oopscraft.arch4j.core.common.data.IdGenerator;
import org.oopscraft.arch4j.core.common.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.dao.BasketAssetEntity;
import org.oopscraft.fintics.dao.BasketEntity;
import org.oopscraft.fintics.dao.BasketRepository;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.model.BasketAsset;
import org.oopscraft.fintics.model.BasketSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasketService {

    private final BasketRepository basketRepository;

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
                .map(PbePropertiesUtil::encode)
                .orElse(null));
        basketEntity.setScript(basket.getScript());

        // basket assets
        List<BasketAssetEntity> basketAssetEntities = basketEntity.getBasketAssets();
        basketAssetEntities.clear();
        int sort = 0;
        for (BasketAsset basketAsset : basket.getBasketAssets()) {
            BasketAssetEntity basketAssetEntity = BasketAssetEntity.builder()
                    .basketId(basketEntity.getBasketId())
                    .assetId(basketAsset.getAssetId())
                    .fixed(basketAsset.isFixed())
                    .enabled(basketAsset.isEnabled())
                    .holdingWeight(basketAsset.getHoldingWeight())
                    .sort(sort ++)
                    .build();
            basketAssetEntities.add(basketAssetEntity);
        }

        // save
        BasketEntity savedBasketEntity = basketRepository.saveAndFlush(basketEntity);
        entityManager.refresh(savedBasketEntity);
        return Basket.from(savedBasketEntity);
    }

    @Transactional
    public void deleteBasket(String basketId) {
        basketRepository.deleteById(basketId);
        basketRepository.flush();
    }

}
