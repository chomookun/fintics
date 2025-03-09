package org.chomookun.fintics.core.strategy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.arch4j.core.common.pbe.PbePropertiesUtil;
import org.chomookun.fintics.core.basket.entity.BasketEntity;
import org.chomookun.fintics.core.strategy.entity.StrategyEntity;
import org.chomookun.fintics.core.strategy.repository.StrategyRepository;
import org.chomookun.fintics.core.trade.repository.TradeRepository;
import org.chomookun.fintics.core.strategy.model.Strategy;
import org.chomookun.fintics.core.strategy.model.StrategySearch;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StrategyService {

    @PersistenceContext
    private final EntityManager entityManager;

    private final StrategyRepository strategyRepository;

    private final TradeRepository tradeRepository;

    /**
     * Saves strategy
     * @param strategy strategy
     * @return saved strategy
     */
    @Transactional
    public Strategy saveStrategy(Strategy strategy) {
        StrategyEntity strategyEntity = Optional.ofNullable(strategy.getStrategyId())
                .flatMap(strategyRepository::findById)
                .orElseGet(() -> StrategyEntity.builder()
                        .strategyId(IdGenerator.uuid())
                        .build());
        strategyEntity.setName(strategy.getName());
        strategyEntity.setSort(strategy.getSort());
        strategyEntity.setLanguage(strategy.getLanguage());
        strategyEntity.setVariables(Optional.ofNullable(strategy.getVariables())
                .map(PbePropertiesUtil::encodePropertiesString)
                .orElse(null));
        strategyEntity.setScript(strategy.getScript());
        StrategyEntity savedStrategyEntity = strategyRepository.saveAndFlush(strategyEntity);
        entityManager.refresh(savedStrategyEntity);
        return Strategy.from(savedStrategyEntity);
    }

    /**
     * Returns specified strategy
     * @param strategyId strategy id
     * @return strategy
     */
    public Optional<Strategy> getStrategy(String strategyId) {
        return strategyRepository.findById(strategyId)
                .map(Strategy::from);
    }

    /**
     * Deletes specified strategy
     * @param strategyId strategy id
     */
    @Transactional
    public void deleteStrategy(String strategyId) {
        StrategyEntity strategyEntity = strategyRepository.findById(strategyId).orElseThrow();
        // checks referenced by trade
        if (!tradeRepository.findAllByStrategyId(strategyEntity.getStrategyId()).isEmpty()) {
            throw new DataIntegrityViolationException("Referenced by existing trade");
        }
        // deletes
        strategyRepository.delete(strategyEntity);
        strategyRepository.flush();
    }

    /**
     * Gets strategies
     * @param strategySearch strategy search
     * @param pageable pageable
     * @return list of strategy
     */
    public Page<Strategy> getStrategies(StrategySearch strategySearch, Pageable pageable) {
        Page<StrategyEntity> strategyEntityPage = strategyRepository.findAll(strategySearch, pageable);
        List<Strategy> strategies = strategyEntityPage.getContent().stream()
                .map(Strategy::from)
                .toList();
        long total = strategyEntityPage.getTotalElements();
        return new PageImpl<>(strategies, pageable, total);
    }

    /**
     * Changes strategy sort
     * @param strategyId strategy id
     * @param sort sort
     */
    @Transactional
    public void changeStrategySort(String strategyId, Integer sort) {
        StrategyEntity strategyEntity = strategyRepository.findById(strategyId).orElseThrow();
        int originSort = Optional.ofNullable(strategyEntity.getSort()).orElse(Integer.MAX_VALUE);
        double finalSort = sort;
        // up
        if (sort < originSort) {
            finalSort = sort - 0.5;
        }
        // down
        if (sort > originSort) {
            finalSort = sort + 0.5;
        }
        // sort
        Map<String,Double> strategySorts = strategyRepository.findAll().stream()
                .collect(Collectors.toMap(StrategyEntity::getStrategyId, it ->
                        Optional.ofNullable(it.getSort())
                                .map(Double::valueOf)
                                .orElse(Double.MAX_VALUE)));
        strategySorts.put(strategyId, finalSort);
        List<String> sortedBasketIds = strategySorts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
        // updates
        for (int i = 0; i < sortedBasketIds.size(); i++) {
            strategyRepository.updateSort(sortedBasketIds.get(i), i);
        }
    }

}
