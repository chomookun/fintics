package org.chomoo.fintics.client.broker.kis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * kis access throttler
 * appKey 별 초당 20건 호출 제한
 */
public class KisAccessThrottler {

    private static final ConcurrentHashMap<String, LockAndCondition> requestLocks = new ConcurrentHashMap<>();

    /**
     * lock and condition class
     */
    private static class LockAndCondition {
        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        long lastAccessTime = 0;
    }

    /**
     * sleep
     * @param appKey app key
     * @param sleepMillis sleep milli seconds
     */
    public static void sleep(String appKey, long sleepMillis) throws InterruptedException {
        LockAndCondition lockAndCondition = requestLocks.computeIfAbsent(appKey, k -> new LockAndCondition());
        lockAndCondition.lock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastAccess = currentTime - lockAndCondition.lastAccessTime;

            if (timeSinceLastAccess < sleepMillis) {
                long sleepTime = sleepMillis - timeSinceLastAccess;
                lockAndCondition.condition.awaitNanos(sleepTime * 1_000_000);
            }

            // Update the last access time for this access token
            lockAndCondition.lastAccessTime = currentTime;
        } finally {
            lockAndCondition.lock.unlock();
        }
    }

}
