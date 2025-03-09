package org.chomookun.fintics.core.basket.repository;

import org.chomookun.fintics.core.basket.entity.BasketEntity;
import org.chomookun.fintics.core.basket.entity.BasketEntity_;
import org.chomookun.fintics.core.basket.model.BasketSearch;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
        // specification
        Specification<BasketEntity> specification = Specification.where(null);
        if (basketSearch.getName() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get(BasketEntity_.NAME), '%' + basketSearch.getName() + '%')
            );
        }
        // sort
        Sort sort = pageable.getSort()
                .and(Sort.by(BasketEntity_.SORT).ascending())
                .and(Sort.by(BasketEntity_.NAME).ascending());
        // finds all
        Pageable finalPageable = pageable.isUnpaged()
                ? Pageable.unpaged(sort)
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return findAll(specification, finalPageable);
    }

    /**
     * Updates sort
     * @param basketId basket id
     * @param sort sort
     */
    @Modifying
    @Query("update BasketEntity b set b.sort = :sort where b.basketId = :basketId")
    void updateSort(String basketId, Integer sort);

}
