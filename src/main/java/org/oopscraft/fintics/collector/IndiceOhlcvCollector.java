package org.oopscraft.fintics.collector;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.FinticsProperties;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.dao.IndiceOhlcvEntity;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.model.IndiceId;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndiceOhlcvCollector extends OhlcvCollector {

    private final FinticsProperties finticsProperties;

    private final IndiceClient indiceClient;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final PlatformTransactionManager transactionManager;

    @PersistenceContext
    private final EntityManager entityManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    @Transactional
    @Override
    public void collect() {
        try {
            log.info("Start collect indice ohlcv.");
            LocalDateTime dateTime = LocalDateTime.now();
            for (IndiceId indiceId : IndiceId.values()) {
                try {
                    saveIndiceMinuteOhlcvs(indiceId, dateTime);
                    saveIndiceDailyOhlcvs(indiceId, dateTime);
                    deletePastRetentionOhlcv(indiceId);
                } catch (Throwable e) {
                    log.warn(e.getMessage());
                }
            }
            log.info("End collect indice ohlcv");
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            // TODO send error alarm
            throw new RuntimeException(e);
        }
    }

    private void saveIndiceMinuteOhlcvs(IndiceId indiceId, LocalDateTime dateTime) throws InterruptedException {
        // current
        List<Ohlcv> minuteOhlcvs = indiceClient.getMinuteOhlcvs(indiceId, dateTime);
        List<IndiceOhlcvEntity> minuteOhlcvEntities = minuteOhlcvs.stream()
                .map(ohlcv -> toIndiceOhlcvEntity(indiceId, ohlcv))
                .toList();

        // previous
        LocalDateTime dateTimeFrom = minuteOhlcvs.get(minuteOhlcvs.size()-1).getDateTime();
        LocalDateTime dateTimeTo = minuteOhlcvs.get(0).getDateTime();
        List<IndiceOhlcvEntity> previousMinuteOhlcvEntities = indiceOhlcvRepository.findAllByIndiceIdAndType(indiceId, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo, Pageable.unpaged());

        // save new or changed
        List<IndiceOhlcvEntity> newOrChangedMinuteOhlcvEntities = extractNewOrChangedOhlcvEntities(minuteOhlcvEntities, previousMinuteOhlcvEntities);
        log.info("saveIndiceMinuteOhlcvs[{}]:{}", indiceId, newOrChangedMinuteOhlcvEntities.size());
        saveEntities(newOrChangedMinuteOhlcvEntities, transactionManager, indiceOhlcvRepository);
    }

    private void saveIndiceDailyOhlcvs(IndiceId indiceId, LocalDateTime dateTime) throws InterruptedException {
        // current
        List<Ohlcv> dailyOhlcvs = indiceClient.getDailyOhlcvs(indiceId, dateTime);
        List<IndiceOhlcvEntity> dailyOhlcvEntities = dailyOhlcvs.stream()
                .map(ohlcv -> toIndiceOhlcvEntity(indiceId, ohlcv))
                .toList();

        // previous
        LocalDateTime dateTimeFrom = dailyOhlcvs.get(dailyOhlcvs.size()-1).getDateTime();
        LocalDateTime dateTimeTo = dailyOhlcvs.get(0).getDateTime();
        List<IndiceOhlcvEntity> previousDailyOhlcvEntities = indiceOhlcvRepository.findAllByIndiceIdAndType(indiceId, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo, Pageable.unpaged());

        // save new or changed
        List<IndiceOhlcvEntity> newOrChangedDailyOhlcvEntities = extractNewOrChangedOhlcvEntities(dailyOhlcvEntities, previousDailyOhlcvEntities);
        log.info("saveIndiceDailyOhlcvs[{}]:{}", indiceId, newOrChangedDailyOhlcvEntities.size());
        saveEntities(newOrChangedDailyOhlcvEntities, transactionManager, indiceOhlcvRepository);
    }

    private IndiceOhlcvEntity toIndiceOhlcvEntity(IndiceId indiceId, Ohlcv ohlcv) {
        return IndiceOhlcvEntity.builder()
                .indiceId(indiceId)
                .dateTime(ohlcv.getDateTime())
                .type(ohlcv.getType())
                .openPrice(ohlcv.getOpenPrice())
                .highPrice(ohlcv.getHighPrice())
                .lowPrice(ohlcv.getLowPrice())
                .closePrice(ohlcv.getClosePrice())
                .volume(ohlcv.getVolume())
                .build();
    }

    private void deletePastRetentionOhlcv(IndiceId indiceId) {
        LocalDateTime expiredDateTime = LocalDateTime.now().minusMonths(finticsProperties.getOhlcvRetentionMonths());
        entityManager.createQuery(
                        "delete" +
                                " from IndiceOhlcvEntity" +
                                " where indiceId = :indiceId " +
                                " and dateTime < :expiredDateTime")
                .setParameter("indiceId", indiceId)
                .setParameter("expiredDateTime", expiredDateTime)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

}
