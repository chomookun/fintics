package org.chomookun.fintics.core.broker.repository;

import org.chomookun.fintics.core.broker.entity.BrokerEntity;
import org.chomookun.fintics.core.broker.model.BrokerSearch;
import org.chomookun.fintics.core.broker.entity.BrokerEntity_;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrokerRepository extends JpaRepository<BrokerEntity, String>, JpaSpecificationExecutor<BrokerEntity> {

    /**
     * Find brokers by broker search
     * @param brokerSearch broker search criteria
     * @param pageable pageable
     * @return page of broker entity
     */
    default Page<BrokerEntity> findAll(BrokerSearch brokerSearch, Pageable pageable) {
        // specifications
        Specification<BrokerEntity> specification = Specification.where(null);
        if (brokerSearch.getName() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(BrokerEntity_.NAME), '%' + brokerSearch.getName() + '%')
            );
        }
        // sort
        Sort sort = pageable.getSort()
                .and(Sort.by(BrokerEntity_.SORT).ascending())
                .and(Sort.by(BrokerEntity_.NAME).ascending());
        // find all
        Pageable finalPageable = pageable.isUnpaged()
                ? Pageable.unpaged(sort)
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return findAll(specification, finalPageable);
    }

    @Modifying
    @Query("update BrokerEntity b set b.sort = :sort where b.brokerId = :brokerId")
    void updateSort(String brokerId, Integer sort);

}
