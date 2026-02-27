# at-library

BDD-библиотека шагов для автоматизированного тестирования на основе:

| Технология | Назначение |
|---|---|
| **Cucumber** | BDD-сценарии на русском языке |
| **Selenide** | Web UI тестирование |
| **Rest-Assured** | REST API тестирование |
| **TestNG** | Запуск тестов |
| **Allure** | Отчётность |

Тест-кейсы пишутся на **русском языке** и представляют собой пользовательские сценарии (Gherkin).

---

## Архитектура проекта

Проект разбит на модули:

```
at-library/
├── at-library-core  — ядро: BDD-движок, хранилище переменных, утилиты, базовые шаги
├── at-library-web   — шаги для WEB-тестирования (Selenide + Page Object)
├── at-library-api   — шаги для REST API тестирования (Rest-Assured)
└── distribution     — сборка shaded JAR со всеми модулями
```

> Подробная документация по каждому модулю — в `README.md` соответствующей директории.

### Зависимости между модулями

```
at-library-web  ──► at-library-core
at-library-api  ──► at-library-core
distribution    ──► core + web + api
```

`at-library-core` подключается автоматически при использовании `at-library-web` или `at-library-api`.

---

## Подключение

Добавьте нужные модули в `pom.xml` вашего проекта:

```xml
<dependencies>
    <!-- Web UI тестирование -->
    <dependency>
        <groupId>ru</groupId>
        <artifactId>at-library-web</artifactId>
        <version>27.02.2026</version>
    </dependency>

    <!-- REST API тестирование -->
    <dependency>
        <groupId>ru</groupId>
        <artifactId>at-library-api</artifactId>
        <version>27.02.2026</version>
    </dependency>
</dependencies>
```

> Можно подключать модули по отдельности — только `at-library-web` или только `at-library-api`.

---

## Настройка плагинов Maven

### Компиляция

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.14.1</version>
    <configuration>
        <encoding>UTF-8</encoding>
        <source>21</source>
        <target>21</target>
    </configuration>
</plugin>
```

### Запуск тестов (Surefire + AspectJ)

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.3.1</version>
    <configuration>
        <threadCount>1</threadCount>
        <parallel>classes</parallel>
        <testFailureIgnore>true</testFailureIgnore>
        <argLine>
            -javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/1.9.22/aspectjweaver-1.9.22.jar"
            -Dcucumber.options="--plugin io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        </argLine>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.9.22</version>
        </dependency>
    </dependencies>
</plugin>
```

### Allure-отчёты

```xml
<plugin>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-maven</artifactId>
    <version>2.15.2</version>
    <configuration>
        <reportVersion>2.29.0</reportVersion>
        <resultsDirectory>allure-results</resultsDirectory>
    </configuration>
</plugin>
```

---

## Команды сборки и запуска

### Сборка

```bash
# Сборка всех модулей (без тестов)
mvn -DskipTests package

# Сборка shaded JAR (distribution)
mvn -pl distribution -am clean package
```

### Запуск тестов

```bash
# Все тесты
mvn clean test

# Только Web-модуль
mvn -pl at-library-web -am clean test

# Только API-модуль
mvn -pl at-library-api -am clean test

# Конкретный браузер
mvn -pl at-library-web -am clean test -Dselenide.browser=chrome
```

### Фильтрация по тегам

```bash
# Сценарии с тегом @api
mvn clean test -Dcucumber.options="--tags @api"

# Сценарии с любым из тегов (OR)
mvn clean test -Dcucumber.options="--tags '@api or @web'"

# Конкретный сценарий по имени
mvn clean test -Dcucumber.options="--name 'Имя сценария'"
```

### Allure-отчёт

```bash
# Сгенерировать и открыть отчёт (из директории модуля)
cd at-library-web && mvn allure:serve
```

---

## Конфигурация

Параметры тестов задаются через:

- **System properties** (`-DbaseURI=...`, `-Dselenide.browser=...`)
- **Файл `application.properties`** в `src/test/resources` вашего проекта

Пример `application.properties`:

```properties
# Базовый URL
baseURI=https://your-app.example.com

# Таймаут ожидания элементов (мс)
waitingAppearTimeout=15000
```

> Системные свойства (`-D...`) имеют приоритет над значениями из файла.
