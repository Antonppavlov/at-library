# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов BodyExtractSteps

  Предыстория: Получение ответа
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" код ответа 200 ответ сохранен в "body_response":
      | PARAMETER | status | available |

  Сценарий: Извлечение body ответа в переменную
    И body ответа "body_response" сохранено в переменную "saved_body"
