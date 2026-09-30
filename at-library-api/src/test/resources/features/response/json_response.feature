# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов работы с JSON-ответами (JsonResponseSteps)
  Для примеров используется публичное API Petstore (https://petstore.swagger.io).

  Предыстория: Получение списка доступных питомцев
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" код ответа 200 ответ сохранен в "json_response":
      | PARAMETER | status | available |

  # =======================================================================
  # ПРОВЕРКА ЗНАЧЕНИЙ
  # =======================================================================

  # checkJsonResponseValue — одно значение по jsonPath
  Сценарий: Проверка одного значения по jsonPath
    И в ответе "json_response" содержимое найденное по jsonPath "[0].status" равно "available"

  # saveJsonResponseValue — сохранение одного значения по jsonPath
  Сценарий: Сохранение одного значения по jsonPath в переменную
    И из ответа "json_response" содержимое найденное по jsonPath "[0].status" сохранено в "first_status"
    И значение переменной "first_status" равно "available"

  # checkJsonResponseValues — проверка нескольких значений по таблице
  Сценарий: Проверка нескольких значений по jsonPath с учётом регистра
    И в ответе "json_response" содержимые найденные по jsonPath равны:
      | [0].status | available |

  # checkJsonResponseValues — без учёта регистра
  Сценарий: Проверка нескольких значений по jsonPath без учёта регистра
    И в ответе "json_response" содержимые найденные по jsonPath без учета регистра равны:
      | [0].status | AVAILABLE |

  # =======================================================================
  # СОХРАНЕНИЕ
  # =======================================================================

  # saveFromJsonResponse — сохранение нескольких значений по таблице
  Сценарий: Сохранение нескольких значений из JSON по jsonPath
    И из ответа "json_response" содержимые найденные по jsonPath сохранены в переменные:
      | [0].status | saved_status |
      | [0].name   | saved_name   |

  # =======================================================================
  # СРАВНЕНИЕ
  # =======================================================================

  # compareJsonResponses — сравнение двух ответов по jsonPath
  Сценарий: Сравнение значений по jsonPath между двумя ответами
    И отправлен HTTP GET на "https://petstore.swagger.io/v2/pet/findByStatus" код ответа 200 ответ сохранен в "json_response_2":
      | PARAMETER | status | available |
    И в ответах "json_response" и "json_response_2" содержимые найденные по jsonPath совпадают:
      | [0].status |

  # compareJsonBody — сравнение body с эталонным JSON (строгое, побайтовое: httpbin /json отдаёт неизменный документ)
  Сценарий: Сравнение body ответа с эталонным JSON
    И отправлен HTTP GET на "https://httpbin.org/json" код ответа 200 ответ сохранен в "slideshow_response"
    И в ответе "slideshow_response" содержимое равно json "json/httpbin_slideshow.json"
    И в ответе "slideshow_response" содержимое найденное по jsonPath "slideshow.slides[0].title" равно "Wake up to WonderWidgets!"

  # Petstore возвращает созданный объект: проверяем значения из ответа на POST
  Сценарий: Создание питомца — значения ответа равны отправленным
    И отправлен HTTP POST на "https://petstore.swagger.io/v2/pet" код ответа 200 ответ сохранен в "created_pet":
      | HEADER | Accept       | application/json |
      | HEADER | Content-Type | application/json |
      | BODY   | BODY         | json.post.pet    |
    И в ответе "created_pet" содержимое найденное по jsonPath "status" равно "available"

  # =======================================================================
  # СХЕМЫ
  # =======================================================================

  # validateJsonSchema — проверка ответа по JSON-схеме
  Сценарий: Проверка ответа на соответствие JSON-схеме
    И в ответе "json_response" содержимое соответствует json схеме "json/pet_array_schema.json"

  # =======================================================================
  # МАССИВЫ
  # =======================================================================

  # arrayContains — массив содержит значение
  Сценарий: Массив значений по jsonPath содержит указанное значение
    И в ответе "json_response" массив значений найденных по jsonPath "status" содержит значение "available"

  # arrayAllEqual — все элементы массива равны значению
  Сценарий: Все значения массива по jsonPath равны ожидаемому
    И в ответе "json_response" массив значений найденных по jsonPath "status" все значения равны "available"

  # arrayAllContain — все элементы массива содержат подстроку
  Сценарий: Все значения массива по jsonPath содержат подстроку
    И в ответе "json_response" массив значений найденных по jsonPath "status" все значения содержат "avail"

  # arraySize — размер массива
  Сценарий: Проверка размера массива по jsonPath
    И в ответе "json_response" массив значений найденных по jsonPath "findAll { it.id == -1 }" размер 0

  # arraySizeNotNull — непустой массив
  Сценарий: Непустой массив по jsonPath
    И в ответе "json_response" массив значений найденных по jsonPath "status" не пустой

  # Числовые массивы: httpbin возвращает JSON, который отправили в запросе, поэтому данные под контролем теста
  Сценарий: Массив чисел — содержит значение, размер, все значения содержат
    И отправлен HTTP POST на "https://httpbin.org/anything" код ответа 200 ответ сохранен в "numbers_response":
      | HEADER | Content-Type | application/json                  |
      | BODY   | BODY         | {"ids":[11,21,31],"flags":[true]} |
    Тогда в ответе "numbers_response" массив значений найденных по jsonPath "json.ids" содержит значение "21"
    И в ответе "numbers_response" массив значений найденных по jsonPath "json.ids" размер 3
    И в ответе "numbers_response" массив значений найденных по jsonPath "json.ids" все значения содержат "1"
    И в ответе "numbers_response" массив значений найденных по jsonPath "json.flags" все значения равны "true"

  # arraySortedAsc — сортировка по возрастанию. Числа 2, 10, 100 упорядочены как числа (как строки было бы "10" < "100" < "2"),
  # а массив из одинаковых значений (например, status) проходил бы проверку в обе стороны, поэтому данные заданы явно.
  Сценарий: Массив отсортирован по возрастанию
    И отправлен HTTP POST на "https://httpbin.org/anything" код ответа 200 ответ сохранен в "sorted_asc_response":
      | HEADER | Content-Type | application/json                                     |
      | BODY   | BODY         | {"numbers":[2,10,100],"names":["alpha","beta","gamma"]} |
    Тогда в ответе "sorted_asc_response" массив значений найденных по jsonPath "json.numbers" отсортирован по возрастанию
    И в ответе "sorted_asc_response" массив значений найденных по jsonPath "json.names" отсортирован по возрастанию

  # arraySortedDesc — сортировка по убыванию
  Сценарий: Массив отсортирован по убыванию
    И отправлен HTTP POST на "https://httpbin.org/anything" код ответа 200 ответ сохранен в "sorted_desc_response":
      | HEADER | Content-Type | application/json                                     |
      | BODY   | BODY         | {"numbers":[100,10,2],"names":["gamma","beta","alpha"]} |
    Тогда в ответе "sorted_desc_response" массив значений найденных по jsonPath "json.numbers" отсортирован по убыванию
    И в ответе "sorted_desc_response" массив значений найденных по jsonPath "json.names" отсортирован по убыванию

  # arrayDatesInRange — проверка периода дат
  Сценарий: Все даты находятся в указанном периоде
    И отправлен HTTP GET на "https://fakerestapi.azurewebsites.net/api/v1/Activities" код ответа 200 ответ сохранен в "json_dates_response"
    И в ответе "json_dates_response" массив значений найденных по jsonPath "dueDate" в периоде между "0001-01-01T00:00:00+00:00" и "9999-12-31T23:59:59+00:00" в формате "yyyy-MM-dd'T'HH:mm:ssXXX"
