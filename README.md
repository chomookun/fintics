# FINTICS (Financial System Trading Application)

[![Sponsor](https://img.shields.io/badge/Sponsor-%E2%9D%A4-green?logo=github)](https://github.com/sponsors/chomookun)
[![Donate](https://img.shields.io/badge/Donate-Ko--fi-green?logo=kofi)](https://ko-fi.com/chomookun)

If you don't have your own investment strategy and philosophy, Don't do it.<br/> 
If you mess up, you'll be in big trouble.<br/>
This program only automates your own investment strategy.

![](docs/assets/image/gambling-raccon.gif)
![](docs/assets/image/gambling-dog.gif)

---

## 🖥️ Demo site

Credentials: **developer/developer**

### Management web application (google cloud run)
[![](https://img.shields.io/badge/Cloud%20Run-https://gcp.fintics--web.chomookun.org-blue?logo=google-cloud)](https://gcp.fintics-web.chomookun.org)
<br/>
Due to a cold start, there is an initialization delay of approximately 30 seconds.<br/>
(No money!!!)

### Trading daemon application
![](https://img.shields.io/badge/N/A-Not%20available-red?logo=)
<br/>
Trading daemon is not available on the demo site.<br/>
(No money!!!)

---

## 🧪 Running from source code

### Configures Gradle 
Adds private maven repository
```shell
vim ~/.gradle/init.gradle
...
allprojects {
    repositories {
        // ...
        maven {
            url = "https://nexus.chomookun.org/repository/maven-public/"
        }
        // ...
    }
}
...
```

### Starts fintics-daemon
Runs the trading daemon application.
```shell
# starts fintics-daemon
./gradlew :fintics-daemon:bootRun
```

### Starts fintics-web
Runs the UI management web application.
```shell
# starts fintics-web
./gradlew :fintics-web:bootRun
```

---

## 🧪 Running from release binary

Downloads Released archives.

### Starts fintics-daemon

```shell
./bin/fintics-daemon
```

### Starts fintics-web
```shell
./bin/fintics-web
```

---

## 🧪 Running from container image

### Starts fintics-daemon
```shell
docker run -rm -p 8081:8081 docker.io/chomoookun/fintics-daemon:latest
```

### Starts fintics-web
```shell
docker run -rm -p 8080:8080 docker.io/chomoookun/fintics-web:latest
```

---

## 🔗 References

### Git source repository
[![](https://img.shields.io/badge/Github-https://github.com/chomoomun/fintics-green?logo=github)](https://github.com/chomookun/fintics)

### Arch4j framework (based on spring boot)
[![](https://img.shields.io/badge/Arch4j-https://github.com/chomookun/arch4j-red?logo=github)](https://github.com/chomookun/arch4j)

---

## 💼 My Passive EMP(ETF Managed Portfolio)

### Concept
- Seeking a Balance Between Growth and Dividend
- Hedging Through a Balanced Allocation of Growth, Dividends, and Bonds
- Targeting Stable Cash Flow via Monthly Income Distributions

### Rebalance Strategy
- Buying at oversold level.
- Selling at overbought level.

ps. Technical Indicator: RSI, CCI, Stochastic Slow, Williams %R 

### 1. US Market (50% of Passive EMP)
Equity(Growth) 35% + Equity(Dividend) 35% + Bond(Sovereign) 10% + Bond(Aggregate) 10% + Cash Equivalent 10%

#### [35%] Equity(Growth) ETF

| Symbol | Name    | Holding weight | Reference                                                                  |
|------|---------|----------------|----------------------------------------------------------------------------|
| **QDVO** | Amplify CWP Growth & Income ETF | 8.75%          | [Nasdaq](https://www.nasdaq.com/market-activity/etf/gdvo/dividend-history) |
| **GPIQ** | Goldman Sachs Nasdaq-100 Core Premium Income ETF | 8.75%          | [Nasdaq](https://www.nasdaq.com/market-activity/etf/gpiq/dividend-history) |
| **JEPQ** | JPMorgan Nasdaq Equity Premium Income ETF | 8.75%          | [Nasdaq](https://www.nasdaq.com/market-activity/etf/jepq/dividend-history) |
| **SPUS** | SP Funds S&P 500 Sharia Industry Exclusions ETF        | 8.75%          | [Nasdaq](https://www.nasdaq.com/market-activity/etf/spus/dividend-history) |

#### [35%] Equity(Dividend) ETF

| Symbol | Name       | Holding weight | Reference                                                                  |
|------|------------|----------------|----------------------------------------------------------------------------|
| **DGRW** | WisdomTree U.S. Quality Dividend Growth Fund | 7.0%           | [Nasdaq](https://www.nasdaq.com/market-activity/etf/dgrw/dividend-history) |
| **DIVO** | Amplify CPW Enhanced Dividend Income ETF | 7.0%           | [Nasdaq](https://www.nasdaq.com/market-activity/etf/divo/dividend-history) |
| **BALI** | iShares Advantage Large Cap Income ETF | 7.0%           | [Nasdaq](https://www.nasdaq.com/market-activity/etf/bali/dividend-history) |
| **JEPI** | JPMorgan Equity Premium Income ETF | 7.0%           | [Nasdaq](https://www.nasdaq.com/market-activity/etf/jepi/dividend-history) |
| **IDVO** | Amplify CWP International Enhanced Dividend Income ETF | 7.0%           | [Nasdaq](https://www.nasdaq.com/market-activity/etf/idvo/dividend-history) |

#### [10%] Bond(Sovereign) ETF

| Symbol   | Name                   | Holding weight | Reference                                                                  |
|----------|------------------------|----------------|----------------------------------------------------------------------------|
| **GOVI** | Invesco Equal Weight 0-30 Year Treasury ETF | 5%             | [Nasdaq](https://www.nasdaq.com/market-activity/etf/govi/dividend-history) |
| **TIP**  | iShares TIPS Bond ETF | 5%             | [Nasdaq](https://www.nasdaq.com/market-activity/etf/tip/dividend-history)  |

#### [10%] Bond(Aggregate) ETF
| Symbol   | Name                           | Holding weight | Reference                                                                  |
|----------|--------------------------------|----------------|----------------------------------------------------------------------------|
| **FBND** | Fidelity Total Bond ETF | 3.33%          | [Nasdaq](https://www.nasdaq.com/market-activity/etf/fbnd/dividend-history) |
| **PYLD** | PIMCO Multisector Bond Active ETF | 3.33%          | [Nasdaq](https://www.nasdaq.com/market-activity/etf/pyld/dividend-history) |
| **IGLD** | FT Vest Gold Strategy Target Income ETF | 3.33%          | [Nasdaq](https://www.nasdaq.com/market-activity/etf/igld/dividend-history) |

#### [10%] Cash Equivalent ETF
| Symbol   | Name                   | Holding weight | Reference                                                                  |
|----------|------------------------|----------------|----------------------------------------------------------------------------|
| **SGOV** | iShares 0-3 Month Treasury Bond ETF | 10%            | [Nasdaq](https://www.nasdaq.com/market-activity/etf/sgov/dividend-history) |


### 2. KR Market (50% of Passive EMP)
US.Equity(Growth) 17.5% + US.Equity(Dividend) 17.5% + KR.Equity(Growth) 17.5% + KR.Equity(Dividend) 17.5% + US.Bond(Sovereign) 10% + US.Bond(Aggregate) 10% + Cash Equivalent 10%

#### [17.5%] US.Equity(Growth) ETF

| Symbol     | Name | Holding weight | Reference |
|------------|--|----------------|-------------------------------------|
| **474220** | TIGER 미국테크TOP10타겟커버드콜 | 5.83%          | [K-ETF](https://www.k-etf.com/etf/474220) |
| **0144L0** | KODEX 미국성장커버드콜액티브 | 5.83%          | [K-ETF](https://www.k-etf.com/etf/0144L0) |
| **486290** | TIGER 미국나스닥100타겟데일리커버드콜 | 5.83%          | [K-ETF](https://www.k-etf.com/etf/486290) |

#### [17.5%] US.Equity(Dividend) ETF

| Symbol | Name                | Holding weight | Reference                            |
|------|---------------------|----------------|--------------------------------------|
| **441640** | KODEX 미국배당커버드콜액티브   | 5.83%          | [K-ETF](https://www.k-etf.com/etf/441640) |
| **0046Y0** | ACE 미국배당퀄리티         | 5.83%          | [K-ETF](https://www.k-etf.com/etf/0046Y0) |
| **0049M0** | ACE 미국배당퀄리티+커버드콜액티브 | 5.83%          | [K-ETF](https://www.k-etf.com/etf/0049M0)  |

#### [17.5%] KR.Equity(Growth) ETF

| Symbol | Name               | Holding weight | Reference |
|------|--------------------|----------------|-------------------------------------|
| **472150** | TIGER 배당커버드콜액티브    | 5.83%          | [K-ETF](https://www.k-etf.com/etf/472150) |
| **498400** | KODEX 200타겟위클리커버드콜 | 5.83%          | [K-ETF](https://www.k-etf.com/etf/498400) |
| **496080** | TIGER 코리아밸류업       | 5.83%          | [K-ETF](https://www.k-etf.com/etf/496080) |

#### [17.5%] KR.Equity(Dividend) ETF

| Symbol | Name   | Holding weight | Reference |
|------|--------|----------------|-------------------------------------|
| **441800** | TIMEFOLIO Korea플러스배당액티브 | 4.37%          | [K-ETF](https://www.k-etf.com/etf/441800) |
| **161510** | PLUS 고배당주 | 4.37%          | [K-ETF](https://www.k-etf.com/etf/161510) |
| **279530** | KODEX 고배당주       | 4.37%          | [K-ETF](https://www.k-etf.com/etf/279530) |
| **0052D0** | TIGER 코리아배당다우존스 | 4.37%          | [K-ETF](https://www.k-etf.com/etf/0052D0) |

#### [10%] US.Bond(Sovereign) ETF

| Symbol | Name | Holding weight | Reference                                 |
|------|--|----------------|-------------------------------------------|
| **476760** | ACE 미국30년국채액티브 | 3.33%          | [K-ETF](https://www.k-etf.com/etf/476760) |
| **0085P0** | ACE 미국10년국채액티브 | 3.33%          | [K-ETF](https://www.k-etf.com/etf/0085P0)       |
| **468370** | KODEX iShares미국인플레이션국채액티브 | 3.33%          | [K-ETF](https://www.k-etf.com/etf/468370) |

#### [10%] US.Bond(Aggregate) ETF

| Symbol | Name | Holding weight | Reference                                                            |
|------|----|----------------|----------------------------------------------------------------------|
| **468630** | KODEX iShares미국투자등급회사채액티브 | 3.33%          | [K-ETF](https://www.k-etf.com/etf/468630) |
| **468380** | KODEX iShares미국하이일드액티브 | 3.33%          | [K-ETF](https://www.k-etf.com/etf/468380) |
| **0022T0** | SOL 국제금커버드콜액티브 | 3.33%          | [K-ETF](https://www.k-etf.com/etf/0022T0) |

#### [10%] Cash Equivalent ETF
| Symbol   | Name | Holding weight | Reference                                                            |
|----------|--|---------------|----------------------------------------------------------------------|
| **488770** | KODEX 머니마켓액티브 | 10%           | [K-ETF](https://www.k-etf.com/etf/488770) |

