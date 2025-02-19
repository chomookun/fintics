package org.chomookun.fintics.core.basket.repository;

import org.chomookun.fintics.core.basket.entity.BasketEntity;
import org.chomookun.fintics.core.basket.entity.BasketEntity_;
import org.chomookun.fintics.core.basket.model.BasketSearch;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BasketRepository extends JpaRepository<BasketEntity, String>, JpaSpecificationExecutor<BasketEntity> {

    /**
     * find all
     * @param basketSearch basket search
     * @param pageable pageable
     * @return page of basket entities
     */
    default Page<BasketEntity> findAll(BasketSearch basketSearch, Pageable pageable) {
        // where
        Specification<BasketEntity> specification = Specification.where(null);

        // like name
        if (basketSearch.getName() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(BasketEntity_.NAME), '%' + basketSearch.getName() + '%')
            );
        }

        // sort
        Sort sort = Sort.by(BasketEntity_.NAME).ascending();

        // find
        if (pageable.isPaged()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            return findAll(specification, pageable);
        } else {
            List<BasketEntity> basketEntities = findAll(specification, sort);
            return new PageImpl<>(basketEntities, pageable, basketEntities.size());
        }
    }

}
