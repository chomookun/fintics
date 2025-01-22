package org.chomoo.fintics.dao;

import org.chomoo.fintics.model.OrderSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String>, JpaSpecificationExecutor<OrderEntity> {

    /**
     * finds order entities by order search
     * @param orderSearch order search
     * @param pageable pageable
     * @return page of order entity
     */
    default Page<OrderEntity> findAll(OrderSearch orderSearch, Pageable pageable) {
        // where
        Specification<OrderEntity> specification = Specification.where(null);

        // order at from
        if (orderSearch.getOrderAtFrom() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get(OrderEntity_.ORDER_AT), orderSearch.getOrderAtFrom()));
        }

        // order at to
        if (orderSearch.getOrderAtTo() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get(OrderEntity_.ORDER_AT), orderSearch.getOrderAtTo()));
        }

        // trade id
        if (orderSearch.getTradeId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.TRADE_ID), orderSearch.getTradeId()));
        }

        // asset id
        if (orderSearch.getAssetId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.ASSET_ID), orderSearch.getAssetId()));
        }

        // type
        if (orderSearch.getType() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.TYPE), orderSearch.getType()));
        }

        // result
        if (orderSearch.getResult() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.RESULT), orderSearch.getResult()));
        }

        // sort
        Sort sort = Sort.by(OrderEntity_.ORDER_AT).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // return
        return findAll(specification, pageable);
    }
}
