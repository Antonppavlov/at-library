# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов XmlResponseSteps
  API: httpbin XML sample

  Предыстория: Получение XML ответа
    И отправлен HTTP GET на "https://httpbin.org/xml" код ответа 200 ответ сохранен в "xml_response"

  Сценарий: Проверка одного значения и сохранение по xPath
    И в ответе "xml_response" содержимое найденное по xPath "//slide[1]/title" равно "Wake up to WonderWidgets!"
    И из ответа "xml_response" содержимое найденное по xPath "//slide[1]/title" сохранено в "xml_title"

  Сценарий: Проверка значений по таблице
    И в ответе "xml_response" содержимые найденные по xPath равны:
      | //slide[1]/title | Wake up to WonderWidgets! |
    И в ответе "xml_response" содержимые найденные по xPath без учета регистра равны:
      | //slide[1]/title | WAKE UP TO WONDERWIDGETS! |

  Сценарий: Сохранение набора значений по xPath
    И из ответа "xml_response" содержимые найденные по xPath сохранены в переменные:
      | //slide[1]/title | xml_title_saved |

  Сценарий: Сравнение двух XML ответов по xPath
    И отправлен HTTP GET на "https://httpbin.org/xml" код ответа 200 ответ сохранен в "xml_response_2"
    И в ответах "xml_response" и "xml_response_2" содержимые найденные по xPath совпадают:
      | //slide[1]/title |

  Сценарий: Сравнение XML body и проверка XSD схемы
    И в ответе "xml_response" содержимое равно xml "xml/httpbin_slideshow.xml"
    И в ответе "xml_response" содержимое соответствует xsd схеме "xsd/httpbin_slideshow.xsd"

  Сценарий: Проверки списка значений по xPath
    И в ответе "xml_response" список значений найденных по xPath "//slide/title" содержит значение "Overview"
    И в ответе "xml_response" список значений найденных по xPath "//slide/type" все значения равны "all"
    И в ответе "xml_response" список значений найденных по xPath "//slide/type" все значения содержат "all"
    И в ответе "xml_response" список значений найденных по xPath "//slide/title" размер 2
