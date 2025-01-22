package org.chomoo.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomoo.fintics.service.OhlcvService;
import org.junit.jupiter.api.Test;
import org.chomoo.arch4j.core.common.test.CoreTestSupport;
import org.chomoo.fintics.FinticsConfiguration;
import org.chomoo.fintics.dao.OhlcvEntity;
import org.chomoo.fintics.dao.OhlcvSplitEntity;
import org.chomoo.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
public class OhlcvServiceTest extends CoreTestSupport {

    private final OhlcvService ohlcvService;

    @Test
    void getDailyOhlcvs() {
        String assetId = "test";
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(30).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime dateTimeTo = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        List.of(dateTimeFrom, dateTimeTo).forEach(dateTime -> entityManager.persist(OhlcvEntity.builder()
                .assetId(assetId)
                .type(Ohlcv.Type.DAILY)
                .dateTime(dateTime)
                .open(BigDecimal.ONE)
                .high(BigDecimal.ONE)
                .low(BigDecimal.ONE)
                .close(BigDecimal.ONE)
                .volume(BigDecimal.ONE)
                .build()));
        entityManager.flush();
        // when
        List<Ohlcv> ohlcvs = ohlcvService.getDailyOhlcvs(assetId, dateTimeFrom, dateTimeTo, PageRequest.of(0, 10));
        // then
        assertEquals(2, ohlcvs.size());
    }

    @Test
    void getMinuteOhlcvs() {
        String assetId = "test";
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime dateTimeTo = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List.of(dateTimeFrom, dateTimeTo).forEach(dateTime -> entityManager.persist(OhlcvEntity.builder()
                .assetId(assetId)
                .type(Ohlcv.Type.MINUTE)
                .dateTime(dateTime)
                .open(BigDecimal.ONE)
                .high(BigDecimal.ONE)
                .low(BigDecimal.ONE)
                .close(BigDecimal.ONE)
                .volume(BigDecimal.ONE)
                .build()));
        entityManager.flush();
        // when
        List<Ohlcv> assetOhlcvs = ohlcvService.getMinuteOhlcvs(assetId, dateTimeFrom, dateTimeTo, PageRequest.of(0, 10));
        // then
        assertEquals(2, assetOhlcvs.size());
    }

