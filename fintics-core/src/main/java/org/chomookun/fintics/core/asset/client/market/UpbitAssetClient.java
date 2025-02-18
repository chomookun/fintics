package org.chomookun.fintics.core.asset.client.market;

import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.chomookun.arch4j.core.common.support.RestTemplateBuilder;
import org.chomookun.fintics.core.asset.client.AssetClient;
import org.chomookun.fintics.core.asset.client.AssetClientProperties;
import org.chomookun.fintics.core.asset.model.Asset;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UpbitAssetClient extends AssetClient {

    private final RestTemplate restTemplate;

    public UpbitAssetClient(AssetClientProperties assetClientProperties) {
        super(assetClientProperties);

        // rest template
        this.restTemplate = RestTemplateBuilder.create()
                .httpRequestRetryStrategy(new DefaultHttpRequestRetryStrategy())
                .build();
    }

    @Override
    public List<Asset> getAssets() {
        RequestEntity<Void> requestEntity = RequestEntity
                .get("https://api.upbit.com/v1/market/all")
                .build();
        ResponseEntity<List<Map<String, String>>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>() {
        });
        List<Map<String,String>> responseBody = Optional.ofNullable(responseEntity.getBody()).orElseThrow();
        return responseBody.stream()
                .filter(map -> map.get("market").startsWith("KRW-"))
                .map(map -> Asset.builder()
                        .assetId(toAssetId("UPBIT", map.get("market")))
                        .name(map.get("english_name"))
                        .market("UPBIT")
                        .exchange("UPBIT")
                        .type("CRYPTO")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void populateAsset(Asset asset) {
        // not supported
    }

    @Override
    public boolean isSupport(Asset asset) {
        return false;
    }

}
