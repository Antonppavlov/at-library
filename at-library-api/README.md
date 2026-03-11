at-library-api
=========================

Настройка проекта
====================
Подключите зависимость
```xml
<dependency>
      <groupId>ru</groupId>
      <artifactId>at-library-api</artifactId>
      <version>11.03.2026</version>
</dependency>
```

Высокоуровневые шаги для REST-запросов
======================================

Модуль `at-library-api` предоставляет шаги для отправки HTTP-запросов (GET/POST/PUT/DELETE/HEAD/OPTIONS/TRACE/PATCH) и проверки ответов.
Все шаги построены вокруг следующей схемы:

1. **Отправка запроса и сохранение ответа**

```gherkin
И отправлен HTTP GET на "url.store.inventory" ответ сохранен в "inventory_response"
```

2. **Отправка запроса с параметрами (headers/query/body/path и т.д.)**

```gherkin
И отправлен HTTP GET на "url.pet.findByStatus" ответ сохранен в "pets_response":
  | HEADER    | Accept       | application/json |
  | PARAMETER | status       | available        |
```

3. **Отправка запроса с проверкой кода ответа и сохранением**

```gherkin
И отправлен HTTP POST на "url.pet" код ответа 200 ответ сохранен в "create_pet_response":
  | HEADER | Accept       | application/json   |
  | HEADER | Content-Type | application/json   |
  | BODY   | BODY         | ${json.post.pet}   |
```

4. **Периодическая отправка (polling) до выполнения условия**

```gherkin
И каждые 5с/30с отправлен HTTP GET на "url.store.inventory" код ответа 200 ответ сохранен в "inventory_response"
```

или с проверкой параметров ответа по таблице:

```gherkin
И каждые 5с/60с отправлен HTTP GET на "url.store.inventory" код ответа 200 ответ сохранен в "inventory_response":
  | HEADER   | Accept       | application/json |
  | RESPONSE |              |                  |
  | HEADER   | Content-Type | application/json |
```

Типы параметров в таблице
==========================

В таблицах параметров запроса поддерживаются типы (первый столбец):

- `HEADER`         – HTTP-заголовок: `name -> value`
- `PARAMETER`      – query-параметр (строка запроса)
- `FORM_PARAMETER` – `application/x-www-form-urlencoded` параметр формы
- `PATH_PARAMETER` – path-параметр (используется вместе с шаблоном пути, например `/pet/{petId}`)
- `BODY`           – тело запроса. Если значение указывает на ресурс в `src/test/resources` (например, `restBodies/post_new_pet.json` или `${json.post.pet}`), то будет загружен JSON из файла.
- `FILE`           – multipart-файл (значение – путь до файла, читаемый из properties или variables)
- `COOKIES`        – установка cookie
- `BASIC_AUTHENTICATION` – базовая авторизация (логин/пароль)
- `ACCESS_TOKEN`   – добавление заголовка с Bearer-токеном
- `MULTIPART`      – произвольные multipart-параметры

Для проверки параметров ответа в шаге polling используются типы:

- `HEADER`  – проверка значения заголовка
- `COOKIES` – проверка значения cookie
- `BODY`    – проверка, что тело ответа целиком равно ожидаемому значению

Использование application.properties
====================================

В `src/test/resources/application.properties` можно описать базовый URL и относительные пути,
а также пути к JSON-файлам и тестовые данные. Например:

```properties
# Базовый URL Petstore, используется по умолчанию RestAssured'ом
baseURI=https://petstore.swagger.io/v2

# Относительные пути к основным ресурсам Petstore
url.pet=/pet
url.pet.petId=/pet/{petId}
url.store.inventory=/store/inventory
url.pet.findByStatus=/pet/findByStatus

# Пути до JSON-шаблонов тел запросов (для шагов с BODY)
json.post.pet=restBodies/post_new_pet.json
json.put.pet=restBodies/put_update_pet.json

# Тестовый идентификатор питомца, используемый в JSON-шаблонах и шагах
pet.id=17122019
```

В шагах можно ссылаться на эти значения по ключу (`url.store.inventory`, `json.post.pet`, `pet.id`),
а `PropertyLoader` подставит реальные значения в URL, таблицы параметров и JSON-шаблоны.

JSON-шаблоны тел запросов
==========================

Файлы под `src/test/resources/restBodies` используются как шаблоны для тела запроса.
В них можно использовать плейсхолдеры в фигурных скобках, которые будут заполнены
значениями из `application.properties` или переменных сценария.

Пример `restBodies/post_new_pet.json`:

```json
{
  "id": {pet.id},
  "category": {
    "id": {pet.id},
    "name": "cat"
  },
  "name": "tomas",
  "photoUrls": [
    "string"
  ],
  "tags": [
    {
      "id": {pet.id},
      "name": "domestic cat"
    }
  ],
  "status": "available"
}
```

При формировании запроса плейсхолдеры вида `{pet.id}` будут заменены на значения из properties,
а итоговая строка будет валидным JSON, передаваемым в `BODY`.

Логирование HTTP-запросов и ответов
===================================

По умолчанию `RequestSteps` включает логирование HTTP-запросов и ответов RestAssured в консоль
(через `request.log().all()` и `response.then().log().all()`). Это помогает отлаживать сами шаги и
сценарии с API.

Логирование можно отключить с помощью системного свойства:

