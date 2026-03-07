# language: ru
@api
@extension
Функционал: Расширение покрытия XmlResponseSteps на публичном API
  API: Swagger Petstore v2
  Swagger: https://petstore.swagger.io/

  Предыстория: Получение XML ответа списка питомцев
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" код ответа 200 ответ сохранен в "xml_response":
      | PARAMETER | status | available       |
      | HEADER    | Accept | application/xml |

  Сценарий: Проверка и сохранение значений xPath
    И в ответе "xml_response" содержимое найденное по xPath "//Pet/status" равно "available"
    И из ответа "xml_response" содержимое найденное по xPath "//Pet/status" сохранено в "xml_first_status"
    И в ответе "xml_response" содержимые найденные по xPath равны:
      | //Pet/status | available |
    И в ответе "xml_response" содержимые найденные по xPath без учета регистра равны:
      | //Pet/status | AVAILABLE |

  Сценарий: Массовое сохранение и сравнение двух XML ответов
    И из ответа "xml_response" содержимые найденные по xPath сохранены в переменные:
      | //Pet/status | xml_status_saved |
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" код ответа 200 ответ сохранен в "xml_response_2":
      | PARAMETER | status | available       |
      | HEADER    | Accept | application/xml |
    И в ответах "xml_response" и "xml_response_2" содержимые найденные по xPath совпадают:
      | //Pet/status |

  Сценарий: Проверки списков по xPath
    И в ответе "xml_response" список значений найденных по xPath "//Pet/status" содержит значение "available"
    И в ответе "xml_response" список значений найденных по xPath "//Pet/status" все значения равны "available"
    И в ответе "xml_response" список значений найденных по xPath "//Pet/status" все значения содержат "avail"
