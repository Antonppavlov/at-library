# language: ru
@offline
Функционал: Оффлайн-контракт шагов SendRequestSteps
  Запросы уходят на локальный сервер, который возвращает то, что получил (метод, query, заголовки, cookies, form, multipart, body).

  Предыстория: Локальный сервер
    Дано запущен локальный HTTP сервер

  # =======================================================================
  # httpRequest / httpRequestWithParams: методы
  # =======================================================================

  Структура сценария: Метод <method> доходит до сервера
    Когда отправлен HTTP <method> на "local_echo" код ответа 200 ответ сохранен в "r"
    Тогда в ответе "r" содержимое найденное по jsonPath "method" равно "<method>"

    Примеры:
      | method  |
      | GET     |
      | POST    |
      | PUT     |
      | PATCH   |
      | DELETE  |
      | OPTIONS |

  Сценарий: HEAD-запрос возвращает заголовки без тела
    Когда отправлен HTTP HEAD на "local_headers" код ответа 200 ответ сохранен в "r"
    Тогда в ответе "r" headers равны значениям из таблицы:
      | X-Custom | custom-value |

  Сценарий: TRACE-запрос доходит до сервера
    Когда отправлен HTTP TRACE на "local_echo" ответ сохранен в "r"
    Тогда в ответе "r" содержимое найденное по jsonPath "method" равно "TRACE"

  Сценарий: Запрос без таблицы и без ожидаемого кода
    Когда отправлен HTTP GET на "local_status" ответ сохранен в "not_found":
      | PATH_PARAMETER | code | 404 |
    Тогда в ответе "not_found" statusCode: 404

  # =======================================================================
  # ожидаемый код ответа
  # =======================================================================

  Сценарий: Код ответа совпал — с таблицей и без неё
    Когда отправлен HTTP GET на "local_status" код ответа 201 ответ сохранен в "created":
      | PATH_PARAMETER | code | 201 |
    И отправлен HTTP GET на "local_text" код ответа 200 ответ сохранен в "ok"
    Тогда в ответе "created" statusCode: 201

  Сценарий: Код ответа не совпал — запрос без таблицы
    Тогда выполнение шага завершается ошибкой, содержащей "Ожидался статус 200 для HTTP GET":
      """
      отправлен HTTP GET на "local_flaky_twice" код ответа 200 ответ сохранен в "r"
      """

  Сценарий: Код ответа не совпал — запрос с таблицей
    Тогда выполнение шага завершается ошибкой, содержащей "после 1 попыток, фактически 404":
      """
      отправлен HTTP GET на "local_status" код ответа 200 ответ сохранен в "r":
      | PATH_PARAMETER | code | 404 |
      """

  # =======================================================================
  # повторы запроса (request.retries), не polling
  # =======================================================================

  Сценарий: Повторы запроса — успех на третьей попытке
    Дано количество повторов запроса равно 3
    Когда отправлен HTTP GET на "local_flaky" код ответа 200 ответ сохранен в "r":
      | PATH_PARAMETER | key      | retry_ok |
      | PATH_PARAMETER | failures | 2        |
    Тогда в ответе "r" содержимое найденное по jsonPath "attempt" равно "3"

  Сценарий: Повторы запроса — попытки исчерпаны
    Дано количество повторов запроса равно 2
    Тогда выполнение шага завершается ошибкой, содержащей "после 2 попыток, фактически 503":
      """
      отправлен HTTP GET на "local_flaky" код ответа 200 ответ сохранен в "r":
      | PATH_PARAMETER | key      | retry_fail |
      | PATH_PARAMETER | failures | 5          |
      """

  # =======================================================================
  # типы параметров таблицы
  # =======================================================================

  Сценарий: PARAMETER — query-параметры, в том числе повторяющиеся
    Когда отправлен HTTP GET на "local_echo" код ответа 200 ответ сохранен в "r":
      | PARAMETER | status | available |
      | PARAMETER | tag    | a         |
      | PARAMETER | tag    | b         |
    Тогда в ответе "r" содержимые найденные по jsonPath равны:
      | query.status[0] | available |
      | query.tag[0]    | a         |
      | query.tag[1]    | b         |

  Сценарий: HEADER — произвольные заголовки
    Когда отправлен HTTP GET на "local_echo" код ответа 200 ответ сохранен в "r":
      | HEADER | X-Test | value-1          |
      | HEADER | Accept | application/json |
    Тогда в ответе "r" содержимые найденные по jsonPath равны:
      | headers.'x-test' | value-1          |
      | headers.accept   | application/json |

  Сценарий: COOKIES — cookies запроса
    Когда отправлен HTTP GET на "local_echo" код ответа 200 ответ сохранен в "r":
      | COOKIES | sid   | 42   |
      | COOKIES | theme | dark |
    Тогда в ответе "r" содержимые найденные по jsonPath равны:
      | cookies.sid   | 42   |
      | cookies.theme | dark |

  Сценарий: FORM_PARAMETER — поля формы
    Когда отправлен HTTP POST на "local_echo" код ответа 200 ответ сохранен в "r":
      | FORM_PARAMETER | grant_type | password |
      | FORM_PARAMETER | username   | ivan     |
    Тогда в ответе "r" содержимые найденные по jsonPath равны:
      | form.grant_type[0] | password |
      | form.username[0]   | ivan     |
    И body ответа "r" сохранено в переменную "r_body"
    И в JSON строке "{r_body}" значения соответствуют таблице:
      | $.contentType | ~ | application/x-www-form-urlencoded.* |

  Сценарий: PATH_PARAMETER — подстановка в путь
    Когда отправлен HTTP GET на "local_echo_by_id" код ответа 200 ответ сохранен в "r":
      | PATH_PARAMETER | id | 77 |
    Тогда в ответе "r" содержимое найденное по jsonPath "path" равно "/echo/items/77"

  Сценарий: BODY — строка JSON прямо в таблице
    Когда отправлен HTTP POST на "local_echo" код ответа 200 ответ сохранен в "r":
      | HEADER | Content-Type | application/json |
      | BODY   | body         | {"name":"alpha"} |
    Тогда из ответа "r" содержимое найденное по jsonPath "body" сохранено в "sent_body"
    И в JSON строке "{sent_body}" значения соответствуют таблице:
      | $.name | == | alpha |
    И body ответа "r" сохранено в переменную "r_body"
    И в JSON строке "{r_body}" значения соответствуют таблице:
      | $.contentType | ~ | application/json.* |

  Сценарий: BODY — файл-шаблон, плейсхолдеры подставляются из переменных сценария
    Дано установлено значение переменной "user_name" равным "Ivan"
    И установлено значение переменной "user_age" равным "30"
    Когда отправлен HTTP POST на "local_echo" код ответа 200 ответ сохранен в "r":
      | HEADER | Content-Type | application/json           |
      | BODY   | body         | offline/bodies/user.json   |
    И из ответа "r" содержимое найденное по jsonPath "body" сохранено в "sent_body"
    Тогда в JSON строке "{sent_body}" значения соответствуют таблице:
      | $.name | == | Ivan |
      | $.age  | == | 30   |

  Сценарий: BODY — тело берётся из переменной сценария
    Дано установлено значение переменной "prepared_body" равным "{"prepared":true}"
    Когда отправлен HTTP POST на "local_echo" код ответа 200 ответ сохранен в "r":
      | HEADER | Content-Type | application/json |
      | BODY   | body         | prepared_body    |
    Тогда из ответа "r" содержимое найденное по jsonPath "body" сохранено в "sent_body"
    И в JSON строке "{sent_body}" значения соответствуют таблице:
      | $.prepared | == | true |

  Сценарий: ACCESS_TOKEN — Bearer-токен добавляется в Authorization
    Когда отправлен HTTP GET на "local_bearer" код ответа 200 ответ сохранен в "r":
      | ACCESS_TOKEN | Authorization | token-123 |
    Тогда в ответе "r" содержимое найденное по jsonPath "scheme" равно "bearer"

  Сценарий: ACCESS_TOKEN — неверный токен отклоняется сервером
    Тогда выполнение шага завершается ошибкой, содержащей "фактически 401":
      """
      отправлен HTTP GET на "local_bearer" код ответа 200 ответ сохранен в "r":
      | ACCESS_TOKEN | Authorization | wrong-token |
      """

  Сценарий: BASIC_AUTHENTICATION — сервер запрашивает авторизацию заголовком WWW-Authenticate
    Когда отправлен HTTP GET на "local_basic" код ответа 200 ответ сохранен в "r":
      | BASIC_AUTHENTICATION | user | pass |
    Тогда в ответе "r" содержимое найденное по jsonPath "scheme" равно "basic"

  Сценарий: BASIC_AUTHENTICATION — сервер отвечает 401 без WWW-Authenticate
    Когда отправлен HTTP GET на "local_basic_no_challenge" код ответа 200 ответ сохранен в "r":
      | BASIC_AUTHENTICATION | user | pass |
    Тогда в ответе "r" содержимое найденное по jsonPath "scheme" равно "basic"

  Сценарий: MULTIPART — текстовые части
    Когда отправлен HTTP POST на "local_echo" код ответа 200 ответ сохранен в "r":
      | MULTIPART | title       | hello |
      | MULTIPART | description | world |
    Тогда в ответе "r" содержимые найденные по jsonPath равны:
      | multipart[0].name    | title |
      | multipart[0].content | hello |
      | multipart[1].name    | description |
      | multipart[1].content | world |

  Сценарий: FILE — файл отправляется как multipart-часть "file"
    Когда отправлен HTTP POST на "local_echo" код ответа 200 ответ сохранен в "r":
      | FILE | text/plain | src/test/resources/offline/upload/sample.txt |
    Тогда в ответе "r" содержимые найденные по jsonPath равны:
      | multipart[0].name        | file         |
      | multipart[0].filename    | sample.txt   |
      | multipart[0].contentType | text/plain   |
      | multipart[0].content     | hello upload |

  Сценарий: FILE — путь к файлу берётся из переменной сценария в фигурных скобках
    Дано установлено значение переменной "upload_path" равным "src/test/resources/offline/upload/sample.txt"
    Когда отправлен HTTP POST на "local_echo" код ответа 200 ответ сохранен в "r":
      | FILE | text/plain | {upload_path} |
    Тогда в ответе "r" содержимые найденные по jsonPath равны:
      | multipart[0].filename | sample.txt   |
      | multipart[0].content  | hello upload |

  Сценарий: FILE — путь к файлу берётся из property в фигурных скобках
    Когда отправлен HTTP POST на "local_echo" код ответа 200 ответ сохранен в "r":
      | FILE | text/plain | {offline.upload.file} |
    Тогда в ответе "r" содержимые найденные по jsonPath равны:
      | multipart[0].filename | sample.txt   |
      | multipart[0].content  | hello upload |

  Сценарий: FILE — имя property без фигурных скобок
    Когда отправлен HTTP POST на "local_echo" код ответа 200 ответ сохранен в "r":
      | FILE | text/plain | offline.upload.file |
    Тогда в ответе "r" содержимые найденные по jsonPath равны:
      | multipart[0].filename | sample.txt   |
      | multipart[0].content  | hello upload |

  Сценарий: Неизвестный тип параметра таблицы
    Тогда выполнение шага завершается ошибкой, содержащей "Некорректно задан тип QUERY_PARAMETER для параметра запроса status":
      """
      отправлен HTTP GET на "local_echo" ответ сохранен в "r":
      | QUERY_PARAMETER | status | available |
      """

  # =======================================================================
  # HTTPS: RELAXED_HTTPS
  # =======================================================================

  Сценарий: RELAXED_HTTPS — запрос к серверу с самоподписанным сертификатом
    Дано запущен локальный HTTPS сервер с самоподписанным сертификатом
    Когда отправлен HTTP GET на "local_https_echo" код ответа 200 ответ сохранен в "r":
      | RELAXED_HTTPS | true | true |
    Тогда в ответе "r" содержимое найденное по jsonPath "method" равно "GET"

  Сценарий: Без RELAXED_HTTPS самоподписанный сертификат отклоняется
    Дано запущен локальный HTTPS сервер с самоподписанным сертификатом
    Тогда выполнение шага завершается ошибкой, содержащей "SSLHandshakeException":
      """
      отправлен HTTP GET на "local_https_echo" ответ сохранен в "r"
      """

  # =======================================================================
  # polling: каждые Nс/Mс
  # =======================================================================

  Сценарий: Polling без таблицы — дожидаемся нужного кода ответа
    Когда каждые 1с/10с отправлен HTTP GET на "local_flaky_twice" код ответа 200 ответ сохранен в "r"
    Тогда в ответе "r" содержимое найденное по jsonPath "state" равно "OK"
    И в ответе "r" содержимое найденное по jsonPath "attempt" равно "3"

  Сценарий: Polling с таблицей параметров запроса
    Когда каждые 1с/10с отправлен HTTP GET на "local_flaky" код ответа 200 ответ сохранен в "r":
      | PATH_PARAMETER | key      | poll_table |
      | PATH_PARAMETER | failures | 1          |
      | HEADER         | X-Test   | polling    |
    Тогда в ответе "r" содержимое найденное по jsonPath "attempt" равно "2"

  Сценарий: Polling с условием по заголовку ответа (разделитель RESPONSE)
    Когда каждые 1с/10с отправлен HTTP GET на "local_poll_ready_on_3" код ответа 200 ответ сохранен в "r":
      | HEADER   | Accept  | application/json |
      | RESPONSE |         |                  |
      | HEADER   | X-State | READY            |
    Тогда в ответе "r" содержимые найденные по jsonPath равны:
      | attempt | 3     |
      | state   | READY |

  Сценарий: Polling с условием по cookie ответа
    Когда каждые 1с/10с отправлен HTTP GET на "local_poll" код ответа 200 ответ сохранен в "r":
      | PATH_PARAMETER | key         | poll_cookie |
      | PATH_PARAMETER | ready_after | 2           |
      | RESPONSE       |             |             |
      | COOKIES        | state       | READY       |
    Тогда в ответе "r" содержимое найденное по jsonPath "attempt" равно "2"

  Сценарий: Polling с условием по телу ответа целиком
    Когда каждые 1с/10с отправлен HTTP GET на "local_poll" код ответа 200 ответ сохранен в "r":
      | PATH_PARAMETER | key         | poll_body |
      | PATH_PARAMETER | ready_after | 2         |
      | RESPONSE       |             |           |
      | BODY           |             | {"attempt":2,"state":"READY"} |
    Тогда в ответе "r" содержимое найденное по jsonPath "state" равно "READY"

  Сценарий: Polling с условием по телу ответа — имя параметра в таблице заполнено (как в README)
    Когда каждые 1с/10с отправлен HTTP GET на "local_poll" код ответа 200 ответ сохранен в "r":
      | PATH_PARAMETER | key         | poll_body_named |
      | PATH_PARAMETER | ready_after | 2               |
      | RESPONSE       |             |                 |
      | BODY           | BODY        | {"attempt":2,"state":"READY"} |
    Тогда в ответе "r" содержимое найденное по jsonPath "state" равно "READY"

  Сценарий: Polling — код ответа так и не совпал за отведённое время
    Тогда выполнение шага завершается ошибкой, содержащей "Ожидался статус 200 для HTTP GET":
      """
      каждые 1с/2с отправлен HTTP GET на "local_status" код ответа 200 ответ сохранен в "r":
      | PATH_PARAMETER | code | 503 |
      """

  Сценарий: Polling — условие по ответу не выполнилось за отведённое время
    Тогда выполнение шага завершается ошибкой, содержащей "X-State":
      """
      каждые 1с/2с отправлен HTTP GET на "local_poll" код ответа 200 ответ сохранен в "r":
      | PATH_PARAMETER | key         | poll_never |
      | PATH_PARAMETER | ready_after | 1000       |
      | RESPONSE       |             |            |
      | HEADER         | X-State     | READY      |
      """

  Сценарий: Polling — неизвестный тип условия по ответу
    Тогда выполнение шага завершается ошибкой, содержащей "Некорректно задан тип STATUS для параметра ответа":
      """
      каждые 1с/2с отправлен HTTP GET на "local_text" код ответа 200 ответ сохранен в "r":
      | RESPONSE |        |     |
      | STATUS   | status | 200 |
      """
