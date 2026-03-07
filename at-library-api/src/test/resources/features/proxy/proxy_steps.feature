# language: ru
@unit
@api
@proxy
#noinspection NonAsciiCharacters
Функционал: Проверка шагов ProxySteps
  Запускать только в окружении с доступным proxy сервером.

  Сценарий: Включение и отключение proxy и отправка запроса через него
    И используется proxy: "127.0.0.1" port: "8888"
    И через прокси отправлен запрос "https://petstore.swagger.io/v2/pet/findByStatus?status=available"
    И выключено использование proxy
