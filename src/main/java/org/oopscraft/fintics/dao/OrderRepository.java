package org.oopscraft.fintics.dao;

import org.oopscraft.fintics.model.OrderResult;
import org.oopscraft.fintics.model.OrderType;
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

    default Page<OrderEntity> findAllByTradeId(String tradeId, String symbol, OrderType orderType, OrderResult orderResult, Pageable pageable) {
        Specification<OrderEntity> specification = Specification.where(null);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(OrderEntity_.TRADE_ID), tradeId));
        if(symbol != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.SYMBOL), symbol));
        }
        if(orderType != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.ORDER_TYPE), orderType));
        }
        if(orderResult != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(OrderEntity_.ORDER_RESULT), orderResult));
        }
        Sort sort = Sort.by(OrderEntity_.ORDER_AT).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return findAll(specification, pageable);
    }

}
