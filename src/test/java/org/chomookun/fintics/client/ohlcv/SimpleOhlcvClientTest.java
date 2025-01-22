package org.chomookun.fintics.client.ohlcv;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.FinticsConfiguration;
import org.chomookun.fintics.model.Asset;
import org.chomookun.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class SimpleOhlcvClientTest extends CoreTestSupport {

    private final OhlcvClientProperties ohlcvClientProperties;

    private final ObjectMapper objectMapper;

    SimpleOhlcvClient getSimpleOhlcvClient() {
        return new SimpleOhlcvClient(ohlcvClientProperties, objectMapper);
    }

    static Stream<Arguments> getIsSupportedArguments() {
        return Stream.of(
                Arguments.of("US.AAPL", "XNAS", true),
                Arguments.of("US.INVALIDXXX", "XNAS", false),
                Arguments.of("KR.005930", "XKRX", true),      // samsung electronics
                Arguments.of("KR.122630", "XKRX", true)       // KODEX Leverage ETF
        );
    }

    @ParameterizedTest
    @MethodSource({"getIsSupportedArguments"})
    void isSupported(String assetId, String exchange, boolean expected) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        // when
        boolean supported = getSimpleOhlcvClient().isSupported(asset);
        // then
        assertEquals(expected, supported);
    }

    static Stream<Arguments> getKrAssetInfos() {
        return Stream.of(
                Arguments.of("KR.005930", "XKRX"),      // samsung electronics
                Arguments.of("KR.122630", "XKRX")       // KODEX Leverage ETF
        );
    }

    static Stream<Arguments> getUsAssetInfos() {
        return Stream.of(
                Arguments.of("US.AAPL", "XNAS"),        // Apple
                Arguments.of("US.SPY", "XASE")          // SPY ETF
        );
    }

    @ParameterizedTest
    @MethodSource("getKrAssetInfos")
    void getDailyOhlcvInKrMarket(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusYears(1);
        LocalDateTime dateTimeTo = LocalDateTime.now();

        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);

        // then - check size
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() > 0);
        // then - check time range
        ohlcvs.forEach(ohlcv -> {
            LocalDate date = ohlcv.getDateTime().toLocalDate();
            log.info("date:{}", date);
            LocalDate dateRangeFrom = dateTimeFrom.toLocalDate();
            LocalDate dateRangeTo = dateTimeTo.toLocalDate();
            assertTrue(date.equals(dateRangeFrom) || date.isAfter(dateRangeFrom));
            assertTrue(date.equals(dateRangeTo) || date.isBefore(dateRangeTo));
        });
    }

    @ParameterizedTest
    @MethodSource("getUsAssetInfos")
    void getDailyOhlcvInUsMarket(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusYears(1);
        LocalDateTime dateTimeTo = LocalDateTime.now();

        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);

        // then - check size
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() > 0);
        // then - check time range
        ohlcvs.forEach(ohlcv -> {
            LocalDate date = ohlcv.getDateTime().toLocalDate();
            log.info("date:{}", date);
            LocalDate dateRangeFrom = dateTimeFrom.toLocalDate();
            LocalDate dateRangeTo = dateTimeTo.toLocalDate();
            assertTrue(date.equals(dateRangeFrom) || date.isAfter(dateRangeFrom));
            assertTrue(date.equals(dateRangeTo) || date.isBefore(dateRangeTo));
        });
    }

    @ParameterizedTest
    @MethodSource("getKrAssetInfos")
    void getMinuteOhlcvInKrMarket(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(30);
        LocalDateTime dateTimeTo = LocalDateTime.now();

        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);

        // then - check size
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() >= 60 * 4 * 15);
        // then - check date time range
        Ohlcv firstOhlcv = ohlcvs.get(0);
        Ohlcv lastOhlcv = ohlcvs.get(ohlcvs.size()-1);
        assertTrue(firstOhlcv.getDateTime().isBefore(dateTimeTo));
        assertTrue(lastOhlcv.getDateTime().isAfter(dateTimeFrom));
        // then - check time range
        ohlcvs.forEach(ohlcv -> {
            LocalTime time = ohlcv.getDateTime().toLocalTime();
            log.info("time:{}", time);
            LocalTime timeRangeFrom = LocalTime.of(9, 0);
            LocalTime timeRangeTo = LocalTime.of(15, 30);
            assertTrue(time.equals(timeRangeFrom) || time.isAfter(timeRangeFrom));
            assertTrue(time.equals(timeRangeTo) || time.isBefore(timeRangeTo));
        });
    }

    @ParameterizedTest
    @MethodSource("getUsAssetInfos")
    void getMinuteOhlcvInUsMarket(String assetId, String exchange) {
        // given
        Asset asset = Asset.builder()
                .assetId(assetId)
                .exchange(exchange)
                .build();
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(30);
        LocalDateTime dateTimeTo = LocalDateTime.now();

        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);

        // then - check size
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() >= 60 * 4 * 15);
        // then - check date time range
        Ohlcv firstOhlcv = ohlcvs.get(0);
        Ohlcv lastOhlcv = ohlcvs.get(ohlcvs.size()-1);
        assertTrue(firstOhlcv.getDateTime().isBefore(dateTimeTo));
        assertTrue(lastOhlcv.getDateTime().isAfter(dateTimeFrom));
        // then - check time range
        ohlcvs.forEach(ohlcv -> {
            LocalTime time = ohlcv.getDateTime().toLocalTime();
            log.info("time:{}", time);
            LocalTime timeRangeFrom = LocalTime.of(9, 30);
            LocalTime timeRangeTo = LocalTime.of(16, 0);
            assertTrue(time.equals(timeRangeFrom) || time.isAfter(timeRangeFrom));
            assertTrue(time.equals(timeRangeTo) || time.isBefore(timeRangeTo));
        });
    }

    @Test
    void getDailyOhlcvOfInvalidAsset() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.invalid")
                .exchange("XKRX")
                .build();
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusYears(6);
        LocalDateTime dateTimeTo = LocalDateTime.now().minusYears(3);

        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);

        // then - check size
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() < 1);
    }

    @Test
    void getMinuteOhlcvOfInvalidAsset() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.invalid")
                .exchange("XKRX")
                .build();
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusMonths(3);
        LocalDateTime dateTimeTo = LocalDateTime.now().minusMonths(2);

        // when
        List<Ohlcv> ohlcvs = getSimpleOhlcvClient().getOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);

        // then - check size
        log.debug("ohlcvs.size():{}", ohlcvs.size());
        assertTrue(ohlcvs.size() < 1);
    }

}