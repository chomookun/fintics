-- delete not used data
delete from `core_menu_i18n`;
delete from `core_menu`;
delete from `core_git`;
delete from `core_alarm`;

-- core_authority
insert into `core_authority`
    (`authority_id`,`system_required`,`name`)
values
    ('api.assets','Y','Assets Access API Authority'),
    ('api.ohlcvs','Y','Ohlcvs API Authority'),
    ('api.dividends','Y','Dividends Access API Authority'),
    ('api.orders','Y','Orders API Authority'),
    ('api.trades','Y','Trades API Access Authority'),
    ('api.trades.edit','Y','Trades Edit API Authority'),
    ('api.baskets','Y','Baskets API Authority'),
    ('api.baskets.edit','Y','Baskets Edit API Authority'),
    ('api.strategies','Y','Strategies API Authority'),
    ('api.strategies.edit','Y','Strategies Edit API Authority'),
    ('api.brokers','Y','Brokers API Access Authority'),
    ('api.brokers.edit','Y','Brokers Edit API Authority'),
    ('api.profits','Y','Profits Access Authority'),
    ('monitors','Y','Monitor Access Authority'),
    ('assets','Y','Assets Access Authority'),
    ('baskets','Y','Baskets Access Authority'),
    ('basket','Y','Basket Access Authority'),
    ('basket.edit','Y','Baskets Edit Authority'),
    ('strategies','Y','Strategies Access Authority'),
    ('strategy','Y','Strategy Access Authority'),
    ('strategy.edit','Y','Strategy Edit Authority'),
    ('brokers','Y','Brokers Access Authority'),
    ('brokers.edit','Y','Brokers Edit Authority'),
    ('trades','Y','Trades Access Authority'),
    ('trade','Y','Trade Access Authority'),
    ('trade.edit','Y','Trade Edit Authority'),
    ('orders','Y','Orders Access Authority'),
    ('profits','Y','Profits Access Authority');

-- core_role_authority
insert into `core_role_authority`
    (`role_id`,`authority_id`)
values
    ('USER','api.assets'),
    ('USER','api.ohlcvs'),
    ('USER','api.dividends'),
    ('USER','api.orders'),
    ('USER','api.baskets'),
    ('USER','api.strategies'),
    ('USER','api.brokers'),
    ('USER','api.trades'),
    ('USER','api.profits'),
    ('USER','monitors'),
    ('USER','assets'),
    ('USER','baskets'),
    ('USER','basket'),
    ('USER','strategies'),
    ('USER','strategy'),
    ('USER','brokers'),
    ('USER','trades'),
    ('USER','trade'),
    ('USER','orders'),
    ('USER','profits');

-- core_menu
insert into `core_menu`
    (`menu_id`,`system_required`,`parent_menu_id`,`name`,`link`,`target`,`sort`,`icon`)
values
    ('monitors','Y',null,'Monitors','/monitors',null,1,'/static/image/icon-monitor.svg'),
    ('baskets','Y',null,'Basket','/baskets',null,2,'/static/image/icon-basket.svg'),
    ('strategies','Y',null,'Strategies','/strategies',null,3,'/static/image/icon-strategy.svg'),
    ('brokers','Y',null,'Brokers','/brokers',null,4,'/static/image/icon-broker.svg'),
    ('trades','Y',null,'Trades','/trades',null,5,'/static/image/icon-trade.svg'),
    ('assets','Y',null,'Assets','/assets',null,6,'/static/image/icon-asset.svg'),
    ('orders','Y',null,'Orders','/orders',null,7,'/static/image/icon-order.svg'),
    ('profits','Y',null,'Profits','/profits',null,8,'/static/image/icon-profit.svg'),
    ('admin','N',null,'Admin','/admin','_blank',99,'/static/image/icon-admin.svg');

-- core_menu_i18n
insert into `core_menu_i18n`
    (`menu_id`,`language`,`name`)
values
    ('monitors','ko','모니터'),
    ('baskets','ko','바스켓'),
    ('strategies','ko','매매전략'),
    ('brokers','ko','브로커'),
    ('trades','ko','트레이드'),
    ('assets','ko','종목'),
    ('orders','ko','거래'),
    ('profits','ko','수익현황'),
    ('admin','ko','관리자');

