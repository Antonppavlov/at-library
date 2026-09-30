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

  # Шаг читает фактический код ответа: для кодов, отличных от 200, он тоже должен сработать.
  Структура сценария: Проверка HTTP статус-кода ответа <code>
    И отправлен HTTP GET на "https://httpbin.org/status/<code>" ответ сохранен в "status_other_response"
    Тогда в ответе "status_other_response" statusCode: <code>

    Примеры:
      | code |
      | 201  |
      | 400  |
      | 404  |
      | 500  |
