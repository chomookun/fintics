-- delete not used data
delete from `core_menu_role`;
delete from `core_menu_i18n`;
delete from `core_menu`;
delete from `core_git`;

-- core_authority
insert into `core_authority`
    (`authority_id`,`system_required`,`name`)
values
    ('monitor','Y','Monitor Access Authority'),
    ('asset','Y','Asset Access Authority'),
    ('asset:edit','Y','Asset Edit Authority'),
    ('basket','Y','Basket Access Authority'),
    ('basket:edit','Y','Basket Edit Authority'),
    ('strategy','Y','Strategy Access Authority'),
    ('strategy:edit','Y','Strategy Edit Authority'),
    ('broker','Y','Broker Access Authority'),
    ('broker:edit','Y','Broker Edit Authority'),
    ('trade','Y','Trade Access Authority'),
    ('trade:edit','Y','Trade Edit Authority'),
    ('order','Y','Order Access Authority'),
    ('balance','Y','Balance Access Authority');

-- core_role_authority
insert into `core_role_authority`
    (`role_id`,`authority_id`)
values
    ('USER','monitor'),
    ('USER','asset'),
    ('USER','basket'),
    ('USER','strategy'),
    ('USER','broker'),
    ('USER','trade'),
    ('USER','order'),
    ('USER','balance');

-- core_menu
insert into `core_menu`
    (`menu_id`,`system_required`,`enabled`,`parent_menu_id`,`link`,`target`,`sort`,`icon`)
values
    ('monitor','Y','Y',null,'/monitor',null,1,'/static/image/icon-monitor.svg'),
    ('asset','Y','Y',null,'/asset',null,2,'/static/image/icon-asset.svg'),
    ('basket','Y','Y',null,'/basket',null,3,'/static/image/icon-basket.svg'),
    ('strategy','Y','Y',null,'/strategy',null,4,'/static/image/icon-strategy.svg'),
    ('broker','Y','Y',null,'/broker',null,5,'/static/image/icon-broker.svg'),
    ('trade','Y','Y',null,'/trade',null,6,'/static/image/icon-trade.svg'),
    ('order','Y','Y',null,'/order',null,7,'/static/image/icon-order.svg'),
    ('admin','N','Y',null,'/admin','_blank',99,'/static/image/icon-admin.svg');

-- core_menu_i18n
insert into `core_menu_i18n`
    (`menu_id`,`locale`,`name`)
values
    ('monitor','en','Monitor'),
    ('asset','en','Asset'),
    ('basket','en','Basket'),
    ('strategy','en','Strategy'),
    ('broker','en','Broker'),
    ('trade','en','Trade'),
    ('order','en','Order'),
    ('profit','en','Profit'),
    ('admin','en','Admin'),
    ('monitor','ko','모니터'),
    ('asset','ko','종목'),
    ('basket','ko','바스켓'),
    ('strategy','ko','매매전략'),
    ('broker','ko','브로커'),
    ('trade','ko','트레이드'),
    ('order','ko','거래'),
    ('admin','ko','관리자');

