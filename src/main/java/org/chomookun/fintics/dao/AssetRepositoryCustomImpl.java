package org.chomookun.fintics.dao;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.model.AssetSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AssetRepositoryCustomImpl implements AssetRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * find asset entities
     * @param assetSearch asset search criteria
     * @param pageable pageable
     * @return page of asset entities
     */
    @Override
    public Page<AssetEntity> findAll(AssetSearch assetSearch, Pageable pageable) {
        QAssetEntity qAssetEntity = QAssetEntity.assetEntity;
        JPAQuery<AssetEntity> query = jpaQueryFactory
                .selectFrom(qAssetEntity)
                .where(
                        Optional.ofNullable(assetSearch.getAssetId())
                                .map(qAssetEntity.assetId::contains)
                                .orElse(null),
                        Optional.ofNullable(assetSearch.getName())
                                .map(qAssetEntity.name::contains)
                                .orElse(null),
                        Optional.ofNullable(assetSearch.getMarket())
                                .map(qAssetEntity.market::eq)
                                .orElse(null),
                        Optional.ofNullable(assetSearch.getType())
                                .map(qAssetEntity.type::eq)
                                .orElse(null),
                        Optional.ofNullable(assetSearch.getFavorite())
                                .map(qAssetEntity.favorite::eq)
                                .orElse(null)
                );
        // sort
        List<OrderSpecifier<?>> orderSpecifiers = pageable.getSort().stream()
                .map(sort -> {
                    ComparableExpressionBase<?> expression = switch (sort.getProperty()) {
                        case AssetEntity_.ROE -> qAssetEntity.roe;
                        case AssetEntity_.PER -> qAssetEntity.per;
                        case AssetEntity_.DIVIDEND_FREQUENCY -> qAssetEntity.dividendFrequency;
                        case AssetEntity_.DIVIDEND_YIELD -> qAssetEntity.dividendYield;
                        case AssetEntity_.CAPITAL_GAIN -> qAssetEntity.capitalGain;
                        case AssetEntity_.TOTAL_RETURN -> qAssetEntity.totalReturn;
                        default -> throw new IllegalStateException("Unexpected value: " + sort.getProperty());
                    };
                    Order direction = sort.isAscending() ? Order.ASC : Order.DESC;
                    return new OrderSpecifier<>(direction, expression);
                }).collect(Collectors.toList());
        orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, qAssetEntity.marketCap));
        query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));

        // list
        List<AssetEntity> assetEntities = query.clone()
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();
        // total
        JPAQuery<AssetEntity> totalQuery = query.clone();
        totalQuery.getMetadata().clearOrderBy();
        Long total = totalQuery
                .select(qAssetEntity.count())
                .fetchOne();
        total = Optional.ofNullable(total).orElse(0L);
        // return
        return new PageImpl<>(assetEntities, pageable, total);
    }

}
