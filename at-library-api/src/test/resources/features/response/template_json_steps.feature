# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов TemplateJsonSteps

  Сценарий: Заполнение JSON-шаблона данными из таблицы
    И заполнение JSON-шаблон "json.post.pet" данными из таблицы и сохранение в переменную "filled_pet"
      | tomas | Барсик |
    И в JSON строке "{filled_pet}" значения соответствуют таблице:
      | $.name | == | Барсик |

  Сценарий: Заполнение XML-шаблона данными из таблицы
    И заполнение XML-шаблон "xml/template_order.xml" данными из таблицы и сохранение в переменную "filled_order_xml"
      | \{\{orderId\}\} | 1001 |
      | \{\{status\}\}  | READY |
    И в XML строке "{filled_order_xml}" значения соответствуют таблице:
      | /order/id/text()     | == | 1001  |
      | /order/status/text() | == | READY |