-- core_notification
insert into `core_notifier` (`notifier_id`,`system_required`,`name`, `client_type`, `client_properties`) values
    ('fintics','Y','Fintics','TELEGRAM','bot-token=
chat-id=
');

-- core_menu_role
insert into `core_menu_role`
    (`menu_id`,`role_id`,`type`)
values
    ('monitor','USER','VIEW'),
    ('monitor','USER','LINK'),
    ('asset','USER','VIEW'),
    ('asset','USER','LINK'),
    ('basket','USER','VIEW'),
    ('basket','USER','LINK'),
    ('strategy','USER','VIEW'),
    ('strategy','USER','LINK'),
    ('broker','USER','VIEW'),
    ('broker','USER','LINK'),
    ('trade','USER','VIEW'),
    ('trade','USER','LINK'),
    ('order','USER','VIEW'),
    ('order','USER','LINK'),
    ('admin','USER','VIEW'),
    ('admin','USER','LINK');

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
    (`broker_id`,`name`,`sort`,`client_type`,`client_properties`)
values
    ('961eb9c68c9547ce9ae61bbe3be7f037','Korea Investment US Test',0,'KIS_US',null),
    ('ca5f55cd88694715bcb4c478710d9a68','Korea Investment Test',1,'KIS',null),
    ('a135ee9a276f4edf81d6e1b6b9d31e39','Upbit Test',2,'UPBIT',null);

-- fintics_basket
insert into `fintics_basket`
    (`basket_id`, `name`, `sort`, `market`)
values
    ('a920f8813c6f46fda2947cee1c8cfb1d','미국대형주', 0, 'US'),
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','국내대형주',1, 'KR'),
    ('7818b580e3f340498b97f50e0e801ff8','Upbit (24시간 테스트용)',2, 'UPBIT');
insert into `fintics_basket_asset`
    (`basket_id`,`asset_id`,`enabled`, `holding_weight`, `sort`)
values
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.488770','N',0, 0),
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.122630','Y',20, 1),
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.229200','Y',20, 2),
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.005930','Y',20, 3),
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','KR.000660','Y',20, 4),
    ('a920f8813c6f46fda2947cee1c8cfb1d','US.SPY','Y',20, 0),
    ('a920f8813c6f46fda2947cee1c8cfb1d','US.QQQ','Y',20, 1),
    ('a920f8813c6f46fda2947cee1c8cfb1d','US.AAPL','Y',20, 2),
    ('a920f8813c6f46fda2947cee1c8cfb1d','US.MSFT','Y',20, 3),
    ('7818b580e3f340498b97f50e0e801ff8','UPBIT.KRW-BTC','Y',40, 0),
    ('7818b580e3f340498b97f50e0e801ff8','UPBIT.KRW-ETH','Y',40, 1);
insert into `fintics_basket_divider`
    (`basket_id`,`divider_id`,`name`,`sort`)
values
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','36a9236308eb4f3bb369b435e519bc8d','ETF', 0),
    ('e5b2dda4ede54176b5e01eed7c4b9ed8','39c3320fe34d418db2937eff9fbdc1c8','Stock', 3),
    ('a920f8813c6f46fda2947cee1c8cfb1d','6d83494f2e5240749795fa9405e924ab','ETF', 0),
    ('a920f8813c6f46fda2947cee1c8cfb1d','d29dd523892243b980522d85ebec13db','Stock', 2);

-- fintics_strategy
insert into `fintics_strategy`
    (`strategy_id`,`name`,`language`,`script`)
values
    ('7c94187b346f4727a0f2478fdc53064f','Test Rule','GROOVY','return null');

-- fintics_trade
insert into `fintics_trade`
    (`trade_id`,`name`,`sort`,`enabled`,`interval`,`threshold`,`start_at`,`end_at`,`invest_amount`,`broker_id`,`basket_id`,`strategy_id`,`strategy_variables`,`notifier_id`,`order_kind`, cash_asset_id, cash_buffer_weight)
values
    ('06c228451ce0400fa57bb36f0568d7cb','한국투자증권 모의투자 - 국내', 1, 'Y','60','2','09:00','15:30','1000000','ca5f55cd88694715bcb4c478710d9a68','e5b2dda4ede54176b5e01eed7c4b9ed8','7c94187b346f4727a0f2478fdc53064f', null, null, 'LIMIT', 'KR.488770','1'),
    ('7af6bc641eef4254b12dd9fa1d43384d','한국투자증권 모의투자 - 미국', 2, 'Y','60','2','09:30','16:00','1000','961eb9c68c9547ce9ae61bbe3be7f037','a920f8813c6f46fda2947cee1c8cfb1d','7c94187b346f4727a0f2478fdc53064f', null, null, 'LIMIT', null, null),
    ('81c6a451d6da49449faa2b5b7e66041b','코인놀이방(24시간 테스트용)', 3, 'N','30','3','00:00','23:59','100000','a135ee9a276f4edf81d6e1b6b9d31e39','7818b580e3f340498b97f50e0e801ff8','7c94187b346f4727a0f2478fdc53064f', null, null, 'LIMIT', null, null);

-- fintics_order
insert into fintics_order
    (`order_id`, order_at, type, trade_id, asset_id, asset_name, quantity, result, error_message)
values
    ('36ac47501be24bd5b7cdb0255912e757', '2023-11-10 02:23:50.000', 'BUY', '06c228451ce0400fa57bb36f0568d7cb', 'KR.122630', 'KODEX 레버리지(테스트)', 262, 'FAILED', '모의투자 장시작전 입니다.'),
    ('a44181a8d6424dc78682b4fa8e4b0729', '2023-11-10 09:01:12.000', 'BUY', '06c228451ce0400fa57bb36f0568d7cb', 'KR.122630', 'KODEX 레버리지(테스트)', 264, 'COMPLETED', NULL),
    ('62b521b88ee742239753c5b1157d7407', '2023-11-10 14:47:14.000', 'SELL', '06c228451ce0400fa57bb36f0568d7cb', 'KR.122630', 'KODEX 레버리지(테스트)', 264, 'COMPLETED', NULL);

-- for test (paper trade)
update fintics_broker set client_properties='
production=false
apiUrl=https://openapivts.koreainvestment.com:29443
appKey=[appkey]
appSecret=[appSecret]
accountNo=[accountNo]
insecure=true
'
where broker_id in ('ca5f55cd88694715bcb4c478710d9a68','961eb9c68c9547ce9ae61bbe3be7f037');

update fintics_strategy set script = '
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