-- core_menu_role
insert into `core_menu_role`
    (`menu_id`,`role_id`,`type`)
values
    ('monitors','USER','VIEW'),
    ('monitors','USER','LINK'),
    ('baskets','USER','VIEW'),
    ('baskets','USER','LINK'),
    ('strategies','USER','VIEW'),
    ('strategies','USER','LINK'),
    ('brokers','USER','VIEW'),
    ('brokers','USER','LINK'),
    ('trades','USER','VIEW'),
    ('trades','USER','LINK'),
    ('assets','USER','VIEW'),
    ('assets','USER','LINK'),
    ('orders','USER','VIEW'),
    ('orders','USER','LINK'),
    ('profits','USER','VIEW'),
    ('profits','USER','LINK'),
    ('admin','USER','VIEW');

-- core_alarm
insert into `core_alarm`
    (`alarm_id`,`name`,`alarm_client_id`,`alarm_client_config`)
values
    ('fintics','Slack-Fintics','SLACK','url=https://hooks.slack.com/services/___');

-- fintics_asset
insert into `fintics_asset`
    (`asset_id`,`name`,`market`,`exchange`,`type`)
values
    ('KR.488770','KODEX 머니마켓액티브','KR','XKRX','ETF'),
    ('KR.122630','KODEX 레버리지','KR','XKRX','ETF'),
    ('KR.229200','KODEX 코스닥150','KR','XKRX','ETF'),
    ('KR.252670','KODEX 200선물인버스2X','KR','XKRX','ETF'),
    ('KR.251340','KODEX 코스닥150선물인버스','KR','XKRX','ETF'),
    ('KR.005930','삼성전자','KR','XKRX','STOCK'),
    ('KR.000660','에스케이하이닉스','KR','XKRX','STOCK'),
    ('US.SPY','SPDR S&P 500','US','XASE','ETF'),
    ('US.QQQ','Invesco QQQ Trust Series 1','US','XNAS','ETF'),
    ('US.AAPL','Apple Inc. Common Stock','US','XNAS','STOCK'),
    ('US.MSFT','Microsoft Corporation Common Stock','US','XNAS','STOCK'),
    ('UPBIT.KRW-BTC','Bitcoin','UPBIT','UPBIT',null),
    ('UPBIT.KRW-ETH','Ethereum','UPBIT','UPBIT',null);

-- fintics_broker
insert into `fintics_broker`
    (`broker_id`,`name`,`broker_client_id`,`broker_client_properties`)
values
    ('ca5f55cd88694715bcb4c478710d9a68','Korea Investment Test','KIS',null),
    ('961eb9c68c9547ce9ae61bbe3be7f037','Korea Investment US Test','KIS_US',null),
    ('a135ee9a276f4edf81d6e1b6b9d31e39','Upbit Test','UPBIT',null);

-- fintics_basket - 국내대형주
insert into `fintics_basket`
    (`basket_id`, `name`, `market`)
values
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','국내대형주', 'KR'),
    ('a920f8813c6f46fda2947cee1c8cfb1d','미국대형주', 'US'),
    ('7818b580e3f340498b97f50e0e801ff8','Upbit (24시간 테스트용)', 'UPBIT');

insert into `fintics_basket_asset`
    (`basket_id`,`asset_id`,`enabled`, `holding_weight`)
values
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.488770','N','0'),
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.122630','Y','20'),
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.229200','Y','20'),
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.005930','Y','20'),
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.000660','Y','20'),
    ('a920f8813c6f46fda2947cee1c8cfb1d','US.SPY','Y','20'),
    ('a920f8813c6f46fda2947cee1c8cfb1d','US.QQQ','Y','20'),
    ('a920f8813c6f46fda2947cee1c8cfb1d','US.AAPL','Y','20'),
    ('a920f8813c6f46fda2947cee1c8cfb1d','US.MSFT','Y','20'),
    ('7818b580e3f340498b97f50e0e801ff8','UPBIT.KRW-BTC','Y','40'),
    ('7818b580e3f340498b97f50e0e801ff8','UPBIT.KRW-ETH','Y','40');

-- fintics_strategy
insert into `fintics_strategy`
    (`strategy_id`,`name`,`language`,`script`)
values
    ('7c94187b346f4727a0f2478fdc53064f','Test Rule','GROOVY','return null');

