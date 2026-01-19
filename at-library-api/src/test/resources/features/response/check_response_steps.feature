# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов валидации ответов (CheckResponseSteps)
  Для примеров используется публичное API Petstore (https://petstore.swagger.io) и простые JSON/BASE64 данные.

  Предыстория: Получение списка доступных питомцев
    И отправлен HTTP GET запрос на URL "url.pet.findByStatus" с параметрами запроса и ответ сохранён в переменную "pets_available_response"
      | PARAMETER | status | available |
    И значение из body ответа "pets_available_response" сохранено в переменную "pets_available_body"

  # checkResponseStatusCode
  Сценарий: Проверка кода ответа по переменной
    И в ответе "pets_available_response" statusCode: 200

  # checkResponseHeaderValues
  Сценарий: Проверка заголовков ответа по таблице
    И в ответе "pets_available_response" содержатся header со значениями из таблицы
      | Content-Type | application/json |

  # checkFormattedData(String path, ...)
  Сценарий: Проверка форматированных данных JSON по таблице (автоопределение формата)
    И значения в "{pets_available_body}" проверены по таблице:
      | $[0].status | == | available |

  # checkFormattedData(TextFormat, String, ...)
  Сценарий: Проверка форматированных данных JSON по таблице с явным указанием формата
    И значения в JSON "{pets_available_body}" проверены по таблице:
      | $[0].status | == | available |

  # saveValuesFromFormattedData(String, DataTable)
  Сценарий: Сохранение значений из JSON по таблице
    И значения из JSON "{pets_available_body}" сохранены в переменные по таблице:
      | $[0].status | first_pet_status |

  # saveValuesFromFormattedData(TextFormat, ...)
  Сценарий: Сохранение значений из JSON по таблице с явным форматом
    И значения из JSON "{pets_available_body}" сохранены в переменные по таблице:
      | $[0].id | first_pet_id |


  # Дополнительные сценарии для проверки ответов POST/PUT/DELETE с использованием CheckResponseSteps
  Сценарий: Проверка кода и тела ответа для POST создания питомца
    И отправлен HTTP POST запрос на URL "url.pet" с параметрами запроса и ожидается код ответа 200, а ответ сохранён в переменную "create_pet_response_for_check"
      | HEADER | Accept       | application/json        |
      | HEADER | Content-Type | application/json        |
      | BODY   | BODY         | json.post.pet           |
    И в ответе "create_pet_response_for_check" statusCode: 200
    И значение из body ответа "create_pet_response_for_check" сохранено в переменную "create_pet_body"
    И значения в JSON "{create_pet_body}" проверены по таблице:
      | $.status | == | available |

  Сценарий: Проверка кода и тела ответа для PUT обновления питомца
    И отправлен HTTP PUT запрос на URL "url.pet" с параметрами запроса и ожидается код ответа 200, а ответ сохранён в переменную "update_pet_response_for_check"
      | HEADER | Accept       | application/json      |
      | HEADER | Content-Type | application/json      |
      | BODY   | BODY         | json.put.pet          |
    И в ответе "update_pet_response_for_check" statusCode: 200
    И значение из body ответа "update_pet_response_for_check" сохранено в переменную "update_pet_body"
    И значения в JSON "{update_pet_body}" проверены по таблице:
      | $.name | == | tomas dangerous |

  Сценарий: Проверка кода ответа для DELETE питомца
    И отправлен HTTP DELETE запрос на URL "url.pet.petId" с параметрами запроса и ожидается код ответа 200, а ответ сохранён в переменную "delete_pet_response_for_check"
      | PATH_PARAMETER | petId | pet.id |
    И в ответе "delete_pet_response_for_check" statusCode: 200

  # getValuesFromBodyAsString уже покрыта в Предыстории (сохранение body в переменную)
  # Остальные шаги (форматированные данные, сохранение из них) покрыты сценариями выше.