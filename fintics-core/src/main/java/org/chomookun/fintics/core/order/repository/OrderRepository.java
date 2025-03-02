package org.chomookun.fintics.core.order.repository;

import org.chomookun.fintics.core.order.entity.OrderEntity_;
import org.chomookun.fintics.core.order.model.OrderSearch;
import org.chomookun.fintics.core.order.entity.OrderEntity;
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
     * Finds order entities by order search
     * @param orderSearch order search
     * @param pageable pageable
     * @return page of order entity
     */
    default Page<OrderEntity> findAll(OrderSearch orderSearch, Pageable pageable) {
        // specifications
        Specification<OrderEntity> specification = Specification.where(null);
        if (orderSearch.getOrderAtFrom() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get(OrderEntity_.ORDER_AT), orderSearch.getOrderAtFrom()));
        }
        if (orderSearch.getOrderAtTo() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get(OrderEntity_.ORDER_AT), orderSearch.getOrderAtTo()));
        }
        if (orderSearch.getTradeId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.TRADE_ID), orderSearch.getTradeId()));
        }
        if (orderSearch.getAssetId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.ASSET_ID), orderSearch.getAssetId()));
        }
        if (orderSearch.getType() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.TYPE), orderSearch.getType()));
        }
        if (orderSearch.getResult() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.RESULT), orderSearch.getResult()));
        }
        // sort
        Sort sort = pageable.getSort().and(Sort.by(OrderEntity_.ORDER_AT).descending());
        Pageable finalPageable = pageable.isUnpaged()
                ? Pageable.unpaged(sort)
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        // return
        return findAll(specification, finalPageable);
    }
}
