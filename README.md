# FINTICS (Financial System Trading Application)

Don't do it. If you mess up, you'll be in big trouble.

![](docs/assets/image/gambling-raccon.gif)

![](docs/assets/image/gambling-dog.gif)


## Starts applications

### Starts fintics-daemon

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
