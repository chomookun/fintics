package org.chomookun.fintics.core.trade.repository;

import org.chomookun.fintics.core.broker.entity.BrokerEntity_;
import org.chomookun.fintics.core.trade.entity.TradeEntity_;
import org.chomookun.fintics.core.trade.entity.TradeEntity;
import org.chomookun.fintics.core.trade.model.TradeSearch;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity,String>, JpaSpecificationExecutor<TradeEntity> {

    @Query("select a from TradeEntity a" +
            " order by a.name")
    List<TradeEntity> findAllOrderByName();

    default Page<TradeEntity> findAll(TradeSearch tradeSearch, Pageable pageable) {
        Specification<TradeEntity> specification = Specification.where(null);
        // name
        if (tradeSearch.getName() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(TradeEntity_.NAME), '%' + tradeSearch.getName() + '%'));
        }
        // sort
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.ASC, BrokerEntity_.NAME);
        // find
        if (pageable.isPaged()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            return findAll(specification, pageable);
        } else {
            List<TradeEntity> tradeEntities = findAll(specification, sort);
            return new PageImpl<>(tradeEntities);
        }
    }

    List<TradeEntity> findAllByBasketId(String basketId);

    List<TradeEntity> findAllByStrategyId(String strategyId);

    List<TradeEntity> findAllByBrokerId(String brokerId);

}
