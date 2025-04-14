# FINTICS (Financial System Trading Application)

[![Sponsor](https://img.shields.io/badge/Sponsor-%E2%9D%A4-green?logo=github)](https://github.com/sponsors/chomookun)
[![Donate](https://img.shields.io/badge/Donate-Ko--fi-green?logo=kofi)](https://ko-fi.com/chomookun)

If you don't have your own investment strategy and philosophy, Don't do it. 
If you mess up, you'll be in big trouble.
This program only automates your own investment strategy and philosophy.

![](docs/assets/image/gambling-raccon.gif)

![](docs/assets/image/gambling-dog.gif)


## 🖥️ Demo site

username/password: developer/developer

### [![](https://img.shields.io/badge/Cloud%20Run-https://gcp.fintics--web.chomookun.org-blue?logo=google-cloud)](https://gcp.fintics-web.chomookun.org)

Due to a cold start, there is an initialization delay of approximately 30 seconds. (No money!!!)


## 🧪 Running from source

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
./gradlew :fintics:fintics-daemon:bootRun
```

### Starts fintics-web
Runs the UI management web application.
```shell
# starts fintics-web
./gradlew :fintics:fintics-web:bootRun
```

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

## 🧪 Running from container image

### Starts fintics-daemon
```shell
docker run -rm -p 8081:8081 docker.io/chomoookun/fintics-daemon:latest
```

### Starts fintics-web
```shell
docker run -rm -p 8080:8080 docker.io/chomoookun/fintics-web:latest
```


## 🔗 References

### [![](https://img.shields.io/badge/Github-https://github.com/chomoomun/fintics-green?logo=github)](https://github.com/chomookun/fintics)
Git source repository

### [![](https://img.shields.io/badge/Arch4j-https://github.com/chomookun/arch4j-red?logo=github)](https://github.com/chomookun/arch4j)
Arch4j framework based on spring boot


## 💼 My passive EMP(ETF Managed Portfolio)

### Concept
- Seeking a Balance Between Growth and Dividend
- Hedging Through a Balanced Allocation of Growth, Dividends, and Bonds
- Targeting Stable Cash Flow via Monthly Income Distributions

### Rebalance Strategy
- Buying at oversold level.
- Selling at overbought level.

ps. Technical Indicator: RSI, CCI, Stochastic Slow, Williams %R 

### 1. US Market (50% of passive)
Growth 35% + Dividend 35% + Bond 20% + Cash 10%

#### [35%] Growth Equity ETF
| Symbol   | Name                                             | Holding weight | Reference |
|----------|--------------------------------------------------|----------------|----------------------------------------------------------------------------|
| **JEPQ** | JPMorgan Nasdaq Equity Premium Income ETF        | 7% | [Nasdaq](https://www.nasdaq.com/market-activity/etf/jepq/dividend-history) |
| **GPIQ** | Goldman Sachs Nasdaq-100 Core Premium Income ETF | 7% | [Nasdaq](https://www.nasdaq.com/market-activity/etf/gpiq/dividend-history) |
| **IQQQ** | ProShares Nasdaq-100 High Income ETF | 7% | [Nasdaq](https://www.nasdaq.com/market-activity/etf/iqqq/dividend-history) |
| **BALI** | iShares Advantage Large Cap Income ETF | 7% | [Nasdaq](https://www.nasdaq.com/market-activity/etf/bali/dividend-history) |
| **GPIX** | Goldman Sachs S&P 500 Core Premium Income ETF | 7% | [Nasdaq](https://www.nasdaq.com/market-activity/etf/gpix/dividend-history) |

#### [35%] Dividend Equity ETF
| Symbol   | Name                                         | Holding weight | Reference |
|----------|----------------------------------------------|----------------|----------------------------------------------------------------------------|
| **DGRW** | WisdomTree U.S. Quality Dividend Growth Fund | 8.75% | [Nasdaq](https://www.nasdaq.com/market-activity/etf/dgrw/dividend-history) |
| **DIVO** | Amplify CPW Enhanced Dividend Income ETF | 8.75% | [Nasdaq](https://www.nasdaq.com/market-activity/etf/divo/dividend-history) |
| **JEPI** | JPMorgan Equity Premium Income ETF | 8.75% | [Nasdaq](https://www.nasdaq.com/market-activity/etf/jepi/dividend-history) |
| **RDVI** | FT Vest Rising Dividend Achievers Target Income ETF | 8.75% | [Nasdaq](https://www.nasdaq.com/market-activity/etf/rdvi/dividend-history) |

#### [20%] Bond ETF
| Symbol   | Name                                        | Holding weight | Reference |
|----------|---------------------------------------------|----------------|-----------------------------------------------------------------------------|
| **GOVI** | Invesco Equal Weight 0-30 Year Treasury ETF | 4%             | [Nasdaq](https://www.nasdaq.com/market-activity/etf/govi/dividend-history)  |
| **FBND** | Fidelity Total Bond ETF  | 4%             | [Nasdaq](https://www.nasdaq.com/market-activity/etf/fbnd/dividend-history)  |
| **JBND** | JPMorgan Active Bond ETF | 4%             | [Nasdaq](https://www.nasdaq.com/market-activity/etf/jbnd/dividend-history)  |
| **BOND** | PIMCO Active Bond ETF | 4%             | [Nasdaq](https://www.nasdaq.com/market-activity/etf/bond/dividend-history)  |
| **PYLD** | PIMCO Multisector Bond Active ETF | 4%             | [Nasdaq](https://www.nasdaq.com/market-activity/pyld/dgrw/dividend-history) |


### 2. KR Market (50% of passive)
US Growth 17.5% + US Dividend 17.5% + KR Growth 17.5% + KR Dividend 17.5% + US Bond 20% + Cash 10%

#### [17.5%] US Growth Equity ETF
| Symbol     | Name | Holding weight | Reference |
|------------|----|----------------|--------------------------------------------|
| **474220** | TIGER 미국테크TOP10타겟커버드콜 | 4.37% | [K-ETF](https://www.k-etf.com/etf/474220) |
| **486290** | TIGER 미국나스닥100타겟데일리커버드콜 | 4.37% | [K-ETF](https://www.k-etf.com/etf/486290) |
| **494300** | KODEX 미국나스닥100데일리커버드콜OTM | 4.37% | [K-ETF](https://www.k-etf.com/etf/494300) |
| **491620** | RISE 미국테크100데일리고정커버드콜 | 4.37% | [K-ETF](https://www.k-etf.com/etf/491620)  |

#### [17.5%] US Dividend Equity ETF
| Symbol     | Name | Holding weight | Reference |
|------------|------|----------------|-------------------------------------|
| **441640** | KODEX 미국배당커버드콜액티브 | 4.37% | [K-ETF](https://www.k-etf.com/etf/441640) |
| **483290** | KODEX 미국배당다우존스타겟커버드콜 | 4.37% | [K-ETF](https://www.k-etf.com/etf/483290) |
| **494420** | PLUS 미국배당증가성장주데일리커버드콜 | 4.37% | [K-ETF](https://www.k-etf.com/etf/494420) |
| **490600** | RISE 미국배당100데일리고정커버드콜 | 4.37% | [K-ETF](https://www.k-etf.com/etf/490600) |

#### [17.5%] KR Growth Equity ETF
| Symbol | Name | Holding weight | Reference |
|------|--|----------------|-------------------------------------|
| **472150** | TIGER 배당커버드콜액티브 | 5.83%          | [K-ETF](https://www.k-etf.com/etf/472150) |
| **498400** | KODEX 200타겟위클리커버드콜 | 5.83%          | [K-ETF](https://www.k-etf.com/etf/498400) |
| **496080** | TIGER 코리아밸류업 | 5.83%          | [K-ETF](https://www.k-etf.com/etf/496080) |

#### [17.5%] KR Dividend Equity ETF
| Symbol | Name | Holding weight | Reference |
|------|--|----------------|-------------------------------------|
| **441800** | TIMEFOLIO Korea플러스배당액티브 | 5.83%          | [K-ETF](https://www.k-etf.com/etf/441800) |
| **161510** | PLUS 고배당주 | 5.83%          | [K-ETF](https://www.k-etf.com/etf/161510) |
| **466940** | TIGER 은행고배당플러스TOP10 | 5.83%          | [K-ETF](https://www.k-etf.com/etf/466940) |

#### [20%] US Bond ETF
| Symbol     | Name | Holding weight | Reference |
|------------|------|----------------|-------------------------------------------|
| **476760** | ACE 미국30년국채액티브 | 4%             | [K-ETF](https://www.k-etf.com/etf/476760) |
| **476760** | ACE 미국30년국채엔화노출액티브(H) | 4%             | [K-ETF](https://www.k-etf.com/etf/476760) |
| **468370** | KODEX iShares미국인플레이션국채액티브 | 4%             | [K-ETF](https://www.k-etf.com/etf/468370) |
| **468630** | KODEX iShares미국투자등급회사채액티브 | 4%             | [K-ETF](https://www.k-etf.com/etf/468630) |
| **468380** | KODEX iShares미국하이일드액티브 | 4%             | [K-ETF](https://www.k-etf.com/etf/468380) |


