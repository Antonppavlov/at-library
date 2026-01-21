at-library
=========================

[![](https://jitpack.io/v/AtLibrary/at-library.svg)](https://jitpack.io/#AtLibrary/at-library)

BDD библиотека шагов для тестирования на основе:

- Cucumber
- Selenide
- Rest-Assured
- Allure

Тест-кейсы пишутся на русском языке и представляют собой пользовательские сценарии.

Архитектура проекта
====================

Проект разбит на 3 модуля:

- **at-library-api** — шаги для написания API-тестов
- **at-library-core** — общий набор шагов и классов утилит (подключается по умолчанию при подключении любого модуля с шагами)
- **at-library-web** — шаги для написания WEB-тестов

В каждом модуле создан файл `README.md`, описывающий подключение и работу с этим модулем.

В этом (корневом) `README.md` описано, как подключить библиотеку и необходимые плагины в `pom.xml`.

Подключение через JitPack
====================

Добавьте репозиторий JitPack:

```xml path=null start=null
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Подключение зависимостей `at-library` из JitPack:

```xml path=null start=null
<properties>
    <!-- Рекомендуется зафиксировать конкретный релиз или hash -->
    <version.at-library>3a0b4d6b08</version.at-library>
</properties>

<dependencies>
    <dependency>
        <groupId>com.github.AtLibrary.at-library</groupId>
        <artifactId>at-library-core</artifactId>
        <version>${version.at-library}</version>
    </dependency>
    <dependency>
        <groupId>com.github.AtLibrary.at-library</groupId>
        <artifactId>at-library-web</artifactId>
        <version>${version.at-library}</version>
    </dependency>
</dependencies>
```

Пример настройки плагинов Maven
====================

Плагин компиляции:

```xml path=null start=null
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.0</version>
    <configuration>
        <encoding>UTF-8</encoding>
        <source>1.8</source>
        <target>1.8</target>
    </configuration>
</plugin>
```

Плагин запуска тестов и генерации отчётов (пример конфигурации):

```xml path=null start=null
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.22.1</version>
    <configuration>
        <threadCount>1</threadCount>
        <parallel>classes</parallel>
        <testFailureIgnore>true</testFailureIgnore>
        <argLine>
            -javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/1.9.1/aspectjweaver-1.9.1.jar"
            -Dcucumber.options="--plugin io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        </argLine>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.9.1</version>
        </dependency>
    </dependencies>
</plugin>
```

Плагин для генерации и просмотра Allure-отчётов:

```xml path=null start=null
<plugin>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-maven</artifactId>
    <version>2.10.0</version>
    <configuration>
        <reportVersion>2.10.0</reportVersion>
        <resultsDirectory>allure-results</resultsDirectory>
    </configuration>
</plugin>
```
