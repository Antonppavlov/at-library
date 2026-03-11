# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов отправки HTTP-запросов (SendRequestSteps)
  Для примеров используется публичное API Petstore (https://petstore.swagger.io).

  # httpRequest (без параметров, с кодом)
  Сценарий: GET без параметров, проверка кода и сохранение ответа
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/store/inventory" код ответа 200 ответ сохранен в "inventory_response"

  # httpRequestWithParams (с таблицей, без кода)
  Сценарий: GET c query-параметром через таблицу и сохранением ответа
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" ответ сохранен в "pets_available_response":
      | PARAMETER | status | available |

  # httpRequestWithParams (с таблицей и кодом)
  Сценарий: GET c заголовком через таблицу и проверкой кода
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" код ответа 200 ответ сохранен в "pets_with_header_response":
      | HEADER | Accept | application/json |

  # httpRequest (полный URL без свойств)
  Сценарий: GET по полному URL
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus?status=pending" код ответа 200 ответ сохранен в "pets_pending_response"

  # pollRequest
  Сценарий: Периодическая проверка статуса без параметров
    И каждые 2с/10с отправлен HTTP GET на "https://petstore.swagger.io/v2/store/inventory" код ответа 200 ответ сохранен в "inventory_periodic_response"

  # pollRequestWithParams
  Сценарий: Периодическая проверка статуса c параметрами
    И каждые 2с/10с отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" код ответа 200 ответ сохранен в "pets_available_periodic_response":
      | PARAMETER | status | available |

  # pollRequestWithParams (с проверкой ответа через RESPONSE-разделитель)
  Сценарий: Периодическая проверка заголовков ответа по таблице
    И каждые 2с/10с отправлен HTTP GET на "https://petstore.swagger.io/v2/store/inventory" код ответа 200 ответ сохранен в "inventory_periodic_checked_response":
      | HEADER   | Accept       | application/json |
      | RESPONSE |              |                  |
      | HEADER   | Content-Type | application/json |

  # Примеры типов параметров запроса
  Сценарий: POST c BODY из JSON-файла
    И отправлен HTTP POST на "https://petstore.swagger.io/v2/pet" код ответа 200 ответ сохранен в "create_pet_prepare_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | json.post.pet    |
    И отправлен HTTP PUT на "https://petstore.swagger.io/v2/pet" код ответа 200 ответ сохранен в "update_pet_prepare_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | json.put.pet     |
    И отправлен HTTP POST на "https://petstore.swagger.io/v2/pet" код ответа 200 ответ сохранен в "create_pet_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | json.post.pet    |

  Сценарий: GET c PATH_PARAMETER
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/{petId}" код ответа 200 ответ сохранен в "get_pet_by_pathparam_response":
      | PATH_PARAMETER | petId  | pet.id           |
      | HEADER         | Accept | application/json |

  Сценарий: DELETE питомца по id
    И отправлен HTTP POST на "https://petstore.swagger.io/v2/pet" код ответа 200 ответ сохранен в "create_pet_for_delete_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | json.post.pet.delete |
    И отправлен HTTP PUT на "https://petstore.swagger.io/v2/pet" код ответа 200 ответ сохранен в "update_pet_for_delete_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | json.put.pet.delete |
    И отправлен HTTP DELETE на "https://petstore.swagger.io/v2/pet/{petId}" код ответа 200 ответ сохранен в "delete_pet_response":
      | PATH_PARAMETER | petId  | pet.id.delete      |

  Сценарий: HEAD запрос к inventory
    И отправлен HTTP HEAD на "https://petstore.swagger.io/v2/store/inventory" ответ сохранен в "inventory_head_response"

  Сценарий: OPTIONS запрос к ресурсу Petstore
    И отправлен HTTP OPTIONS на "https://petstore.swagger.io/v2/pet" ответ сохранен в "pet_options_response"
