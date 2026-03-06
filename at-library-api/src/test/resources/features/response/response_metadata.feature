# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка метаданных HTTP-ответа (StatusCodeCheckSteps, HeaderCheckSteps, BodyExtractSteps)
  Для примеров используется публичное API Petstore (https://petstore.swagger.io).

  Предыстория: Получение списка доступных питомцев
    И отправлен HTTP GET на "url.pet.findByStatus" код ответа 200 ответ сохранен в "meta_response":
      | PARAMETER | status | available |

  # === StatusCodeCheckSteps ===

  # checkResponseStatusCode
  Сценарий: Проверка HTTP статус-кода ответа
    И в ответе "meta_response" statusCode: 200

  # === HeaderCheckSteps ===

  # checkHeaders
  Сценарий: Проверка заголовков ответа по таблице
    И в ответе "meta_response" headers равны значениям из таблицы:
      | Content-Type | application/json |

  # saveHeaders
  Сценарий: Сохранение заголовков ответа в переменные
    И headers ответа "meta_response" сохранены в переменные из таблицы:
      | Content-Type | saved_content_type |
    И значение переменной "saved_content_type" равно "application/json"

  # === BodyExtractSteps ===

  # saveResponseBody
  Сценарий: Извлечение body ответа в переменную
    И body ответа "meta_response" сохранено в переменную "meta_body"
