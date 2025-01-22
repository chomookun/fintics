package org.chomookun.fintics.dao;

import org.chomookun.fintics.model.StrategySearch;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StrategyRepository extends JpaRepository<StrategyEntity, String>, JpaSpecificationExecutor<StrategyEntity> {

    default Page<StrategyEntity> findAll(StrategySearch strategySearch, Pageable pageable) {
        Specification<StrategyEntity> specification = Specification.where(null);

        // name
        if (strategySearch.getName() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(StrategyEntity_.NAME), '%' + strategySearch.getName() + '%'));
        }

        // sort
        Sort sort = Sort.by(StrategyEntity_.NAME).ascending();

        // find
        if (pageable.isPaged()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            return findAll(specification, pageable);
        } else {
            List<StrategyEntity> strategyEntities = findAll(specification, sort);
            return new PageImpl<>(strategyEntities, pageable, strategyEntities.size());
        }
    }

}
