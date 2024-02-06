package org.oopscraft.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.converter.AbstractEnumConverter;
import org.oopscraft.fintics.dao.AssetEntity;

import javax.persistence.Converter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Asset {

    private String assetId;

    private String assetName;

    private Type type;

    private LocalDateTime dateTime;

    private BigDecimal marketCap;

    private BigDecimal issuedShares;

    private BigDecimal per;

    private BigDecimal roe;

    private BigDecimal roa;

    public String getSymbol() {
        return Optional.ofNullable(getAssetId())
                .map(string -> string.split("\\."))
                .filter(array -> array.length > 1)
                .map(array -> array[1])
                .orElseThrow(() -> new RuntimeException(String.format("invalid assetId[%s]", getAssetId())));
    }

    public List<Link> getLinks() {
        return Optional.ofNullable(getAssetId())
                .map(value -> value.split("\\."))
                .filter(array -> array.length > 1)
                .map(array -> {
                    String exchangeId = array[0];
                    String symbol = array[1];
                    List<Link> links = new ArrayList<>();
                    switch (exchangeId) {
                        case "KR" ->
                                links.add(Link.of("Naver", "https://finance.naver.com/item/main.naver?code=" + symbol));
                        case "UPBIT" ->
                                links.add(Link.of("UPBIT", "https://upbit.com/exchange?code=CRIX.UPBIT." + symbol));
                    }
                    return links;
                })
                .orElse(new ArrayList<>());
    }

    public enum Type {
        STOCK, ETF
    }

    @Converter(autoApply = true)
    public static class TypeConverter extends AbstractEnumConverter<Type> { }

    public static Asset from(AssetEntity assetEntity) {
        return Asset.builder()
                .assetId(assetEntity.getAssetId())
                .assetName(assetEntity.getAssetName())
                .dateTime(assetEntity.getDateTime())
                .type(assetEntity.getType())
                .marketCap(assetEntity.getMarketCap())
                .issuedShares(assetEntity.getIssuedShares())
                .per(assetEntity.getPer())
                .roe(assetEntity.getRoe())
                .roa(assetEntity.getRoa())
                .build();
    }

}
