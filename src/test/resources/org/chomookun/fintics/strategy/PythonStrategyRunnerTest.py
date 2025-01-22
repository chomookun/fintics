
print (f"basket_asset:{basket_asset.getAssetId()}")

ohlcvs = trade_asset.getOhlcvs("MINUTE", 3)
print (f"ohlcvs: {ohlcvs}")
ohlcvs2 = trade_asset.getOhlcvs("MINUTE", 1);
print (f"ohlcvs2: {ohlcvs2}")
strategy_result = {
    "action": "BUY",
    "position": 100.50,
    "description": "test"
}
log.info("###########################################")

