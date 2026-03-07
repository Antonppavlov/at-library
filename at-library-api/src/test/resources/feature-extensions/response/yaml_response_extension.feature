# language: ru
@api
@extension
Функционал: Расширение покрытия YamlResponseSteps на публичном API
  API: Swagger Petstore v3 OpenAPI YAML
  Swagger: https://petstore3.swagger.io/

  Предыстория: Получение YAML OpenAPI спецификации
    И отправлен HTTP GET на "https://petstore3.swagger.io/api/v3/openapi.yaml" код ответа 200 ответ сохранен в "yaml_response"

  Сценарий: Проверка и сохранение значений yamlPath
    И в ответе "yaml_response" содержимое найденное по yamlPath "openapi" равно "3.0.4"
    И из ответа "yaml_response" содержимое найденное по yamlPath "info.title" сохранено в "yaml_api_title"
    И в ответе "yaml_response" содержимые найденные по yamlPath равны:
      | info.title | Swagger Petstore - OpenAPI 3.0 |
    И в ответе "yaml_response" содержимые найденные по yamlPath без учета регистра равны:
      | info.title | SWAGGER PETSTORE - OPENAPI 3.0 |

  Сценарий: Массовое сохранение и сравнение YAML ответов
    И из ответа "yaml_response" содержимые найденные по yamlPath сохранены в переменные:
      | info.title   | yaml_title_saved   |
      | info.version | yaml_version_saved |
    И отправлен HTTP GET на "https://petstore3.swagger.io/api/v3/openapi.yaml" код ответа 200 ответ сохранен в "yaml_response_2"
    И в ответах "yaml_response" и "yaml_response_2" содержимые найденные по yamlPath совпадают:
      | info.title |


  Сценарий: Проверка массивов в YAML
    И в ответе "yaml_response" массив значений найденных по yamlPath "tags.name" содержит значение "pet"
