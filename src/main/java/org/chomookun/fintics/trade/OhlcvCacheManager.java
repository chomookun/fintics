package org.chomookun.fintics.trade;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.*;
import org.chomookun.fintics.model.Ohlcv;
import org.chomookun.fintics.service.OhlcvService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OhlcvCacheManager {

    private final OhlcvService ohlcvService;

    private final Object dailyOhlcvsLock = new Object();

    private final Object minuteOhlcvsLock = new Object();

    private LocalDateTime dailyOhlcvsExpireDateTime = LocalDateTime.now();

    private LocalDateTime minuteOhlcvsExpireDateTime = LocalDateTime.now();

    private Map<String, List<Ohlcv>> dailyOhlcvsCache = new ConcurrentHashMap<>();

    private Map<String, List<Ohlcv>> minuteOhlcvsCache = new ConcurrentHashMap<>();

    /**
     * Gets daily ohlcvs
     * @param assetId asset id
     * @param dateTimeFrom date time from
     * @param dateTimeTo date time to
     * @return daily ohlcvs
     */
    @Transactional(readOnly = true)
    public List<Ohlcv> getDailyOhlcvs(String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        List<Ohlcv> cachedDailyOhlcvs;

        // check and load cache
        synchronized (dailyOhlcvsLock) {
            // check and clear cache
            if (LocalDateTime.now().isAfter(dailyOhlcvsExpireDateTime)) {
                dailyOhlcvsCache = new ConcurrentHashMap<>();   // clear causes overhead, so delegate it to GC
                dailyOhlcvsExpireDateTime = LocalDateTime.now().plusMinutes(10);
            }
            // load cache
            cachedDailyOhlcvs = dailyOhlcvsCache.get(assetId);
            if (cachedDailyOhlcvs == null || cachedDailyOhlcvs.size() < 250) {
                cachedDailyOhlcvs = ohlcvService.getDailyOhlcvs(assetId, LocalDateTime.now().minusYears(3), LocalDateTime.now(), PageRequest.of(0, 500));
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

    /**
     * Gets minute ohlcvs
     * @param assetId asset id
     * @param dateTimeFrom date time from
     * @param dateTimeTo data time to
     * @return minute ohlcvs
     */
    @Transactional(readOnly = true)
    public List<Ohlcv> getMinuteOhlcvs(String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        List<Ohlcv> cachedMinuteOhlcvs;

        // check and load cache
        synchronized (minuteOhlcvsLock) {
            // check and clear cache
            if (LocalDateTime.now().isAfter(minuteOhlcvsExpireDateTime)) {
                minuteOhlcvsCache = new ConcurrentHashMap<>();  // clear causes overhead, so delegate it to GC
                minuteOhlcvsExpireDateTime = LocalDateTime.now().plusMinutes(5);
            }
            // load cache
            cachedMinuteOhlcvs = minuteOhlcvsCache.get(assetId);
            if (cachedMinuteOhlcvs == null || cachedMinuteOhlcvs.size() < 3_000) {
                cachedMinuteOhlcvs = ohlcvService.getMinuteOhlcvs(assetId, LocalDateTime.now().minusMonths(1), LocalDateTime.now(), PageRequest.of(0, 6_000));
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
