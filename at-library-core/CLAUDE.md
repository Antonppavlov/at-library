# at-library-core — заметки для быстрой работы

Дополняет [../CLAUDE.md](../CLAUDE.md) и [README.md](README.md). Ядро библиотеки: `CorePage` (Page Object), `CoreScenario`/`CoreEnvironment` (хранилище переменных сценария), общие шаги (`OtherSteps` — сравнение текста, работа с датами, `PropertyLoader`), хуки (`InitialSetupSteps`). От этого модуля зависят и `at-library-web`, и `at-library-api`.

## Тестовые раннеры

| Класс | Теги/scope | Назначение |
|---|---|---|
| `RunFeaturesTest` | `@unit` | основной suite (`src/test/resources/features/`) |
| `RunCoreStepCatalogTest` | `dryRun=true`, без браузера | regex-контракт по `src/test/resources/features/core_steps_catalog.feature` |

## Известная, пред-существующая проблема: `mvn clean test` здесь падает

3 сценария в `RunFeaturesTest` стабильно падают на этой машине с `java.lang.IllegalArgumentException: Unsupported class file major version 70` (внутри AspectJ/Groovy weaving, `ClassLoaderWeavingAdaptor`) — `aspectjweaver:1.9.22` не умеет читать байткод JDK 26 (class-file version 70). Конкретные падающие сценарии: «Установка значения переменной с текущей датой минус/плюс N часов», «Base64 декодируется в файл с ожидаемым содержимым», «Шаги даты сохраняют значения в заданном формате» — все в `RunFeaturesTest`. Это версийная несовместимость окружения, не баг в самих шагах — не чинить как побочный эффект другой задачи (и не пытаться подкрутить AspectJ-конфигурацию ради этого).

Практическое следствие: любой `mvn ... -am test` из другого модуля, который **не** ограничен `-Dtest=<конкретный класс>`, потянет `at-library-core`'s собственный `RunFeaturesTest` и упадёт здесь раньше, чем дойдёт до целевого модуля. См. корневой `CLAUDE.md` — как гонять `at-library-web` в обход этого.

`OtherSteps.getTranslateNormalizeSpaceText(String)` — общий XPath-хелпер для поиска элемента «по тексту» (`//*[contains(translate(normalize-space(text()), UPPER, lower), '...')]`), используется из `at-library-web`. Исключает `<script>`/`<style>` из совпадений (иначе матчит текст внутри их исходного кода, а не отображаемый текст страницы) — не убирать это исключение без явной причины.
