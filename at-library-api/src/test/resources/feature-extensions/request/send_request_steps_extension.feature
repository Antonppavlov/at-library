# language: ru
@api
@extension
Функционал: Расширенное покрытие шагов SendRequestSteps на публичных API
  API #1: Swagger Petstore v2
  Swagger: https://petstore.swagger.io/
  API #2: Fake REST API
  Swagger: https://fakerestapi.azurewebsites.net/index.html

  Сценарий: PATCH запрос с явным телом в Petstore
    И отправлен HTTP PATCH на "https://petstore.swagger.io/v2/pet" код ответа 200 ответ сохранен в "patch_pet_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | {"id":17122019,"name":"patch-name","status":"available"} |

  Сценарий: GET и polling в Petstore
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/store/inventory" код ответа 200 ответ сохранен в "inventory_response"
    И каждые 2с/20с отправлен HTTP GET на "https://petstore.swagger.io/v2/store/inventory" код ответа 200 ответ сохранен в "inventory_polling_with_check":
      | HEADER   | Accept       | application/json |
      | RESPONSE |              |                  |
      | HEADER   | Content-Type | application/json |

  Сценарий: POST в Fake REST API
    И отправлен HTTP POST на "https://fakerestapi.azurewebsites.net/api/v1/Activities" код ответа 200 ответ сохранен в "create_activity_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | {"id":9999,"title":"activity","dueDate":"2026-01-01T00:00:00","completed":false} |

  Сценарий: PUT в Fake REST API
    И отправлен HTTP PUT на "https://fakerestapi.azurewebsites.net/api/v1/Activities/9999" код ответа 200 ответ сохранен в "update_activity_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | {"id":9999,"title":"updated activity","dueDate":"2026-01-01T00:00:00","completed":true} |
