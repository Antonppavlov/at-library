# language: ru
@api
@extension
Функционал: Расширение покрытия metadata и StringContentSteps на публичном API
  API: Fake REST API
  Swagger: https://fakerestapi.azurewebsites.net/index.html

  Предыстория: Получение ответа с публичного API
    И отправлен HTTP GET на "https://fakerestapi.azurewebsites.net/api/v1/Activities" код ответа 200 ответ сохранен в "meta_response"
    И body ответа "meta_response" сохранено в переменную "meta_body"

  Сценарий: Проверка метаданных и body
    И в ответе "meta_response" statusCode: 200
    И в ответе "meta_response" headers равны значениям из таблицы:
      | Content-Type | application/json; charset=utf-8; v=1.0 |

  Сценарий: Проверка и извлечение значений из JSON строки
    И в JSON строке "{meta_body}" значения соответствуют таблице:
      | $[0].id      | != | -1        |
      | $[0].title   | ~  | .+        |
      | $[0].dueDate | ~  | .+T.+     |
    И из JSON строки "{meta_body}" извлекаю значения по таблице:
      | $[0].title | first_title |

  Сценарий: Template XML + проверки в строке XML
    И заполнение XML-шаблон "xml/template_order.xml" данными из таблицы и сохранение в переменную "filled_xml"
      | \{\{orderId\}\} | 9001  |
      | \{\{status\}\}  | READY |
    И в XML строке "{filled_xml}" значения соответствуют таблице:
      | /order/id/text()     | == | 9001  |
      | /order/status/text() | == | READY |
