# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов YamlResponseSteps
  API: Petstore OpenAPI v3 YAML

  Предыстория: Получение YAML OpenAPI
    И отправлен HTTP GET на "https://petstore3.swagger.io/api/v3/openapi.yaml" код ответа 200 ответ сохранен в "yaml_response"

  Сценарий: Проверка одного значения и сохранение по yamlPath
    И в ответе "yaml_response" содержимое найденное по yamlPath "openapi" равно "3.0.4"
    И из ответа "yaml_response" содержимое найденное по yamlPath "info.title" сохранено в "yaml_title"

  Сценарий: Проверка значений по таблице
    И в ответе "yaml_response" содержимые найденные по yamlPath равны:
      | info.title | Swagger Petstore - OpenAPI 3.0 |
    И в ответе "yaml_response" содержимые найденные по yamlPath без учета регистра равны:
      | info.title | SWAGGER PETSTORE - OPENAPI 3.0 |

  Сценарий: Сохранение набора значений по yamlPath
    И из ответа "yaml_response" содержимые найденные по yamlPath сохранены в переменные:
      | info.title   | yaml_title_saved   |
      | info.version | yaml_version_saved |

  Сценарий: Сравнение двух YAML ответов
    И отправлен HTTP GET на "https://petstore3.swagger.io/api/v3/openapi.yaml" код ответа 200 ответ сохранен в "yaml_response_2"
    И в ответах "yaml_response" и "yaml_response_2" содержимые найденные по yamlPath совпадают:
      | info.title |

  Сценарий: Проверка массивов по yamlPath
    И в ответе "yaml_response" массив значений найденных по yamlPath "tags.name" содержит значение "pet"
