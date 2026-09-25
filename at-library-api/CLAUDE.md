# at-library-api — заметки для быстрой работы

Дополняет [../CLAUDE.md](../CLAUDE.md) и [README.md](README.md) (там подробно описаны типы параметров таблиц, `application.properties`, JSON-шаблоны тел запроса, список публичных API для тестов). README уже отдельно объясняет каталог `feature-extensions/` (заготовки, специально не подключённые в постоянный прогон) — не путать с обычным `features/`.

## Тестовые раннеры

Тот же паттерн, что в `at-library-web` (см. [../at-library-web/CLAUDE.md](../at-library-web/CLAUDE.md)) — несколько раннеров, не только `RunFeaturesTest`:

| Класс | Теги/scope | Назначение |
|---|---|---|
| `RunFeaturesTest` | **без фильтра тегов** | основной suite (`src/test/resources/features/`) — в отличие от `at-library-web`/`at-library-core`, тут нет `tags = "@unit"` |
| `RunApiLocalStepContractTest` | без фильтра | `local-contract/api_response_steps_local_contract.feature` |
| `RunApiProxyStepContractIT` | без фильтра | `local-contract/api_proxy_steps_local_contract.feature` |
| `RunApiKnownGapContractIT` | без фильтра | `local-contract/api_known_gap_contract.feature` — по Javadoc класса документирует **один** конкретный известный дефект: проверку непустого JSON-массива (в отличие от web-модуля, где `RunWebKnownGapContractIT` документирует сразу 2 разных дефекта); при встрече падения здесь сначала проверить Javadoc/комментарий класса, не чинить вслепую |
| `RunApiStepCatalogTest` | `dryRun=true`, без браузера | regex-контракт по `src/test/resources/step-catalog/api_steps_catalog.feature` |

Раз `RunFeaturesTest` здесь без тегового фильтра — при фильтрации по имени сценария (`-Dcucumber.filter.name=...`) не нужно дополнительно думать про теги, как в web-модуле.

> Этот файл описывает структуру раннеров по факту чтения кода `@CucumberOptions`; в отличие от заметок в `at-library-web/CLAUDE.md`, здесь пока нет собственных находок из реальной отладки (этот модуль не запускался и не чинился в текущей сессии) — при первой реальной работе с модулем стоит перепроверить актуальность и дополнить.
