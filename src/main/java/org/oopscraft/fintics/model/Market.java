package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Market {

    private MarketIndicator spx;

    private MarketIndicator spxFuture;

    private MarketIndicator dji;

    private MarketIndicator djiFuture;

    private MarketIndicator ndx;

    private MarketIndicator ndxFuture;

}
