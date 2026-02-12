# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов отправки HTTP-запросов (SendRequestSteps)
  Для примеров используется публичное API Petstore (https://petstore.swagger.io) и свойства из application.properties.

  @unit_debug
  # sendHttpRequestExpectStatusAndSave (без параметров)
  Сценарий: GET без параметров, проверка кода и сохранение ответа
    И отправлен HTTP GET запрос на URL "url.store.inventory" и ожидается код ответа 200, а ответ сохранён в переменную "inventory_response"

  # sendHttpRequestWithParamsAndSave
  Сценарий: GET c query-параметром через таблицу и сохранением ответа
    И отправлен HTTP GET запрос на URL "url.pet.findByStatus" с параметрами запроса и ответ сохранён в переменную "pets_available_response"
      | PARAMETER | status | available |

  # sendHttpRequestWithParamsExpectStatusAndSave
  Сценарий: GET c заголовком через таблицу и проверкой кода
    И отправлен HTTP GET запрос на URL "url.pet.findByStatus" с параметрами запроса и ожидается код ответа 200, а ответ сохранён в переменную "pets_with_header_response"
      | HEADER | Accept | application/json |

  # sendHttpRequestWithParamsExpectStatusAndSave (ещё один пример c query)
  Сценарий: GET c параметрами через таблицу, проверкой кода и сохранением ответа
    И отправлен HTTP GET запрос на URL "url.pet.findByStatus" с параметрами запроса и ожидается код ответа 200, а ответ сохранён в переменную "pets_sold_response"
      | PARAMETER | status | sold |

  # sendHttpRequestExpectStatusAndSave (полный URL без свойств)
  Сценарий: GET без параметров, проверкой кода и сохранением ответа по полному URL
    И отправлен HTTP GET запрос на URL "https://petstore.swagger.io/v2/pet/findByStatus?status=pending" и ожидается код ответа 200, а ответ сохранён в переменную "pets_pending_response"

  # pollHttpRequestAndSave
  Сценарий: Периодическая проверка статуса без параметров
    И в течение 10 секунд каждые 2 секунд отправляется HTTP GET запрос на URL "url.store.inventory" и ожидается код ответа 200, а ответ сохранён в переменную "inventory_periodic_response"

  # pollHttpRequestWithParamsAndSave
  Сценарий: Периодическая проверка статуса c параметрами и сохранением ответа
    И в течение 10 секунд каждые 2 секунд отправляется HTTP GET запрос на URL "url.pet.findByStatus" с параметрами запроса и ожидается код ответа 200, а ответ сохранён в переменную "pets_available_periodic_response"
      | PARAMETER | status | available |

  # pollHttpRequestWithParamsAndResponseCheck
  Сценарий: Периодическая проверка тела и заголовков ответа по таблице
    И в течение 10 секунд каждые 2 секунд отправляется HTTP GET запрос на URL "url.store.inventory" с параметрами запроса и ожидается код ответа 200 и параметры ответа по таблице, а ответ сохранён в переменную "inventory_periodic_checked_response"
      | HEADER   | Accept       | application/json |
      | RESPONSE |             |                  |
      | HEADER   | Content-Type | application/json |

  # Дополнительные сценарии, показывающие использование разных HTTP-методов и типов параметров

  Сценарий: POST создание питомца по Petstore (BODY из JSON-шаблона)
    И отправлен HTTP POST запрос на URL "url.pet" с параметрами запроса и ожидается код ответа 200, а ответ сохранён в переменную "create_pet_response"
      | HEADER | Accept       | application/json        |
      | HEADER | Content-Type | application/json        |
      | BODY   | BODY         | ${json.post.pet}        |

  Сценарий: GET питомца по id через переменную в URL
    И отправлен HTTP GET запрос на URL "https://petstore.swagger.io/v2/pet/{pet.id}" и ожидается код ответа 200, а ответ сохранён в переменную "get_pet_by_id_response"

  Сценарий: GET питомца по id c PATH_PARAMETER и заголовком
    И отправлен HTTP GET запрос на URL "url.pet.petId" с параметрами запроса и ожидается код ответа 200, а ответ сохранён в переменную "get_pet_by_pathparam_response"
      | PATH_PARAMETER | petId | pet.id           |
      | HEADER         | Accept | application/json |

  Сценарий: PUT обновление питомца по Petstore
    И отправлен HTTP PUT запрос на URL "url.pet" с параметрами запроса и ожидается код ответа 200, а ответ сохранён в переменную "update_pet_response"
      | HEADER | Accept       | application/json      |
      | HEADER | Content-Type | application/json      |
      | BODY   | BODY         | ${json.put.pet}       |

  Сценарий: DELETE питомца по id
    И отправлен HTTP DELETE запрос на URL "https://petstore.swagger.io/v2/pet/{pet.id}" и ожидается код ответа 200, а ответ сохранён в переменную "delete_pet_response"

  Сценарий: HEAD запрос к inventory без проверок кода
    И отправлен HTTP HEAD запрос на URL "url.store.inventory" и ответ сохранён в переменную "inventory_head_response"

  Сценарий: OPTIONS запрос к ресурсу Petstore
    И отправлен HTTP OPTIONS запрос на URL "url.pet" и ответ сохранён в переменную "pet_options_response"