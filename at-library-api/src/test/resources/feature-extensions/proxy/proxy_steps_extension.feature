# language: ru
@api
@extension
@proxy
Функционал: Расширение покрытия ProxySteps
  API: Swagger Petstore v2
  Swagger: https://petstore.swagger.io/
  Запускать только в окружении с доступным proxy сервером.

  Сценарий: Включение прокси, отправка запроса и отключение прокси
    И используется proxy: "127.0.0.1" port: "8888"
    И через прокси отправлен запрос "https://petstore.swagger.io/v2/store/inventory"
    И выключено использование proxy
