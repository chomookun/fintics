package org.oopscraft.fintics.collector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.FinticsProperties;
import org.oopscraft.fintics.client.news.NewsClient;
import org.oopscraft.fintics.dao.NewsEntity;
import org.oopscraft.fintics.dao.NewsRepository;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.News;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetNewsCollector extends AbstractCollector {

    private final FinticsProperties finticsProperties;

    private final ObjectMapper objectMapper;

    private final PlatformTransactionManager transactionManager;

    private final TradeRepository tradeRepository;

    private final NewsClient newsClient;

    private final NewsRepository assetNewsRepository;

    @Scheduled(initialDelay = 10_000, fixedDelay = 3600_000)
    public void collect() {
        try {
            log.info("AssetNewsCollector - Start collect news.");
            // asset
            List<TradeEntity> tradeEntities = tradeRepository.findAll();
            for (TradeEntity tradeEntity : tradeEntities) {
                Trade trade = Trade.from(tradeEntity);
                if (trade.isEnabled()) {
                    for (TradeAsset tradeAsset : trade.getTradeAssets()) {
                        try {
                            if (tradeAsset.isEnabled()) {
                                collectAssetNews(tradeAsset);
                            }
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                            sendSystemAlarm(this.getClass(), String.format("[%s] %s - %s", tradeEntity.getTradeName(), tradeAsset.getAssetName(), e.getMessage()));
                        }
                    }
                }
            }
            log.info("AssetNewsCollector - End collect news");
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    void collectAssetNews(Asset asset) {
        List<News> assetNewses = distinctAssetNewsesByTitle(newsClient.getNewses(asset));
        for (News assetNews : assetNewses) {
            try {
                String newsId = IdGenerator.md5(assetNews.getNewsUrl());
                NewsEntity assetNewsEntity = assetNewsRepository.findById(NewsEntity.Pk.builder()
                                .assetId(newsId)
                                .datetime(assetNews.getDatetime())
                                .newsId(assetNews.getNewsId())
                                .build())
                        .orElse(null);
                if (assetNewsEntity == null) {
                    assetNewsEntity = NewsEntity.builder()
                            .assetId(asset.getAssetId())
                            .datetime(assetNews.getDatetime())
                            .newsId(newsId)
                            .newsUrl(assetNews.getNewsUrl())
                            .title(assetNews.getTitle())
                            .build();
                }

                // analysis
                if (assetNewsEntity.getSentiment() == null) {
                    analysisAssetNews(assetNewsEntity);
                }

                // save news
                String unitName = String.format("assetNewsEntity[%s]: %s", asset.getAssetName(), assetNewsEntity.getTitle());
                saveEntities(unitName, List.of(assetNewsEntity), transactionManager, assetNewsRepository);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
    }

    List<News> distinctAssetNewsesByTitle(List<News> assetNewses) {
        return new ArrayList<>(assetNewses.stream()
                .collect(Collectors.toMap(
                        News::getTitle, // key
                        news -> news,   // value
                        (existing, replacement) -> existing // check existing
                ))
                .values());
    }

    void analysisAssetNews(NewsEntity newsEntity) {
        // config not setting
        if (StringUtils.isBlank(finticsProperties.getAiApiUrl())) {
            return;
        }
        try {
            RestTemplate restTemplate = RestTemplateBuilder.create()
                    .insecure(true)
                    .readTimeout(60_000)
                    .build();
            String url = finticsProperties.getAiApiUrl() + "/news";
            Map<String,String> payload = new LinkedHashMap<>(){{
                put("url", newsEntity.getNewsUrl());
                put("title", newsEntity.getTitle());
            }};
            RequestEntity<Map<String,String>> requestEntity = RequestEntity
                    .post(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload);
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            String responseBody = responseEntity.getBody();

            Map<String,String> responseMap = objectMapper.readValue(responseBody, new TypeReference<>() {});
            String sentiment = responseMap.get("sentiment");
            String confident = responseMap.get("confidence");
            String reason = responseMap.get("reason");
            newsEntity.setSentiment(News.Sentiment.valueOf(sentiment.toUpperCase(Locale.ROOT)));
            newsEntity.setConfidence(new BigDecimal(confident));
            newsEntity.setReason(reason);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }


}
