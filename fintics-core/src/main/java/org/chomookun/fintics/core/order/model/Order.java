package org.chomookun.fintics.core.order.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.data.converter.GenericEnumConverter;
import org.chomookun.arch4j.core.common.support.ObjectMapperHolder;
import org.chomookun.fintics.core.order.entity.OrderEntity;
import org.chomookun.fintics.core.strategy.runner.StrategyResult;

import jakarta.persistence.Converter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class Order {

    private String orderId;

    private Instant orderAt;

    private Type type;

    private String tradeId;

    private String assetId;

    private String assetName;

    private Kind kind;

    private BigDecimal quantity;

    private BigDecimal price;

    private StrategyResult strategyResult;

    private BigDecimal purchasePrice;

    private BigDecimal realizedProfitAmount;

    private String brokerOrderId;

    private Result result;

    private String errorMessage;

    public enum Type { BUY, SELL }

    public enum Kind { LIMIT, MARKET }

    public enum Result { COMPLETED, FAILED }

    /**
     * Gets symbol
     * @return symbol
     */
    public String getSymbol() {
        return Optional.ofNullable(assetId)
                .map(string -> string.split("\\."))
                .filter(array -> array.length > 1)
                .map(array -> array[1])
                .orElseThrow(() -> new RuntimeException(String.format("invalid assetId[%s]",assetId)));
    }

    /**
     * Converts order entity to order
     * @param orderEntity order entity
     * @return order
     */
    public static Order from(OrderEntity orderEntity) {
        ObjectMapper objectMapper = ObjectMapperHolder.getObject();
        // strategy result
        StrategyResult strategyResult = null;
        if (orderEntity.getStrategyResultData() != null) {
           try {
               strategyResult = objectMapper.readValue(orderEntity.getStrategyResultData(), StrategyResult.class);
           } catch (JsonProcessingException ignore) {
               log.warn(ignore.getMessage());
           }
        }
        // returns
        return Order.builder()
                .orderId(orderEntity.getOrderId())
                .orderAt(orderEntity.getOrderAt())
                .type(orderEntity.getType())
                .tradeId(orderEntity.getTradeId())
                .assetId(orderEntity.getAssetId())
                .assetName(orderEntity.getAssetName())
                .kind(orderEntity.getKind())
                .quantity(orderEntity.getQuantity())
                .price(orderEntity.getPrice())
                .strategyResult(strategyResult)
                .purchasePrice(orderEntity.getPurchasePrice())
                .realizedProfitAmount(orderEntity.getRealizedProfitAmount())
                .brokerOrderId(orderEntity.getBrokerOrderId())
                .result(orderEntity.getResult())
                .errorMessage(orderEntity.getErrorMessage())
                .build();
    }

}
