# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов отправки HTTP-запросов (SendRequestSteps)
  Для примеров используется публичное API Petstore (https://petstore.swagger.io) и свойства из application.properties.

  @unit_debug
  # httpRequest (без параметров, с кодом)
  Сценарий: GET без параметров, проверка кода и сохранение ответа
    И отправлен HTTP GET на "url.store.inventory" код ответа 200 ответ сохранен в "inventory_response"

  # httpRequestWithParams (с таблицей, без кода)
  Сценарий: GET c query-параметром через таблицу и сохранением ответа
    И отправлен HTTP GET на "url.pet.findByStatus" ответ сохранен в "pets_available_response":
      | PARAMETER | status | available |

  # httpRequestWithParams (с таблицей и кодом)
  Сценарий: GET c заголовком через таблицу и проверкой кода
    И отправлен HTTP GET на "url.pet.findByStatus" код ответа 200 ответ сохранен в "pets_with_header_response":
      | HEADER | Accept | application/json |

  # httpRequestWithParams (ещё один пример c query)
  Сценарий: GET c параметрами через таблицу, проверкой кода и сохранением ответа
    И отправлен HTTP GET на "url.pet.findByStatus" код ответа 200 ответ сохранен в "pets_sold_response":
      | PARAMETER | status | sold |

  # httpRequest (полный URL без свойств)
  Сценарий: GET без параметров, проверкой кода и сохранением ответа по полному URL
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus?status=pending" код ответа 200 ответ сохранен в "pets_pending_response"

  # pollRequest
  Сценарий: Периодическая проверка статуса без параметров
    И каждые 2с/10с отправлен HTTP GET на "url.store.inventory" код ответа 200 ответ сохранен в "inventory_periodic_response"

  # pollRequestWithParams
  Сценарий: Периодическая проверка статуса c параметрами и сохранением ответа
    И каждые 2с/10с отправлен HTTP GET на "url.pet.findByStatus" код ответа 200 ответ сохранен в "pets_available_periodic_response":
      | PARAMETER | status | available |

  # pollRequestWithParams (с проверкой ответа через RESPONSE-разделитель)
  Сценарий: Периодическая проверка тела и заголовков ответа по таблице
    И каждые 2с/10с отправлен HTTP GET на "url.store.inventory" код ответа 200 ответ сохранен в "inventory_periodic_checked_response":
      | HEADER   | Accept       | application/json |
      | RESPONSE |              |                  |
      | HEADER   | Content-Type | application/json |

  # Дополнительные сценарии, показывающие использование разных HTTP-методов и типов параметров

  # =======================================================================
  # ОТПРАВКА JSON ИЗ ФАЙЛА (BODY)
  # =======================================================================

  # BODY из property → путь к classpath-ресурсу (${property.key})
  Сценарий: POST с BODY из JSON-файла через property
    И отправлен HTTP POST на "url.pet" код ответа 200 ответ сохранен в "create_pet_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | ${json.post.pet} |
    И в ответе "create_pet_response" содержимое найденное по jsonPath "name" равно "tomas"

  # BODY из classpath-ресурса напрямую (без property)
  Сценарий: POST с BODY из JSON-файла по classpath-пути
    И отправлен HTTP POST на "url.pet" код ответа 200 ответ сохранен в "create_pet_classpath_response":
      | HEADER | Accept       | application/json             |
      | HEADER | Content-Type | application/json             |
      | BODY   | BODY         | restBodies/post_new_pet.json |
    И в ответе "create_pet_classpath_response" содержимое найденное по jsonPath "name" равно "tomas"

  # BODY из переменной (JSON заранее сохранён через TemplateJsonSteps)
  Сценарий: POST с BODY из заполненного шаблона
    И заполнение JSON-шаблон "json.post.pet" данными из таблицы и сохранение в переменную "custom_pet_body"
      | tomas     | Мурзик    |
      | available | sold      |
    И отправлен HTTP POST на "url.pet" код ответа 200 ответ сохранен в "create_custom_pet_response":
      | HEADER | Accept       | application/json   |
      | HEADER | Content-Type | application/json   |
      | BODY   | BODY         | {custom_pet_body}  |
    И в ответе "create_custom_pet_response" содержимое найденное по jsonPath "name" равно "Мурзик"
    И в ответе "create_custom_pet_response" содержимое найденное по jsonPath "status" равно "sold"

  # BODY из JSON-файла с переменными {pet.id}, {pet.name}, {pet.status} — резолвятся из application.properties
  Сценарий: POST с BODY из JSON-файла с переменными из properties
    И отправлен HTTP POST на "url.pet" код ответа 200 ответ сохранен в "create_pet_vars_response":
      | HEADER | Accept       | application/json                  |
      | HEADER | Content-Type | application/json                  |
      | BODY   | BODY         | restBodies/post_new_pet_vars.json |
    И в ответе "create_pet_vars_response" содержимое найденное по jsonPath "id" равно "17122019"
    И в ответе "create_pet_vars_response" содержимое найденное по jsonPath "name" равно "tomas"
    И в ответе "create_pet_vars_response" содержимое найденное по jsonPath "status" равно "available"

  # PUT с BODY из файла через property
  Сценарий: PUT обновление питомца с JSON из файла
    И отправлен HTTP PUT на "url.pet" код ответа 200 ответ сохранен в "update_pet_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | ${json.put.pet}  |
    И в ответе "update_pet_response" содержимое найденное по jsonPath "name" равно "tomas dangerous"

  # =======================================================================
  # ОСТАЛЬНЫЕ HTTP-МЕТОДЫ И ПАРАМЕТРЫ
  # =======================================================================

  Сценарий: GET питомца по id через переменную в URL
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/{pet.id}" код ответа 200 ответ сохранен в "get_pet_by_id_response"

  Сценарий: GET питомца по id c PATH_PARAMETER и заголовком
    И отправлен HTTP GET на "url.pet.petId" код ответа 200 ответ сохранен в "get_pet_by_pathparam_response":
      | PATH_PARAMETER | petId  | pet.id           |
      | HEADER         | Accept | application/json |

  Сценарий: DELETE питомца по id
    И отправлен HTTP DELETE на "https://petstore.swagger.io/v2/pet/{pet.id}" код ответа 200 ответ сохранен в "delete_pet_response"

  Сценарий: HEAD запрос к inventory без проверок кода
    И отправлен HTTP HEAD на "url.store.inventory" ответ сохранен в "inventory_head_response"

  Сценарий: OPTIONS запрос к ресурсу Petstore
    И отправлен HTTP OPTIONS на "url.pet" ответ сохранен в "pet_options_response"
