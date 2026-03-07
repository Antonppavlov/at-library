# language: ru
@api
@extension
Функционал: Расширение покрытия JsonResponseSteps на публичном API
  API: Fake REST API
  Swagger: https://fakerestapi.azurewebsites.net/index.html

  Сценарий: Проверка диапазона дат в JSON массиве
    И отправлен HTTP GET на "https://fakerestapi.azurewebsites.net/api/v1/Activities" код ответа 200 ответ сохранен в "json_dates_response"
    И в ответе "json_dates_response" массив значений найденных по jsonPath "dueDate" в периоде между "0001-01-01T00:00:00+00:00" и "9999-12-31T23:59:59+00:00" в формате "yyyy-MM-dd'T'HH:mm:ssXXX"
