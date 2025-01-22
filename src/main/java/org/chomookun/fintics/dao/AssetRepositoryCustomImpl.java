package org.chomookun.fintics.dao;

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
                                .orElse(null)
                )
                .orderBy(qAssetEntity.marketCap.desc());
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