-- fintics_trade
insert into `fintics_trade`
    (`trade_id`,`name`,`enabled`,`interval`,`threshold`,`start_at`,`end_at`,`invest_amount`,`broker_id`,`basket_id`,`strategy_id`,`strategy_variables`,`alarm_id`,`order_kind`, cash_asset_id, cash_buffer_weight)
values
    ('06c228451ce0400fa57bb36f0568d7cb','한국투자증권 모의투자 - 국내','Y','60','2','09:00','15:30','1000000','ca5f55cd88694715bcb4c478710d9a68','e5b2dda4ede54176b5e01eed7c4b9ed8','7c94187b346f4727a0f2478fdc53064f', null, null, 'LIMIT', 'KR.488770','1'),
    ('7af6bc641eef4254b12dd9fa1d43384d','한국투자증권 모의투자 - 미국','Y','60','2','09:30','16:00','1000','961eb9c68c9547ce9ae61bbe3be7f037','a920f8813c6f46fda2947cee1c8cfb1d','7c94187b346f4727a0f2478fdc53064f', null, null, 'LIMIT', null, null),
    ('81c6a451d6da49449faa2b5b7e66041b','코인놀이방(24시간 테스트용)','N','30','3','00:00','23:59','100000','a135ee9a276f4edf81d6e1b6b9d31e39','7818b580e3f340498b97f50e0e801ff8','7c94187b346f4727a0f2478fdc53064f', null, null, 'LIMIT', null, null);

-- fintics_order
insert into fintics_order
    (`order_id`, order_at, type, trade_id, asset_id, asset_name, quantity, result, error_message)
values
    ('36ac47501be24bd5b7cdb0255912e757', '2023-11-10 02:23:50.000', 'BUY', '06c228451ce0400fa57bb36f0568d7cb', 'KR.122630', 'KODEX 레버리지(테스트)', 262, 'FAILED', '모의투자 장시작전 입니다.'),
    ('a44181a8d6424dc78682b4fa8e4b0729', '2023-11-10 09:01:12.000', 'BUY', '06c228451ce0400fa57bb36f0568d7cb', 'KR.122630', 'KODEX 레버리지(테스트)', 264, 'COMPLETED', NULL),
    ('62b521b88ee742239753c5b1157d7407', '2023-11-10 14:47:14.000', 'SELL', '06c228451ce0400fa57bb36f0568d7cb', 'KR.122630', 'KODEX 레버리지(테스트)', 264, 'COMPLETED', NULL);







-- for test (paper trade)
update fintics_broker set broker_client_properties='
production=false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=[appkey]
appSecret=[appSecret]
accountNo=[accountNo]
insecure=true
'
where broker_id in ('ca5f55cd88694715bcb4c478710d9a68','961eb9c68c9547ce9ae61bbe3be7f037');

update fintics_strategy set script = '
import org.oopscraft.fintics.model.Ohlcv
import org.oopscraft.fintics.model.TradeAsset
import org.oopscraft.fintics.model.Strategy
import org.oopscraft.fintics.strategy.StrategyResult
import org.oopscraft.fintics.strategy.StrategyResult.Action
import org.oopscraft.fintics.trade.Tools
import org.oopscraft.fintics.indicator.*

def ohlcvs = tradeAsset.getOhlcvs(Ohlcv.Type.MINUTE, 1)
def ohlcv = ohlcvs.first()
def smas = Tools.indicators(ohlcvs, SmaContext.of(5));
def sma = smas.first()

def message = """
ohlcv: ${ohlcv}
sma: ${sma}
"""
tradeAsset.setMessage(message)
def context = tradeAsset.getContext()
context.put("prev_time", dateTime.toLocalTime());

if (ohlcv.close > sma.value) {
    return StrategyResult.of(Action.BUY, 1.0, "buy")
}
if (ohlcv.close < sma.value) {
    return StrategyResult.of(Action.SELL, 0.0, "sell")
}
'
where strategy_id = '7c94187b346f4727a0f2478fdc53064f';

update fintics_trade
set `interval` = '10',
    threshold = '1',
    order_kind='MARKET'
where trade_id in ('06c228451ce0400fa57bb36f0568d7cb','7af6bc641eef4254b12dd9fa1d43384d')
;


