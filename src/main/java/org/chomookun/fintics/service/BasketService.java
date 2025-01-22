package org.chomookun.fintics.service;

import lombok.RequiredArgsConstructor;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.arch4j.core.common.pbe.PbePropertiesUtil;
import org.chomookun.fintics.dao.BasketAssetEntity;
import org.chomookun.fintics.dao.BasketEntity;
import org.chomookun.fintics.dao.BasketRepository;
import org.chomookun.fintics.dao.TradeRepository;
import org.chomookun.fintics.model.Basket;
import org.chomookun.fintics.model.BasketAsset;
import org.chomookun.fintics.model.BasketSearch;
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
                    .variables(Optional.ofNullable(basketAsset.getVariables())
                            .map(PbePropertiesUtil::encodePropertiesString)
                            .orElse(null))
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
