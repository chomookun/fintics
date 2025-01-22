package org.chomoo.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomoo.fintics.service.AssetService;
import org.junit.jupiter.api.Test;
import org.chomoo.arch4j.core.common.test.CoreTestSupport;
import org.chomoo.fintics.FinticsConfiguration;
import org.chomoo.fintics.dao.AssetEntity;
import org.chomoo.fintics.model.Asset;
import org.chomoo.fintics.model.AssetSearch;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class AssetServiceTest extends CoreTestSupport {

    private final AssetService assetService;

    @PersistenceContext
    private final EntityManager entityManager;

    @Test
    void getAssets() {
        // given
        String assetId = "test";
        String assetName = "test name";
        entityManager.persist(AssetEntity.builder()
                .assetId(assetId)
                .name(assetName)
                .build());
        entityManager.flush();
        // when
        AssetSearch assetSearch = AssetSearch.builder()
                .assetId(assetId)
                .build();
        Page<Asset> assetPage = assetService.getAssets(assetSearch, PageRequest.of(0, 10));
        // then
        assertTrue(assetPage.getContent().stream().anyMatch(it -> Objects.equals(it.getAssetId(), assetId)));
        assertEquals(assetId, assetPage.getContent().get(0).getAssetId());
        assertEquals(assetName, assetPage.getContent().get(0).getName());
    }

    @Test
    void getAsset() {
        // given
        String assetId = "test";
        String assetName = "test name";
        entityManager.persist(AssetEntity.builder()
                .assetId(assetId)
                .name(assetName)
                .build());
        entityManager.flush();
        // when
        Asset asset = assetService.getAsset(assetId).orElseThrow();
        // then
        assertEquals(asset.getAssetId(), assetId);
    }

}