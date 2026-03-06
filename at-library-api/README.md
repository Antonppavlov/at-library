at-library-api
=========================

Настройка проекта
====================
Подключите зависимость
```xml
<dependency>
      <groupId>ru</groupId>
      <artifactId>at-library-api</artifactId>
      <version>06.03.2026</version>
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
