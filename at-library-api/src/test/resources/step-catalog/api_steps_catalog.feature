# language: ru
@catalog
Функционал: Каталог публичных Cucumber-шагов API-модуля

  Сценарий: Шаги отправки HTTP-запросов
    И отправлен HTTP GET на "http://localhost/resource" ответ сохранен в "response"
    И отправлен HTTP POST на "http://localhost/resource" код ответа 201 ответ сохранен в "response":
      | BODY | {"name":"test"} |
    И каждые 1с/5с отправлен HTTP PUT на "http://localhost/resource" код ответа 200 ответ сохранен в "response"
    И каждые 1с/5с отправлен HTTP PATCH на "http://localhost/resource" код ответа 200 ответ сохранен в "response":
      | HEADER | Accept | application/json |

  Структура сценария: Все поддерживаемые HTTP-методы
    И отправлен HTTP <метод> на "http://localhost/resource" ответ сохранен в "response"

    Примеры:
      | метод   |
      | GET     |
      | PUT     |
      | POST    |
      | DELETE  |
      | HEAD    |
      | TRACE   |
      | OPTIONS |
      | PATCH   |

  Сценарий: Шаги proxy
    И используется proxy: "localhost" port: "8080"
    И через прокси отправлен запрос "http://localhost/resource"
    И выключено использование proxy

  Сценарий: Шаги метаданных и тела ответа
    И в ответе "response" statusCode: 200
    И body ответа "response" сохранено в переменную "body"
    И в ответе "response" headers равны значениям из таблицы:
      | Content-Type | application/json |
    И headers ответа "response" сохранены в переменные из таблицы:
      | Content-Type | content_type |
    И в ответе "response" cookies равны значениям из таблицы:
      | session | value |
    И cookies ответа "response" сохранены в переменные из таблицы:
      | session | session_cookie |

  Сценарий: Шаги JSON-ответа
    И в ответе "response" содержимое найденное по jsonPath "name" равно "test"
    И из ответа "response" содержимое найденное по jsonPath "name" сохранено в "name"
    И в ответе "response" содержимые найденные по jsonPath равны:
      | name | test |
    И в ответе "response" содержимые найденные по jsonPath без учета регистра равны:
      | name | TEST |
    И из ответа "response" содержимые найденные по jsonPath сохранены в переменные:
      | name | saved_name |
    И в ответах "response" и "other_response" содержимые найденные по jsonPath совпадают:
      | name | name |
    И в ответе "response" содержимое равно json "json/expected.json"
    И в ответе "response" содержимое соответствует json схеме "json/schema.json"
    И в ответе "response" массив значений найденных по jsonPath "items.name" содержит значение "test"
    И в ответе "response" массив значений найденных по jsonPath "items.name" все значения равны "test"
    И в ответе "response" массив значений найденных по jsonPath "items.name" все значения содержат "tes"
    И в ответе "response" массив значений найденных по jsonPath "items.name" размер 1
    И в ответе "response" массив значений найденных по jsonPath "items.name" не пустой
    И в ответе "response" массив значений найденных по jsonPath "items.name" отсортирован по возрастанию
    И в ответе "response" массив значений найденных по jsonPath "items.name" отсортирован по убыванию
    И в ответе "response" массив значений найденных по jsonPath "items.date" в периоде между "2026-01-01" и "2026-12-31" в формате "yyyy-MM-dd"

  Сценарий: Шаги XML-ответа
    И в ответе "response" содержимое найденное по xPath "//name" равно "test"
    И из ответа "response" содержимое найденное по xPath "//name" сохранено в "name"
    И в ответе "response" содержимые найденные по xPath равны:
      | //name | test |
    И в ответе "response" содержимые найденные по xPath без учета регистра равны:
      | //name | TEST |
    И из ответа "response" содержимые найденные по xPath сохранены в переменные:
      | //name | saved_name |
    И в ответах "response" и "other_response" содержимые найденные по xPath совпадают:
      | //name | //name |
    И в ответе "response" содержимое равно xml "xml/expected.xml"
    И в ответе "response" содержимое соответствует xsd схеме "xsd/schema.xsd"
    И в ответе "response" список значений найденных по xPath "//item/name" содержит значение "test"
    И в ответе "response" список значений найденных по xPath "//item/name" все значения равны "test"
    И в ответе "response" список значений найденных по xPath "//item/name" все значения содержат "tes"
    И в ответе "response" список значений найденных по xPath "//item/name" размер 1
    И в ответе "response" список значений найденных по xPath "//item/name" отсортирован по возрастанию
    И в ответе "response" список значений найденных по xPath "//item/name" отсортирован по убыванию
    И в ответе "response" список значений найденных по xPath "//item/date" в периоде между "2026-01-01" и "2026-12-31" в формате "yyyy-MM-dd"

  Сценарий: Шаги YAML-ответа
    И в ответе "response" содержимое найденное по yamlPath "name" равно "test"
    И из ответа "response" содержимое найденное по yamlPath "name" сохранено в "name"
    И в ответе "response" содержимые найденные по yamlPath равны:
      | name | test |
    И в ответе "response" содержимые найденные по yamlPath без учета регистра равны:
      | name | TEST |
    И из ответа "response" содержимые найденные по yamlPath сохранены в переменные:
      | name | saved_name |
    И в ответах "response" и "other_response" содержимые найденные по yamlPath совпадают:
      | name | name |
    И в ответе "response" содержимое равно yaml "yaml/expected.yaml"
    И в ответе "response" массив значений найденных по yamlPath "items.name" содержит значение "test"
    И в ответе "response" массив значений найденных по yamlPath "items.name" все значения равны "test"
    И в ответе "response" массив значений найденных по yamlPath "items.name" все значения содержат "tes"
    И в ответе "response" массив значений найденных по yamlPath "items.name" размер 1
    И в ответе "response" массив значений найденных по yamlPath "items.name" отсортирован по возрастанию
    И в ответе "response" массив значений найденных по yamlPath "items.name" отсортирован по убыванию
    И в ответе "response" массив значений найденных по yamlPath "items.date" в периоде между "2026-01-01" и "2026-12-31" в формате "yyyy-MM-dd"

  Сценарий: Шаги проверки и извлечения структурированных строк
    И в строке "body" значения соответствуют таблице:
      | name | == | test |
    И в строке "body" значения без учета регистра соответствуют таблице:
      | name | == | TEST |
    И в JSON строке "body" значения соответствуют таблице:
      | name | == | test |
    И в XML строке "body" значения без учета регистра соответствуют таблице:
      | //name | == | TEST |
    И в PARAMS строке "body" значения соответствуют таблице:
      | name | == | test |
    И из строки "body" извлекаю значения по таблице:
      | name | saved_name |
    И из JSON строки "body" извлекаю значения по таблице:
      | name | saved_name |
    И из XML строки "body" извлекаю значения по таблице:
      | //name | saved_name |
    И из PARAMS строки "body" извлекаю значения по таблице:
      | name | saved_name |

  Структура сценария: Шаг заполнения шаблона поддерживает JSON и XML
    И заполнение <формат>-шаблон "template" данными из таблицы и сохранение в переменную "result"
      | name | test |

    Примеры:
      | формат |
      | JSON   |
      | XML    |
