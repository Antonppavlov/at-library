# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов JsonArrayVerificationSteps (валидация JSON-массивов в ответах)
  Для примеров используется публичное API Petstore (https://petstore.swagger.io) и список доступных питомцев.

  Предыстория: Получение списка доступных питомцев
    * отправлен HTTP GET запрос на URL "url.pet.findByStatus" с параметрами запроса и ответ сохранён в переменную "pets_available_jav"
      | PARAMETER | status | available |

  # checkArrayHasItem
  Сценарий: Массив значений по jsonPath содержит указанное значение
    * в ответе "pets_available_jav" по ключу: "status" массив содержит "available"

  # checkArrayEqualAllItem
  Сценарий: Все значения массива по jsonPath равны ожидаемому значению
    * в ответе "pets_available_jav" по ключу: "status" весь массив соотвествует "available"

  # checkArrayContainsAllItem
  Сценарий: Все значения массива по jsonPath содержат ожидаемое значение
    * в ответе "pets_available_jav" по ключу: "status" весь массив частично соотвествует "avail"

  # checkArraySize
  Сценарий: Размер массива по jsonPath равен ожидаемому
    * в ответе "pets_available_jav" по ключу: "[0].tags" размер массива "0"

  # checkSortElementOrder
  Сценарий: Значения массива по jsonPath отсортированы по возрастанию
    * значения из json ответа "pets_available_jav", найденные по jsonpath из таблицы, сохранены в переменные
      | status | first_pet_status_jav_array |
    * в ответе "pets_available_jav" по ключу: "status" элементы отсортированы по возрастанию

  # checkSortDescElementOrder
  Сценарий: Значения массива по jsonPath отсортированы по убыванию
    * в ответе "pets_available_jav" по ключу: "status" элементы отсортированы по убыванию

  # checkArrayContainsDataBetweenDatesAllItem
  Сценарий: Все значения массива дат находятся в заданном интервале
    * установлено значение переменной "date_start_jav" равным "2024-01-01T00:00:00+00:00"
    * установлено значение переменной "date_end_jav" равным "2026-12-31T23:59:59+00:00"
    * установлено значение переменной "date_format_jav" равным "yyyy-MM-dd'T'HH:mm:ssXXX"
    * в ответе "pets_available_jav" по ключу: "status" весь массив соответствуют периоду между "date_start_jav" и "date_end_jav" в формате "date_format_jav"