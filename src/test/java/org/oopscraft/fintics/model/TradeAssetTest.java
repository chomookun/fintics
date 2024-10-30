package org.oopscraft.fintics.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Slf4j
public class TradeAssetTest {

    @Test
    void getOhlcvs() {
        // given
        String assetId = "test";
        Ohlcv.Type type = Ohlcv.Type.MINUTE;
        LocalDateTime now = LocalDateTime.now();
        List<Ohlcv> minuteOhlcvs = new ArrayList<>(){{
            add(Ohlcv.of(assetId, type, now.minusMinutes(1), null, 1000, 1100, 900, 1050, 100, false));
            add(Ohlcv.of(assetId, type, now.minusMinutes(2), null, 1060, 1200, 1030, 1040, 200, false));
            add(Ohlcv.of(assetId, type, now.minusMinutes(3), null, 1050, 1400, 800, 900, 300, false));
        }};

        // when
        TradeAsset tradeAsset = TradeAsset.builder()
                .minuteOhlcvs(minuteOhlcvs)
                .build();
        List<Ohlcv> resampleOhlcvs = tradeAsset.getOhlcvs(Ohlcv.Type.MINUTE, 3);

        // then
        log.info("resampleOhlcvs:{}", resampleOhlcvs);
        assertEquals(1, resampleOhlcvs.size());
        Ohlcv ohlcv = resampleOhlcvs.get(0);
        assertEquals(1050, ohlcv.getOpen().longValue());
        assertEquals(1400, ohlcv.getHigh().longValue());
        assertEquals(800, ohlcv.getLow().longValue());
        assertEquals(1050, ohlcv.getClose().longValue());
        assertEquals(600, ohlcv.getVolume().longValue());
    }

    @Test
    void getOhlcvsWithUnderPeriod() {
        // given
        String assetId = "test";
        Ohlcv.Type type = Ohlcv.Type.MINUTE;
        LocalDateTime now = LocalDateTime.now();
        List<Ohlcv> minuteOhlcvs = new ArrayList<>(){{
            add(Ohlcv.of(assetId, type, now.minusMinutes(1), null, 1000, 1100, 900, 1050, 100, false));
            add(Ohlcv.of(assetId, type, now.minusMinutes(2), null, 1060, 1200, 1030, 1040, 200, false));
        }};

        // when
        TradeAsset tradeAsset = TradeAsset.builder()
                .minuteOhlcvs(minuteOhlcvs)
                .build();
        List<Ohlcv> resampleOhlcvs = tradeAsset.getOhlcvs(Ohlcv.Type.MINUTE, 3);

        // then
        log.info("resampleOhlcvs:{}", resampleOhlcvs);
        assertEquals(1, resampleOhlcvs.size());
        Ohlcv ohlcv = resampleOhlcvs.get(0);
        assertEquals(1060, ohlcv.getOpen().longValue());
        assertEquals(1200, ohlcv.getHigh().longValue());
        assertEquals(900, ohlcv.getLow().longValue());
        assertEquals(1050, ohlcv.getClose().longValue());
        assertEquals(300, ohlcv.getVolume().longValue());
    }

    @Test
    void getOhlcvWithOverPeriod() {
        // given
        String assetId = "test";
        Ohlcv.Type type = Ohlcv.Type.MINUTE;
        LocalDateTime now = LocalDateTime.now();
        List<Ohlcv> minuteOhlcvs = new ArrayList<>(){{
            add(Ohlcv.of(assetId, type, now.minusMinutes(1), null, 1000, 1100, 900, 1050, 100, false));
            add(Ohlcv.of(assetId, type, now.minusMinutes(2), null, 1060, 1200, 1030, 1040, 200, false));
            add(Ohlcv.of(assetId, type, now.minusMinutes(3), null, 1050, 1400, 800, 900, 300, false));
            add(Ohlcv.of(assetId, type, now.minusMinutes(4), null, 1010, 1300, 1000, 1050, 200, false));
            add(Ohlcv.of(assetId, type, now.minusMinutes(5), null, 990, 1100, 980, 1000, 100, false));
        }};

        // when
        TradeAsset tradeAsset = TradeAsset.builder()
                .minuteOhlcvs(minuteOhlcvs)
                .build();
        List<Ohlcv> resampleOhlcvs = tradeAsset.getOhlcvs(Ohlcv.Type.MINUTE, 3);

        // then
        log.info("resampleOhlcvs:{}", resampleOhlcvs);
        assertEquals(2, resampleOhlcvs.size());
        Ohlcv ohlcv1 = resampleOhlcvs.get(0);
        assertEquals(1050, ohlcv1.getOpen().longValue());
        assertEquals(1400, ohlcv1.getHigh().longValue());
        assertEquals(800, ohlcv1.getLow().longValue());
        assertEquals(1050, ohlcv1.getClose().longValue());
        assertEquals(600, ohlcv1.getVolume().longValue());
        Ohlcv ohlcv2 = resampleOhlcvs.get(1);
        assertEquals(990, ohlcv2.getOpen().longValue());
        assertEquals(1300, ohlcv2.getHigh().longValue());
        assertEquals(980, ohlcv2.getLow().longValue());
        assertEquals(1050, ohlcv2.getClose().longValue());
        assertEquals(300, ohlcv2.getVolume().longValue());
    }

}
