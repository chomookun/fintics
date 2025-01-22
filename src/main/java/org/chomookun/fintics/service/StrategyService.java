package org.chomookun.fintics.service;

import lombok.RequiredArgsConstructor;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.arch4j.core.common.pbe.PbePropertiesUtil;
import org.chomookun.fintics.dao.StrategyEntity;
import org.chomookun.fintics.dao.StrategyRepository;
import org.chomookun.fintics.dao.TradeRepository;
import org.chomookun.fintics.model.Strategy;
import org.chomookun.fintics.model.StrategySearch;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final StrategyRepository strategyRepository;

    private final TradeRepository tradeRepository;

    /**
     * gets strategies
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
     * returns specified strategy
     * @param strategyId strategy id
     * @return strategy
     */
    public Optional<Strategy> getStrategy(String strategyId) {
        return strategyRepository.findById(strategyId)
                .map(Strategy::from);
    }

    /**
     * saves strategy
     * @param strategy strategy
     * @return saved strategy
     */
    @Transactional
    public Strategy saveStrategy(Strategy strategy) {
        StrategyEntity strategyEntity;
        if (strategy.getStrategyId() == null) {
            strategyEntity = StrategyEntity.builder()
                    .strategyId(IdGenerator.uuid())
                    .build();
        } else {
            strategyEntity = strategyRepository.findById(strategy.getStrategyId()).orElseThrow();
        }
        strategyEntity.setName(strategy.getName());
        strategyEntity.setLanguage(strategy.getLanguage());
        strategyEntity.setVariables(Optional.ofNullable(strategy.getVariables())
                .map(PbePropertiesUtil::encodePropertiesString)
                .orElse(null));
        strategyEntity.setScript(strategy.getScript());
        StrategyEntity savedStrategyEntity = strategyRepository.saveAndFlush(strategyEntity);
        return Strategy.from(savedStrategyEntity);
    }

    /**
     * deletes specified strategy
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

}
