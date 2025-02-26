# FINTICS (Financial System Trading Application)

If you don't have your own investment strategy and philosophy, Don't do it. 
If you mess up, you'll be in big trouble.
This program only automates your own investment strategy and philosophy.

![](docs/assets/image/gambling-raccon.gif)

![](docs/assets/image/gambling-dog.gif)


## Starts applications

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
