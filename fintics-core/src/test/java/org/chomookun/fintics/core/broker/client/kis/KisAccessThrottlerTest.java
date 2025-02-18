package org.chomookun.fintics.core.broker.client.kis;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class KisAccessThrottlerTest {

    @Disabled
    @Test
    void test() throws InterruptedException {

        Runnable task1 = () -> {
            try {
                for (int i = 0; i < 100; i ++) {
                    KisAccessThrottler.sleep("A", 1000);
                    System.out.println(Thread.currentThread().getName() + " finished sleeping for appKey1.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Runnable task2 = () -> {
            try {
                for (int i = 0; i < 100; i ++) {
                    KisAccessThrottler.sleep("B", 1000);
                    System.out.println(Thread.currentThread().getName() + " finished sleeping for appKey2.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread thread1 = new Thread(task1, "Thread 1");
        Thread thread2 = new Thread(task2, "Thread 2");
        Thread thread3 = new Thread(task1, "Thread 3");
        Thread thread4 = new Thread(task2, "Thread 4");

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        Thread.currentThread().join(10_000);
    }

}