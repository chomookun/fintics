package org.oopscraft.fintics.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.fintics.dao.BrokerAssetOhlcvRepository;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.dao.SimulateEntity;
import org.oopscraft.fintics.dao.SimulateRepository;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.simulate.*;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulateService {

    private final SimulateRepository simulateRepository;

    private final ApplicationContext applicationContext;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final BrokerAssetOhlcvRepository assetOhlcvRepository;

    private final SimulateRunnableFactory simulateRunnableFactory;

    private final SimpMessagingTemplate messagingTemplate;

    private final BlockingQueue<Runnable> simulateQueue = new ArrayBlockingQueue<>(3);

    private final ThreadPoolExecutor simulateExecutor = new ThreadPoolExecutor(
            1,
            3,
           60,
            TimeUnit.SECONDS,
            simulateQueue
    );

    private final Map<String,SimulateRunnable> simulateRunnableMap = new ConcurrentHashMap<>();

    public Page<Simulate> getSimulates(SimulateSearch simulateSearch, Pageable pageable) {
        Page<SimulateEntity> simulateEntityPage = simulateRepository.findAll(simulateSearch, pageable);
        List<Simulate> simulates = simulateEntityPage.getContent().stream()
                .map(Simulate::from)
                .toList();
        long count = simulateEntityPage.getTotalElements();
        return new PageImpl<>(simulates, pageable, count);
    }

    public synchronized Simulate runSimulate(Simulate simulate) {
        simulate.setSimulateId(IdGenerator.uuid());

        // add log appender
        Context context = ((Logger)log).getLoggerContext();
        SimulateLogAppender simulateLogAppender = new SimulateLogAppender(simulate, context, messagingTemplate);

        // run
        SimulateRunnable simulateRunnable = simulateRunnableFactory.getObject(simulate);
        simulateRunnable.setSimulateLogAppender(simulateLogAppender);
        simulateRunnable.onComplete(() -> {
            this.simulateRunnableMap.remove(simulate.getSimulateId());
        });

        simulateExecutor.submit(simulateRunnable);
        simulateRunnableMap.put(simulate.getSimulateId(), simulateRunnable);

        // return
        return simulate;
    }

    public synchronized void stopSimulate(String simulateId) {
        if(simulateRunnableMap.containsKey(simulateId)) {
            simulateRunnableMap.get(simulateId).setInterrupted(true);
        }
    }

}
