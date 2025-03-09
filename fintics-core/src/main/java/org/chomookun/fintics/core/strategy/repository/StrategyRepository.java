package org.chomookun.fintics.core.strategy.repository;

import org.chomookun.fintics.core.strategy.entity.StrategyEntity;
import org.chomookun.fintics.core.strategy.entity.StrategyEntity_;
import org.chomookun.fintics.core.strategy.model.StrategySearch;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StrategyRepository extends JpaRepository<StrategyEntity, String>, JpaSpecificationExecutor<StrategyEntity> {

    /**
     * Finds all by search condition
     * @param strategySearch strategy search
     * @param pageable pageable
     * @return page of strategy entity
     */
    default Page<StrategyEntity> findAll(StrategySearch strategySearch, Pageable pageable) {
        // specifications
        Specification<StrategyEntity> specification = Specification.where(null);
        if (strategySearch.getName() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(StrategyEntity_.NAME), '%' + strategySearch.getName() + '%'));
        }
        // sort
        Sort sort = pageable.getSort()
                .and(Sort.by(StrategyEntity_.SORT).ascending())
                .and(Sort.by(StrategyEntity_.NAME).ascending());
        // find all
        Pageable finalPageable = pageable.isUnpaged()
                ? Pageable.unpaged(sort)
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return findAll(specification, finalPageable);
    }

    /**
     * Updates sort
     * @param strategyId strategy id
     * @param sort sort
     */
    @Modifying
    @Query("update StrategyEntity s set s.sort = :sort where s.strategyId = :strategyId")
    void updateSort(String strategyId, Integer sort);

}
