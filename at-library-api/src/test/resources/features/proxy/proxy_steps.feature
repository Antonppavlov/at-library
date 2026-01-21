# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов работы с proxy (ProxySteps)
  Для примеров используется публичное API Petstore (https://petstore.swagger.io).

  # turnOnProxy + findRequestOnProxy + turnOffProxy
  Сценарий: Включение и отключение proxy и отправка запроса через него
    * используется proxy: "localhost" port: "8888"
    * через прокси отправлен запрос "https://petstore.swagger.io/v2/pet/findByStatus?status=available"
    * выключено использование proxy
