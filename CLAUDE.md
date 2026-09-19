# at-library — заметки для быстрой работы в репозитории

Это BDD step-библиотека (Cucumber на русском + Selenide + Rest-Assured + TestNG + Allure), разбитая на модули `at-library-core` / `at-library-web` / `at-library-api` / `distribution`. Общее описание архитектуры и подключения — в [README.md](README.md); здесь только то, что README не покрывает и что стоит знать до первого запуска.

## Сборка и тесты — практические команды

`mvn clean test` из корня **упадёт** на `at-library-core`: там 3 теста стабильно падают из-за несовместимости `aspectjweaver:1.9.22` с байткодом JDK 26 (`Unsupported class file major version 70`, class-file version 70 = Java 26). Это окружение/версийная проблема, не баг в тестах — не пытаться «починить» AspectJ, просто не гонять `at-library-core`'s тесты заодно, когда не нужно.

Чтобы прогнать **только реальные web-тесты**, не упираясь в это:
```bash
mvn -pl at-library-web -am test \
  -Dtest=ru.at.library.web.RunFeaturesTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dselenide.headless=true -Dselenide.browser=chrome
```
`-Dtest=...` ограничивает surefire этим классом **во всём реакторе**, включая `at-library-core` — там такого класса нет, и с `-Dsurefire.failIfNoSpecifiedTests=false` это просто пропускается вместо падения. Без этого флага `-am` соберёт core и заодно попытается прогнать его собственный (падающий) suite.

Фильтрация по конкретному сценарию/группе — через `-Dcucumber.filter.name='подстрока1|подстрока2'` (регулярка, OR через `|`). **`-Dcucumber.options=...` не работает** — этот способ передачи опций устарел в используемой версии Cucumber (тихо игнорируется с предупреждением), несмотря на то что он всё ещё упоминается в README.

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
