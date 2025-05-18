package org.chomookun.fintics.core.trade;

import org.springframework.data.redis.listener.ChannelTopic;

public class TradeChannels {

    public static final String TRADE_LOG = "fintics:trade:trade_log";

    public static final ChannelTopic TRADE_LOG_CHANNEL = ChannelTopic.of(TRADE_LOG);

    public static final String TRADE_ASSET = "fintics:trade:trade_asset";

    public static final ChannelTopic TRADE_ASSET_CHANNEL = ChannelTopic.of(TRADE_ASSET);

}
