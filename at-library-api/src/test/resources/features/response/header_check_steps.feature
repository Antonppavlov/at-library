# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов HeaderCheckSteps

  Предыстория: Получение ответа
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" код ответа 200 ответ сохранен в "header_response":
      | PARAMETER | status | available |

  Сценарий: Проверка заголовков ответа по таблице
    И в ответе "header_response" headers равны значениям из таблицы:
      | Content-Type | application/json |

  Сценарий: Сохранение заголовков ответа в переменные
    И headers ответа "header_response" сохранены в переменные из таблицы:
      | Content-Type | saved_content_type |
    И значение переменной "saved_content_type" равно "application/json"
