package org.chomookun.fintics.core.trade;

import org.springframework.data.redis.listener.ChannelTopic;

public class TradeChannels {

    public static ChannelTopic TRADE_LOG = new ChannelTopic("fintics:trade:trade_log");

    public static ChannelTopic TRADE_ASSET = new ChannelTopic("fintics:trade:trade_asset");

}
