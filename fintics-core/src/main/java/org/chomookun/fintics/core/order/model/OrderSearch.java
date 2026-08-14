package org.chomookun.fintics.core.order.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Builder
@Getter
public class OrderSearch {

    private Instant orderAtFrom;

    private Instant orderAtTo;

    @Setter
    private String tradeId;

    private String assetId;

    private String assetName;

    private Order.Type type;

    private Order.Result result;

}
