# at-library — заметки для быстрой работы в репозитории

Это BDD step-библиотека (Cucumber на русском + Selenide + Rest-Assured + TestNG + Allure), разбитая на модули `at-library-core` / `at-library-web` / `at-library-api` / `distribution`. Общее описание архитектуры и подключения — в [README.md](README.md); здесь только то, что README не покрывает и что стоит знать до первого запуска.

## Сборка и тесты — практические команды

Сборка прибита к **JDK 21 через Maven Toolchains** (`maven-toolchains-plugin` в корневом `pom.xml`, validate-фаза). Причина: JVM, на которой запущен сам `mvn`, и JDK, который нужен проекту (`java.version=21` в properties), на машине с несколькими JDK — не одно и то же. На этой машине, например, `mvn -version` показывает JDK 26 (Homebrew'вский `openjdk`, берётся по умолчанию, т.к. `JAVA_HOME` не выставлен), а обычный `java -version` в шелле — JDK 21 (системный, зарегистрированный через `java_home`). Раньше это молча приводило к тому, что forked JVM surefire получала JDK 26, и `aspectjweaver:1.9.22` падал на её байткоде (`Unsupported class file major version 70`) — подробности и как это проявлялось см. историю в [at-library-core/CLAUDE.md](at-library-core/CLAUDE.md). Теперь toolchain явно прибивает к JDK 21 и `maven-compiler-plugin`, и `maven-surefire-plugin`, независимо от того, на чём запущен сам Maven. Проверено: `mvn -pl at-library-core test` → `Tests run: 41, Failures: 0`.

**На новой машине нужно один раз создать `~/.m2/toolchains.xml`** (локальный для машины, в git не хранится):
```xml
<toolchains xmlns="http://maven.apache.org/TOOLCHAINS/1.1.0">
    <toolchain>
        <type>jdk</type>
        <provides><version>21</version><vendor>oracle</vendor></provides>
        <configuration><jdkHome>ПУТЬ_К_JDK_21</jdkHome></configuration>
    </toolchain>
</toolchains>
```
Путь на macOS — `/usr/libexec/java_home -v 21`. Без этого файла сборка **осознанно** падает уже на `validate` с понятной `Cannot find matching toolchain for type jdk` — это фейл-фаст вместо невнятных AspectJ-ошибок внутри surefire, не баг.

Чтобы прогнать **только web-тесты**, не дожидаясь тестов core/api (`-am` всё равно потянет их тесты тоже — просто теперь они не падают, а лишь отнимают время):
```bash
mvn -pl at-library-web -am test \
  -Dtest=ru.at.library.web.RunFeaturesTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dselenide.headless=true -Dselenide.browser=chrome
```
`-Dtest=...` ограничивает surefire этим классом **во всём реакторе**, включая `at-library-core` — там такого класса нет, и с `-Dsurefire.failIfNoSpecifiedTests=false` это просто пропускается вместо падения "no tests found".

Фильтрация — через отдельные свойства, не через `-Dcucumber.options=...`: по тегам `-Dcucumber.filter.tags='@api or @web'`, по конкретному сценарию/группе `-Dcucumber.filter.name='подстрока1|подстрока2'` (регулярка, OR через `|`). **`-Dcucumber.options=...` не работает** — этот способ передачи опций устарел в используемой версии Cucumber и тихо игнорируется с предупреждением; README и pom.xml тоже переведены на `cucumber.filter.*`, дублирующей мёртвой строки в surefire `argLine` больше нет.

На этой машине нет `timeout`/`gtimeout` (не GNU coreutils) — не полагаться на них в командах, использовать фоновый запуск (`run_in_background`) вместо блокирующего таймаута.

## Selenium Manager иногда виснет

Автоподбор chromedriver через Selenium Manager может зависнуть на 90+ секунд (подтверждено `jstack`: блокируется в `SeleniumManager.runCommand`/`ExternalProcess.waitFor`), особенно после длинной серии запусков. Обычно само проходит при следующей попытке, но если прогон завис сразу после старта (время идёт, CPU ~0%, лога нет) — можно обойти явным указанием драйвера:
```bash
-Dwebdriver.chrome.driver=$(ls -d ~/.cache/selenium/chromedriver/mac-arm64/*/ | sort -V | tail -1)chromedriver
```
Если версия Chrome обновилась (сам обновляется в фоне), а кэш держит старую chromedriver — тоже источник странных `StaleElementReferenceException`/`renderer timeout`. При сомнениях — просто убрать явный `-Dwebdriver.chrome.driver` и дать Selenium Manager подобрать актуальную версию заново.

Долгая сессия с частыми `kill`/остановками фоновых `mvn test` оставляет осиротевшие `chromedriver`/`selenium-manager`/Chrome-процессы (убийство родителя не каскадируется на детей). Если что-то ведёт себя странно — проверить `ps aux | grep -iE "chromedriver|selenium-manager"`.

## Подробности по модулям

- [at-library-web/CLAUDE.md](at-library-web/CLAUDE.md) — тестовая архитектура web-модуля, конвенции написания шагов, известные ловушки Wikipedia-фикстуры.
