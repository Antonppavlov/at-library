# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов StatusCodeCheckSteps

  Предыстория: Получение ответа
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" код ответа 200 ответ сохранен в "status_response":
      | PARAMETER | status | available |

  Сценарий: Проверка HTTP статус-кода ответа
    И в ответе "status_response" statusCode: 200
