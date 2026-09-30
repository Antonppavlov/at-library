# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов отправки HTTP-запросов (SendRequestSteps)
  Для примеров используются публичные API: Petstore (https://petstore.swagger.io) и httpbin (https://httpbin.org).
  Petstore нужен для «обычных» сценариев работы с API, httpbin возвращает в ответе то, что получил от клиента,
  поэтому по нему видно, что параметр таблицы действительно дошёл до сервера, а не просто «запрос вернул 200».
  Проверки в этом файле используют только шаги библиотеки, поэтому файл можно копировать как пример.

  # =======================================================================
  # httpRequest / httpRequestWithParams / pollRequest / pollRequestWithParams на Petstore
  # =======================================================================

  # httpRequest (без параметров, с кодом)
  Сценарий: GET без параметров, проверка кода и сохранение ответа
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/store/inventory" код ответа 200 ответ сохранен в "inventory_response"
    Тогда в ответе "inventory_response" statusCode: 200
    И в ответе "inventory_response" headers равны значениям из таблицы:
      | Content-Type | application/json |

  # httpRequestWithParams (с таблицей, без кода)
  # Без параметра status Petstore возвращает пустой массив, поэтому непустой массив со статусом available
  # доказывает, что PARAMETER действительно был отправлен.
  Сценарий: GET c query-параметром через таблицу и сохранением ответа
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" ответ сохранен в "pets_available_response":
      | PARAMETER | status | available |
    Тогда в ответе "pets_available_response" statusCode: 200
    И в ответе "pets_available_response" массив значений найденных по jsonPath "status" не пустой
    И в ответе "pets_available_response" массив значений найденных по jsonPath "status" все значения равны "available"

  # httpRequestWithParams (с таблицей и кодом)
  # Petstore выбирает формат ответа по Accept: XML в ответе доказывает, что HEADER дошёл до сервера.
  Сценарий: GET c заголовком через таблицу и проверкой кода
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" код ответа 200 ответ сохранен в "pets_with_header_response":
      | HEADER    | Accept | application/json |
      | PARAMETER | status | available        |
    Тогда в ответе "pets_with_header_response" headers равны значениям из таблицы:
      | Content-Type | application/json |

  Сценарий: GET c заголовком Accept меняет формат ответа
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" код ответа 200 ответ сохранен в "pets_xml_response":
      | HEADER    | Accept | application/xml |
      | PARAMETER | status | available       |
    Тогда в ответе "pets_xml_response" headers равны значениям из таблицы:
      | Content-Type | application/xml |

  # httpRequest (полный URL без свойств, query прямо в адресе)
  Сценарий: GET по полному URL
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus?status=available" код ответа 200 ответ сохранен в "pets_available_url_response"
    Тогда в ответе "pets_available_url_response" массив значений найденных по jsonPath "status" не пустой
    И в ответе "pets_available_url_response" массив значений найденных по jsonPath "status" все значения равны "available"

  # pollRequest
  Сценарий: Периодическая проверка статуса без параметров
    И каждые 2с/10с отправлен HTTP GET на "https://petstore.swagger.io/v2/store/inventory" код ответа 200 ответ сохранен в "inventory_periodic_response"
    Тогда в ответе "inventory_periodic_response" headers равны значениям из таблицы:
      | Content-Type | application/json |

  # pollRequestWithParams
  Сценарий: Периодическая проверка статуса c параметрами
    И каждые 2с/10с отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" код ответа 200 ответ сохранен в "pets_available_periodic_response":
      | PARAMETER | status | available |
    Тогда в ответе "pets_available_periodic_response" массив значений найденных по jsonPath "status" не пустой
    И в ответе "pets_available_periodic_response" массив значений найденных по jsonPath "status" все значения равны "available"

  # pollRequestWithParams (с проверкой ответа через RESPONSE-разделитель)
  # Условие после RESPONSE проверяется на каждой итерации: при неверном значении шаг ждал бы до таймаута и упал.
  Сценарий: Периодическая проверка заголовков ответа по таблице
    И каждые 2с/10с отправлен HTTP GET на "https://petstore.swagger.io/v2/store/inventory" код ответа 200 ответ сохранен в "inventory_periodic_checked_response":
      | HEADER   | Accept       | application/json |
      | RESPONSE |              |                  |
      | HEADER   | Content-Type | application/json |
    Тогда в ответе "inventory_periodic_checked_response" headers равны значениям из таблицы:
      | Content-Type | application/json |

  # Примеры типов параметров запроса. Petstore возвращает в ответе созданный/изменённый объект,
  # поэтому значения из ответа проверяются на равенство отправленным (они не зависят от параллельных сценариев).
  Сценарий: POST c BODY из JSON-файла
    И отправлен HTTP POST на "https://petstore.swagger.io/v2/pet" код ответа 200 ответ сохранен в "create_pet_prepare_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | json.post.pet    |
    Тогда в ответе "create_pet_prepare_response" содержимые найденные по jsonPath равны:
      | id     | pet.id     |
      | name   | pet.name   |
      | status | pet.status |
    И отправлен HTTP PUT на "https://petstore.swagger.io/v2/pet" код ответа 200 ответ сохранен в "update_pet_prepare_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | json.put.pet     |
    И в ответе "update_pet_prepare_response" содержимые найденные по jsonPath равны:
      | id             | pet.id          |
      | name           | tomas dangerous |
      | tags[0].name   | wild cat        |
    И отправлен HTTP POST на "https://petstore.swagger.io/v2/pet" код ответа 200 ответ сохранен в "create_pet_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | json.post.pet    |
    И в ответе "create_pet_response" содержимые найденные по jsonPath равны:
      | id           | pet.id      |
      | name         | pet.name    |
      | tags[0].name | domestic cat |

  Сценарий: GET c PATH_PARAMETER
    И отправлен HTTP POST на "https://petstore.swagger.io/v2/pet" код ответа 200 ответ сохранен в "create_pet_for_pathparam_response":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | json.post.pet    |
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/{petId}" код ответа 200 ответ сохранен в "get_pet_by_pathparam_response":
      | PATH_PARAMETER | petId  | pet.id           |
      | HEADER         | Accept | application/json |
    Тогда в ответе "get_pet_by_pathparam_response" содержимое найденное по jsonPath "id" равно "pet.id"

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
    Тогда в ответе "delete_pet_response" содержимые найденные по jsonPath равны:
      | code    | 200           |
      | message | pet.id.delete |

  Сценарий: HEAD запрос к inventory
    И отправлен HTTP HEAD на "https://petstore.swagger.io/v2/store/inventory" ответ сохранен в "inventory_head_response"
    Тогда в ответе "inventory_head_response" statusCode: 200
    И в ответе "inventory_head_response" headers равны значениям из таблицы:
      | Content-Type | application/json |

  Сценарий: OPTIONS запрос к ресурсу Petstore
    И отправлен HTTP OPTIONS на "https://petstore.swagger.io/v2/pet" ответ сохранен в "pet_options_response"
    Тогда в ответе "pet_options_response" statusCode: 204
    И в ответе "pet_options_response" headers равны значениям из таблицы:
      | Access-Control-Allow-Origin | * |

  # =======================================================================
  # Что именно дошло до сервера (httpbin возвращает полученное в JSON)
  # =======================================================================

  Структура сценария: Метод <method> доходит до сервера
    И отправлен HTTP <method> на "https://httpbin.org/anything" код ответа 200 ответ сохранен в "method_response"
    Тогда в ответе "method_response" содержимое найденное по jsonPath "method" равно "<method>"

    Примеры:
      | method |
      | GET    |
      | POST   |
      | PUT    |
      | PATCH  |
      | DELETE |

  Сценарий: PARAMETER доходит до сервера как query
    И отправлен HTTP GET на "https://httpbin.org/anything" код ответа 200 ответ сохранен в "echo_query_response":
      | PARAMETER | status | available |
      | PARAMETER | tag    | cat       |
    Тогда в ответе "echo_query_response" содержимые найденные по jsonPath равны:
      | args.status | available |
      | args.tag    | cat       |

  Сценарий: HEADER доходит до сервера
    И отправлен HTTP GET на "https://httpbin.org/anything" код ответа 200 ответ сохранен в "echo_header_response":
      | HEADER | X-Test | value-1 |
    Тогда в ответе "echo_header_response" содержимое найденное по jsonPath "headers.'X-Test'" равно "value-1"

  Сценарий: COOKIES доходят до сервера
    И отправлен HTTP GET на "https://httpbin.org/cookies" код ответа 200 ответ сохранен в "echo_cookie_response":
      | COOKIES | sid   | 42   |
      | COOKIES | theme | dark |
    Тогда в ответе "echo_cookie_response" содержимые найденные по jsonPath равны:
      | cookies.sid   | 42   |
      | cookies.theme | dark |

  Сценарий: FORM_PARAMETER доходит до сервера как поле формы
    И отправлен HTTP POST на "https://httpbin.org/post" код ответа 200 ответ сохранен в "echo_form_response":
      | FORM_PARAMETER | grant_type | password |
      | FORM_PARAMETER | username   | ivan     |
    Тогда в ответе "echo_form_response" содержимые найденные по jsonPath равны:
      | form.grant_type | password |
      | form.username   | ivan     |

  Сценарий: PATH_PARAMETER подставляется в адрес
    И отправлен HTTP GET на "https://httpbin.org/anything/items/{itemId}" код ответа 200 ответ сохранен в "echo_path_response":
      | PATH_PARAMETER | itemId | 77 |
    Тогда в ответе "echo_path_response" содержимое найденное по jsonPath "url" равно "https://httpbin.org/anything/items/77"

  Сценарий: BODY из файла доходит до сервера как JSON
    И отправлен HTTP POST на "https://httpbin.org/post" код ответа 200 ответ сохранен в "echo_body_response":
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | json.post.pet    |
    Тогда в ответе "echo_body_response" содержимые найденные по jsonPath равны:
      | json.id            | pet.id   |
      | json.name          | pet.name |
      | json.category.name | cat      |

  Сценарий: BODY из файла-шаблона получает значения плейсхолдеров из properties
    И отправлен HTTP POST на "https://httpbin.org/post" код ответа 200 ответ сохранен в "echo_body_vars_response":
      | HEADER | Content-Type | application/json   |
      | BODY   | BODY         | json.post.pet.vars |
    Тогда в ответе "echo_body_vars_response" содержимые найденные по jsonPath равны:
      | json.id   | pet.id   |
      | json.name | pet.name |

  Сценарий: BASIC_AUTHENTICATION — сервер запрашивает авторизацию (WWW-Authenticate)
    И отправлен HTTP GET на "https://httpbin.org/basic-auth/user/pass" код ответа 200 ответ сохранен в "basic_challenge_response":
      | BASIC_AUTHENTICATION | user | pass |
    Тогда в ответе "basic_challenge_response" содержимые найденные по jsonPath равны:
      | authenticated | true |
      | user          | user |

  # hidden-basic-auth отвечает 404 (а не 401 с WWW-Authenticate), пока не получит логин и пароль в первом же запросе
  Сценарий: BASIC_AUTHENTICATION — логин и пароль отправляются сразу, без ожидания 401
    И отправлен HTTP GET на "https://httpbin.org/hidden-basic-auth/user/pass" код ответа 200 ответ сохранен в "basic_hidden_response":
      | BASIC_AUTHENTICATION | user | pass |
    Тогда в ответе "basic_hidden_response" содержимое найденное по jsonPath "authenticated" равно "true"

  Сценарий: ACCESS_TOKEN — Bearer-токен доходит до сервера
    И отправлен HTTP GET на "https://httpbin.org/bearer" код ответа 200 ответ сохранен в "bearer_response":
      | ACCESS_TOKEN | Authorization | my-token |
    Тогда в ответе "bearer_response" содержимые найденные по jsonPath равны:
      | authenticated | true     |
      | token         | my-token |

  Сценарий: MULTIPART — текстовые части доходят до сервера
    И отправлен HTTP POST на "https://httpbin.org/post" код ответа 200 ответ сохранен в "multipart_response":
      | MULTIPART | title       | hello |
      | MULTIPART | description | world |
    Тогда в ответе "multipart_response" содержимые найденные по jsonPath равны:
      | form.title       | hello |
      | form.description | world |

  Сценарий: FILE — файл доходит до сервера как multipart-часть file
    И отправлен HTTP POST на "https://httpbin.org/post" код ответа 200 ответ сохранен в "file_response":
      | FILE | text/plain | src/test/resources/xml/template_order.xml |
    И body ответа "file_response" сохранено в переменную "file_response_body"
    Тогда в JSON строке "{file_response_body}" значения соответствуют таблице:
      | $.files.file | ~ | (?s).*<order>.*</order>.* |

  Сценарий: Периодическая отправка тоже передаёт параметры запроса
    И каждые 2с/10с отправлен HTTP GET на "https://httpbin.org/anything" код ответа 200 ответ сохранен в "poll_echo_response":
      | PARAMETER | status | available |
      | HEADER    | X-Test | polling   |
    Тогда в ответе "poll_echo_response" содержимые найденные по jsonPath равны:
      | args.status         | available |
      | headers.'X-Test'    | polling   |
