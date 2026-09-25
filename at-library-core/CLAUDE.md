# at-library-core — заметки для быстрой работы

Дополняет [../CLAUDE.md](../CLAUDE.md) и [README.md](README.md). Ядро библиотеки: `CoreScenario`/`CoreEnvironment` (хранилище переменных сценария), общие шаги (`OtherSteps` — сравнение текста, работа с датами, `PropertyLoader`), хуки (`CoreInitialSetup` — создание `CoreEnvironment`/`AssertionHelper`, логирование сценария; класс переименован из `InitialSetupSteps`). `CorePage` (Page Object) сюда не относится, хоть и упомянут в README.md этого модуля — реально живёт в `at-library-web` (зависит от Selenide, которого в core нет). От этого модуля зависят и `at-library-web`, и `at-library-api`.

## Тестовые раннеры

| Класс | Теги/scope | Назначение |
|---|---|---|
| `RunFeaturesTest` | `@unit` | основной suite (`src/test/resources/features/`) |
| `RunCoreStepCatalogTest` | `dryRun=true`, без браузера | regex-контракт по `src/test/resources/features/core_steps_catalog.feature` |

## (Исправлено через toolchains) JDK26/AspectJ: `mvn clean test` здесь раньше падал

До того, как сборка была прибита к JDK 21 через Maven Toolchains (см. корневой [CLAUDE.md](../CLAUDE.md)), 3 сценария в `RunFeaturesTest` стабильно падали на этой машине с `java.lang.IllegalArgumentException: Unsupported class file major version 70` (внутри AspectJ/Groovy weaving, `ClassLoaderWeavingAdaptor`) — `aspectjweaver:1.9.22` не умеет читать байткод JDK 26 (class-file version 70), а `mvn` на этой машине по умолчанию запускался именно на JDK 26 (Homebrew), не на системной JDK 21. Падавшие сценарии: «Установка значения переменной с текущей датой минус/плюс N часов», «Base64 декодируется в файл с ожидаемым содержимым», «Шаги даты сохраняют значения в заданном формате» — все в `RunFeaturesTest`.

С toolchain-конфигурацией (нужен локальный `~/.m2/toolchains.xml` с записью про JDK 21 — см. корневой CLAUDE.md) весь модуль проходит чисто: `mvn -pl at-library-core test` → `Tests run: 41, Failures: 0`. Если снова увидите `Unsupported class file major version`/другую ошибку weaver'а — первым делом проверить `~/.m2/toolchains.xml` на машине и вывод `mvn -version` (не `java -version` — они могут отличаться), а не чинить сами шаги или AspectJ-конфигурацию.

Практическое следствие (актуально независимо от JDK): любой `mvn ... -am test` из другого модуля потянет и `at-library-core`'s собственный `RunFeaturesTest` — теперь он не упадёт, но время на прогон уйдёт. Чтобы ограничиться одним модулем — `-Dtest=<конкретный класс> -Dsurefire.failIfNoSpecifiedTests=false`, см. корневой `CLAUDE.md`.

`OtherSteps.getTranslateNormalizeSpaceText(String)` — общий XPath-хелпер для поиска элемента «по тексту» (`//*[contains(translate(normalize-space(text()), UPPER, lower), '...')]`), используется из `at-library-web`. Исключает `<script>`/`<style>` из совпадений (иначе матчит текст внутри их исходного кода, а не отображаемый текст страницы) — не убирать это исключение без явной причины.
