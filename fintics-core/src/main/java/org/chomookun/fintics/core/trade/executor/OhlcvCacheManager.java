package org.chomookun.fintics.core.trade.executor;

import lombok.*;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.chomookun.fintics.core.ohlcv.OhlcvService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OhlcvCacheManager {

    private final static int DAILY_OHLCVS_CACHE_EXPIRE_MINUTES = 60;

    private final static int MINUTE_OHLCVS_CACHE_EXPIRE_MINUTES = 10;

    private final OhlcvService ohlcvService;

    private final Object dailyOhlcvsLock = new Object();

    private final Object minuteOhlcvsLock = new Object();

    private LocalDateTime dailyOhlcvsExpireDateTime = LocalDateTime.now();

    private LocalDateTime minuteOhlcvsExpireDateTime = LocalDateTime.now();

    private Map<String, List<Ohlcv>> dailyOhlcvsCache = new ConcurrentHashMap<>();

    private Map<String, List<Ohlcv>> minuteOhlcvsCache = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public List<Ohlcv> getDailyOhlcvs(String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        List<Ohlcv> cachedDailyOhlcvs;

        // check and load cache
        synchronized (dailyOhlcvsLock) {
            // check and clear cache
            if (LocalDateTime.now().isAfter(dailyOhlcvsExpireDateTime)) {
                dailyOhlcvsCache = new ConcurrentHashMap<>();   // clear causes overhead, so delegate it to GC
                dailyOhlcvsExpireDateTime = LocalDateTime.now().plusMinutes(DAILY_OHLCVS_CACHE_EXPIRE_MINUTES);
            }
            // load cache
            cachedDailyOhlcvs = dailyOhlcvsCache.get(assetId);
            if (cachedDailyOhlcvs == null || cachedDailyOhlcvs.size() < 250) {
                cachedDailyOhlcvs = ohlcvService.getOhlcvs(assetId, Ohlcv.Type.DAILY, LocalDateTime.now().minusYears(3), LocalDateTime.now(), PageRequest.of(0, 500));
                cachedDailyOhlcvs.forEach(it -> it.setCached(true));
                dailyOhlcvsCache.put(assetId, cachedDailyOhlcvs);
            }
        }

        // filter ohlcvs
        return cachedDailyOhlcvs.stream()
                .filter(ohlcv ->
                        (ohlcv.getDateTime().equals(dateTimeFrom) || ohlcv.getDateTime().isAfter(dateTimeFrom))
                        && (ohlcv.getDateTime().equals(dateTimeTo) || ohlcv.getDateTime().isBefore(dateTimeTo))
                ).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Ohlcv> getMinuteOhlcvs(String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        List<Ohlcv> cachedMinuteOhlcvs;

        // check and load cache
        synchronized (minuteOhlcvsLock) {
            // check and clear cache
            if (LocalDateTime.now().isAfter(minuteOhlcvsExpireDateTime)) {
                minuteOhlcvsCache = new ConcurrentHashMap<>();  // clear causes overhead, so delegate it to GC
                minuteOhlcvsExpireDateTime = LocalDateTime.now().plusMinutes(MINUTE_OHLCVS_CACHE_EXPIRE_MINUTES);
            }
            // load cache
            cachedMinuteOhlcvs = minuteOhlcvsCache.get(assetId);
            if (cachedMinuteOhlcvs == null || cachedMinuteOhlcvs.size() < 3_000) {
                cachedMinuteOhlcvs = ohlcvService.getOhlcvs(assetId, Ohlcv.Type.MINUTE, LocalDateTime.now().minusMonths(1), LocalDateTime.now(), PageRequest.of(0, 6_000));
                cachedMinuteOhlcvs.forEach(it -> it.setCached(true));
                minuteOhlcvsCache.put(assetId, cachedMinuteOhlcvs);
            }
        }

        // fiter ohlcvs
        return cachedMinuteOhlcvs.stream()
                .filter(ohlcv ->
                        (ohlcv.getDateTime().equals(dateTimeFrom) || ohlcv.getDateTime().isAfter(dateTimeFrom))
                                && (ohlcv.getDateTime().equals(dateTimeTo) || ohlcv.getDateTime().isBefore(dateTimeTo))
                ).collect(Collectors.toList());
    }

}
