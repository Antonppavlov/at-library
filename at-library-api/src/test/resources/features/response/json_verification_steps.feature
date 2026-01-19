# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов JsonVerificationSteps (валидация JSON и XML ответов)
  Для примеров используется публичное API Petstore (https://petstore.swagger.io) и тестовые JSON-шаблоны.

  Предыстория: Получение списка доступных питомцев
    * отправлен HTTP GET запрос на URL "url.pet.findByStatus" с параметрами запроса и ответ сохранён в переменную "pets_available_jv"
      | PARAMETER | status | available |
    * значение из body ответа "pets_available_jv" сохранено в переменную "pets_available_body_jv"

  # iFillInTheJsonTypeDataFromTheTableSafeguardTheVariable + checkJson
  Сценарий: Заполнение JSON-шаблона и проверка значений по jsonpath
    * заполняю JSON-шаблон "json.post.pet" данными из таблицы и сохраняю в переменную "filled_pet_json_jv"
      | tomas | Барсик |
    * в json "filled_pet_json_jv" значения равны значениям из таблицы
      | $.name | Барсик |

  # checkValuesBodyValueCaseSensitive
  Сценарий: Проверка значений json-ответа с учётом регистра
    * в json ответа "pets_available_jv" значения равны значениям из таблицы
      | [0].status | available |

  # checkValuesBodyValueCaseInsensitive
  Сценарий: Проверка значений json-ответа без учёта регистра
    * в json ответа "pets_available_jv" значения равны, без учета регистра, значениям из таблицы
      | [0].status | AvAiLaBlE |

  # getValuesFromBodyAsString + проверка сохранённой переменной
  Сценарий: Сохранение значения из json-ответа в переменную по jsonpath
    * значения из json ответа "pets_available_jv", найденные по jsonpath из таблицы, сохранены в переменные
      | [0].status | first_pet_status_jv |
    * значение переменной "first_pet_status_jv" равно "available"

  # getValuesFromJsonAsString (deprecated) + проверка переменной
  Сценарий: Сохранение значений из json-строки по jsonpath
    * сохранено значение "json.simple" из property файла в переменную "simple_json_jv"
    * значения из json "simple_json_jv", найденные по jsonpath из таблицы, сохранены в переменные
      | $.answer | simple_answer_jv |
    * значение переменной "simple_answer_jv" равно "42"

  # checkJsonByRegex (deprecated)
  Сценарий: Проверка json по регулярному выражению
    * сохранено значение "json.simple" из property файла в переменную "simple_json_jv_regex"
    * в json "simple_json_jv_regex" значения соответствуют шаблонам из таблицы
      | $.answer | [0-9]+ |

  # valuesFoundByPathEqual
  Сценарий: Сравнение значений по jsonPath в двух ответах
    * отправлен HTTP GET запрос на URL "url.pet.findByStatus" с параметрами запроса и ответ сохранён в переменную "pets_available_jv_first"
      | PARAMETER | status | available |
    * отправлен HTTP GET запрос на URL "url.pet.findByStatus" с параметрами запроса и ответ сохранён в переменную "pets_available_jv_second"
      | PARAMETER | status | available |
    * значения найденные по jsonPath из json ответа "pets_available_jv_first" равны значениям из json ответа "pets_available_jv_second"
      | [0].status |

  # verifyingResponseMatchesJsonScheme (ответ ... соответствует json схеме ...)
  Сценарий: Проверка json-ответа по JSON-схеме
    * отправлен HTTP GET запрос на URL "url.pet.findByStatus" с параметрами запроса и ожидается код ответа 200, а ответ сохранён в переменную "pets_available_schema_jv"
      | PARAMETER | status | available |
    * ответ "pets_available_schema_jv" соответствует json схеме: "json/pet_array_schema.json"
