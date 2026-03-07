# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов StringContentSteps

  Предыстория: Получение body ответа в строковую переменную
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" код ответа 200 ответ сохранен в "sc_response":
      | PARAMETER | status | available |
    И body ответа "sc_response" сохранено в переменную "sc_body"

  Сценарий: Проверка значений в строке с автоопределением формата
    И в строке "{sc_body}" значения соответствуют таблице:
      | $[0].status | == | available |

  Сценарий: Проверка значений в строке без учёта регистра
    И в строке "{sc_body}" значения без учета регистра соответствуют таблице:
      | $[0].status | == | AVAILABLE |

  Сценарий: Проверка значений с операциями !=, ~ и !~
    И в строке "{sc_body}" значения соответствуют таблице:
      | $[0].status | != | sold    |
      | $[0].status | ~  | avail.+ |
      | $[0].status | !~ | ^sold$  |

  Сценарий: Проверка значений в JSON строке
    И в JSON строке "{sc_body}" значения соответствуют таблице:
      | $[0].status | == | available |

  Сценарий: Извлечение значений из строки
    И из строки "{sc_body}" извлекаю значения по таблице:
      | $[0].status | extracted_status |
      | $[0].name   | extracted_name   |
    И значение переменной "extracted_status" равно "available"

  Сценарий: Извлечение значений из JSON строки
    И из JSON строки "{sc_body}" извлекаю значения по таблице:
      | $[0].status | extracted_status_explicit |
