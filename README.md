[![Build Status](https://travis-ci.org/valery1707/test-serialization.svg)](https://travis-ci.org/valery1707/test-serialization)
[![Coverage Status](https://coveralls.io/repos/valery1707/test-serialization/badge.svg?branch=master&service=github)](https://coveralls.io/github/valery1707/test-serialization?branch=master)

##### Запуск тестового приложения: 
Команда: `./gradlew :test-app:bootRun`

Требования к компьютеру:

1. JDK 1.8+
1. JavaFX (Oracle JDK сразу устанавливает этот модуль, а для OpenJDK необходимо дополнительно установить модуль `openjfx`)

##### Отчёт покрытия тестами:
Команда: `./gradlew clean build jacocoRootReport`

Файл отчёта: /build/reports/jacoco/jacocoRootReport/html/index.html
