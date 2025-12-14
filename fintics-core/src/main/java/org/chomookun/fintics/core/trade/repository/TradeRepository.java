package org.chomookun.fintics.core.trade.repository;

import org.chomookun.fintics.core.trade.entity.TradeEntity_;
import org.chomookun.fintics.core.trade.entity.TradeEntity;
import org.chomookun.fintics.core.trade.model.TradeSearch;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity,String>, JpaSpecificationExecutor<TradeEntity> {

    /**
     * Finds all by trade search
     * @param tradeSearch trade search
     * @param pageable pageable
     * @return page of trade entities
     */
    default Page<TradeEntity> findAll(TradeSearch tradeSearch, Pageable pageable) {
        // specification
        Specification<TradeEntity> specification = Specification.where(null);
        if (tradeSearch.getName() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(TradeEntity_.NAME), '%' + tradeSearch.getName() + '%'));
        }
        // sort
        Sort sort = pageable.getSort()
                .and(Sort.by(TradeEntity_.SORT).ascending())
                .and(Sort.by(TradeEntity_.NAME).ascending());
        // find
        Pageable finalPageable = pageable.isUnpaged()
                ? Pageable.unpaged(sort)
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return findAll(specification, finalPageable);
    }

    /**
     * Find all by basket id
     * @param basketId basket id
     * @return list of trade entities
     */
    List<TradeEntity> findAllByBasketId(String basketId);

    /**
     * Finds all by strategy id
     * @param strategyId strategy id
     * @return list of trade entities
     */
    List<TradeEntity> findAllByStrategyId(String strategyId);

    /**
     * Finds all by broker id
     * @param brokerId broker id
     * @return list of trade entities
     */
    List<TradeEntity> findAllByBrokerId(String brokerId);

    /**
     * Updates sort
     * @param tradeId trade id
     * @param sort sort
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    update TradeEntity t
    set t.sort = :sort
    where t.tradeId= :tradeId
    """)
    void updateSort(@Param("tradeId") String tradeId, @Param("sort") Integer sort);

    /**
     * Updates invest amount
     * @param tradeId trade id
     * @param investAmount invest amount
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    update TradeEntity t
    set t.investAmount = :investAmount
    where t.tradeId= :tradeId
    """)
    void updateInvestAmount(@Param("tradeId") String tradeId,  @Param("investAmount") BigDecimal investAmount);

}
