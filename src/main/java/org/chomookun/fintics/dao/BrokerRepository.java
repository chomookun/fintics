package org.chomookun.fintics.dao;

import org.chomookun.fintics.model.BrokerSearch;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrokerRepository extends JpaRepository<BrokerEntity, String>, JpaSpecificationExecutor<BrokerEntity> {

    /**
     * find broker list
     * @param brokerSearch broker search criteria
     * @param pageable pageable
     * @return page of broker entity
     */
    default Page<BrokerEntity> findAll(BrokerSearch brokerSearch, Pageable pageable) {
        // where
        Specification<BrokerEntity> specification = Specification.where(null);

        // like name
        if (brokerSearch.getName() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(BrokerEntity_.NAME), '%' + brokerSearch.getName() + '%')
            );
        }

        // sort
        Sort sort = pageable.getSort().and(Sort.by(Sort.Direction.ASC, BrokerEntity_.NAME));

        // find
        if (pageable.isPaged()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            return findAll(specification, pageable);
        } else {
            List<BrokerEntity> brokerEntities = findAll(specification, sort);
            return new PageImpl<>(brokerEntities, pageable, brokerEntities.size());
        }
    }

}