```bash
mvn -pl at-library-api -am clean test -Dapi.http.log.disable=true
```

Структура feature-файлов (1 класс шагов = 1 feature)
=====================================================

Основные feature-файлы в `src/test/resources/features` разнесены по принципу:
**один класс шагов — один feature-файл**.

- `request/send_request_steps.feature` → `SendRequestSteps`
- `proxy/proxy_steps.feature` → `ProxySteps`
- `response/json_response.feature` → `JsonResponseSteps`
- `response/xml_response.feature` → `XmlResponseSteps`
- `response/yaml_response.feature` → `YamlResponseSteps`
- `response/status_code_check_steps.feature` → `StatusCodeCheckSteps`
- `response/header_check_steps.feature` → `HeaderCheckSteps`
- `response/body_extract_steps.feature` → `BodyExtractSteps`
- `response/cookie_check_steps.feature` → `CookieCheckSteps`
- `response/string_content_steps.feature` → `StringContentSteps`
- `response/template_json_steps.feature` → `TemplateJsonSteps`

Файлы `response/response_metadata.feature` и `response/string_content.feature`
оставлены как deprecated-заглушки без сценариев, чтобы исключить дубли и смешивание классов шагов.

Запуск тестов модуля at-library-api
===================================

После подключения всех плагинов и зависимостей вы можете запускать тесты модуля командами:

- Запуск только API-модуля:

```bash
mvn -pl at-library-api -am clean test
```

- Запуск тестов с тегом `@api`:

```bash
mvn -pl at-library-api -am clean test -Dcucumber.options="--tags @api"
```

- Генерация Allure-отчёта для модуля:

```bash
cd at-library-api
mvn allure:serve
```

Готовые feature-файлы для расширения покрытия шагов
====================================================

Добавлены готовые заготовки feature-файлов в директорию:

- `src/test/resources/feature-extensions/request/send_request_steps_extension.feature`
- `src/test/resources/feature-extensions/response/json_response_extension.feature`
- `src/test/resources/feature-extensions/response/xml_response_extension.feature`
- `src/test/resources/feature-extensions/response/yaml_response_extension.feature`
- `src/test/resources/feature-extensions/response/metadata_and_string_extension.feature`
- `src/test/resources/feature-extensions/proxy/proxy_steps_extension.feature`

Зачем отдельная директория:
- текущий раннер запускает только `src/test/resources/features`, поэтому расширения не влияют на стабильность текущего CI-пула;
- файлы можно переносить по одному в `features`, когда вы готовы включить их в постоянный прогон.

Важно: extension-файлы уже настроены на публичные API и содержат ссылку на Swagger/OpenAPI в шапке каждого feature.

Рекомендованный порядок включения:
1. Убедиться, что публичные API доступны из вашего контура.
2. При необходимости переопределить URL через `application.properties`.
3. Перенести нужный `.feature` из `feature-extensions` в `features`.
4. Запустить модульные API-тесты и убедиться, что сценарии стабильны.


Публичные API для написания и стабилизации API-тестов
=====================================================

Да, можно использовать общедоступные API, но лучше разделять их на 2 группы:

1. **Стабильные для базовых smoke/contract шагов**
2. **Вспомогательные только для локальной отладки**

Рекомендуемый минимум:

- **Swagger Petstore** (`https://petstore.swagger.io/v2`)
  - Подходит для: GET/POST/PUT/DELETE, query/path/body, базовых JSON-проверок, polling.
  - Уже используется в текущем наборе примеров.

- **JSONPlaceholder** (`https://jsonplaceholder.typicode.com`)
  - Подходит для: чтение/создание JSON-ресурсов, проверки jsonPath, сравнение ответов.
  - Ограничение: это фейковый write API (POST/PUT/DELETE не всегда отражают персистентное состояние).

- **Postman Echo** (`https://postman-echo.com`)
  - Подходит для: проверки headers/query/form-data/cookies, эхо-валидация тела запроса.
  - Хорош для шагов `HEADER`, `PARAMETER`, `FORM_PARAMETER`, `MULTIPART`, `ACCESS_TOKEN`.

- **ReqRes** (`https://reqres.in/api`)
  - Подходит для: сценарии авторизации, негативные кейсы, валидация кодов ответа.
  - Ограничение: некоторые эндпоинты могут требовать api-key/лимитироваться.

Практическая рекомендация по надёжности CI:

- Для **регресса в CI** лучше держать основной пул тестов на **mock-сервисе** (WireMock/MockServer).
- Публичные API оставить как отдельный smoke-пул (например, тег `@external_api`), чтобы внешняя нестабильность не ломала релизный пайплайн.
- Если нужен 100% охват всех шагов библиотеки, правильнее сделать локальные фикстуры (JSON/XML/YAML/XSD) и отдавать их через mock endpoint'ы.


Публичные API и Swagger ссылки для extension-тестов
===================================================

Все расширенные feature-файлы ссылаются на публичные API и используют ссылки из `application.properties`:

- `api.docs.petstore=https://petstore.swagger.io/`
- `api.docs.fakerest=https://fakerestapi.azurewebsites.net/index.html`
- `api.docs.petstore.v3.openapi=https://petstore3.swagger.io/`

Это позволяет явно видеть, какое API и по какой спецификации проверяется в каждом feature.