    @Test
    void getDailyOhlcvWithForwardSplit() {
        // given
        String assetId = "test";
        // 액면 분할 정보
        entityManager.persist(OhlcvSplitEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDateTime.of(2011,3,16, 0, 0, 0))
                .splitFrom(BigDecimal.valueOf(1))
                .splitTo(BigDecimal.valueOf(10))
                .build());
        // 액면 분할 전 가격 정보
        entityManager.persist(OhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDate.of(2011, 3, 15).atStartOfDay())
                .type(Ohlcv.Type.DAILY)
                .open(BigDecimal.valueOf(1000))
                .high(BigDecimal.valueOf(1010))
                .low(BigDecimal.valueOf(990))
                .close(BigDecimal.valueOf(1000))
                .volume(BigDecimal.valueOf(123))
                .build());
        // 액면 분할 후 가격 정보
        entityManager.persist(OhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDate.of(2011, 3, 16).atStartOfDay())
                .type(Ohlcv.Type.DAILY)
                .open(BigDecimal.valueOf(100))
                .high(BigDecimal.valueOf(101))
                .low(BigDecimal.valueOf(99))
                .close(BigDecimal.valueOf(100))
                .volume(BigDecimal.valueOf(1230))
                .build());
        entityManager.flush();
        // when
        List<Ohlcv> dailyOhlcvs = ohlcvService.getDailyOhlcvs(
                assetId,
                LocalDateTime.of(2011,1,1, 0, 0, 0),
                LocalDateTime.of(2011,12,31, 23, 59, 59),
                Pageable.unpaged()
        );
        // then
        log.info("dailyOhlcvs: {}", dailyOhlcvs);
        // 액면 분할 후 가격은 그대로 보존 (100)
        assertEquals(100, dailyOhlcvs.get(0).getClose().doubleValue());
        // 액면 분할 전 가격은 액면 분할 비율이 적용된 가격 (1000 -> 100)
        assertEquals(100, dailyOhlcvs.get(1).getClose().doubleValue());
    }

    @Test
    void getDailyOhlcvWithReverseSplit() {
        // given
        String assetId = "test";
        // 액면 병합 정보
        entityManager.persist(OhlcvSplitEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDateTime.of(2011,3,16, 0, 0, 0))
                .splitFrom(BigDecimal.valueOf(10))
                .splitTo(BigDecimal.valueOf(1))
                .build());
        // 액면 병합 전 가격 정보
        entityManager.persist(OhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDate.of(2011, 3, 15).atStartOfDay())
                .type(Ohlcv.Type.DAILY)
                .open(BigDecimal.valueOf(100))
                .high(BigDecimal.valueOf(101))
                .low(BigDecimal.valueOf(99))
                .close(BigDecimal.valueOf(100))
                .volume(BigDecimal.valueOf(1230))
                .build());
        // 액면 병합 후 가격 정보
        entityManager.persist(OhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDate.of(2011, 3, 16).atStartOfDay())
                .type(Ohlcv.Type.DAILY)
                .open(BigDecimal.valueOf(1000))
                .high(BigDecimal.valueOf(1010))
                .low(BigDecimal.valueOf(990))
                .close(BigDecimal.valueOf(1000))
                .volume(BigDecimal.valueOf(123))
                .build());
        entityManager.flush();
        // when
        List<Ohlcv> dailyOhlcvs = ohlcvService.getDailyOhlcvs(
                assetId,
                LocalDateTime.of(2011,1,1, 0, 0, 0),
                LocalDateTime.of(2011,12,31, 23, 59, 59),
                Pageable.unpaged()
        );
        // then
        log.info("dailyOhlcvs: {}", dailyOhlcvs);
        // 액면 병합 후 가격은 그대로 보존 (1000)
        assertEquals(1000, dailyOhlcvs.get(0).getClose().doubleValue());
        // 액면 병합 전 가격은 액면 분할 비율이 적용된 가격 (1000 -> 10)
        assertEquals(10, dailyOhlcvs.get(1).getClose().doubleValue());
    }


    @Test
    void getDailyOhlcvWithAVGOSplit() {
        // given
        String assetId = "US.AVGO";
        // 브로드컴(AVGO) 2024-07-15 10->1 액면 분할
        entityManager.persist(OhlcvSplitEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDateTime.of(2024,7,15, 0, 0, 0))
                .splitFrom(BigDecimal.valueOf(1))
                .splitTo(BigDecimal.valueOf(10))
                .build());
        // 액면 분할 전 가격 정보
        entityManager.persist(OhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDate.of(2024, 7, 14).atStartOfDay())
                .type(Ohlcv.Type.DAILY)
                .open(BigDecimal.valueOf(1606.34))
                .high(BigDecimal.valueOf(1614.28))
                .low(BigDecimal.valueOf(1598.66))
                .close(BigDecimal.valueOf(1599.23))
                .volume(BigDecimal.valueOf(2946))
                .build());
        // 액면 분할 후 가격 정보
        entityManager.persist(OhlcvEntity.builder()
                .assetId(assetId)
                .dateTime(LocalDate.of(2024, 7, 15).atStartOfDay())
                .type(Ohlcv.Type.DAILY)
                .open(BigDecimal.valueOf(160.634))
                .high(BigDecimal.valueOf(161.428))
                .low(BigDecimal.valueOf(159.866))
                .close(BigDecimal.valueOf(159.923))
                .volume(BigDecimal.valueOf(29460))
                .build());
        entityManager.flush();
        // when
        List<Ohlcv> dailyOhlcvs = ohlcvService.getDailyOhlcvs(
                assetId,
                LocalDateTime.of(2024,1,1, 0, 0, 0),
                LocalDateTime.of(2024,12,31, 23, 59, 59),
                Pageable.unpaged()
        );
        // then
        log.info("dailyOhlcvs: {}", dailyOhlcvs);
        // 액면 분할 후 가격은 그대로 보존 (1000)
        assertEquals(159.923, dailyOhlcvs.get(0).getClose().doubleValue());
        // 액면 분할 전 가격은 액면 분할 비율이 적용된 가격 (1000 -> 10)
        assertEquals(159.923, dailyOhlcvs.get(1).getClose().doubleValue());
    }


}
