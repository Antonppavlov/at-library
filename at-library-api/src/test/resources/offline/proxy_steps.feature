# language: ru
@offline
@local-api-proxy
Функционал: Оффлайн-контракт шагов ProxySteps
  Роль proxy играет локальный HTTP-сервер: недоступный хост local-proxy.test отвечает только потому,
  что запрос действительно ушёл через включённый proxy.

  Сценарий: Включение, использование и выключение proxy
    Дано запущен локальный HTTP proxy с адресом в "local_proxy_host" и портом в "local_proxy_port"
    Когда используется proxy: "local_proxy_host" port: "local_proxy_port"
    Тогда системное свойство "http.proxyHost" равно "local_proxy_host"
    И системное свойство "http.proxyPort" равно "local_proxy_port"
    И системное свойство "https.proxyHost" равно "local_proxy_host"
    И системное свойство "https.proxyPort" равно "local_proxy_port"
    И RestAssured использует proxy с адресом "local_proxy_host" и портом "local_proxy_port"
    Когда через прокси отправлен запрос "http://local-proxy.test/probe"
    И выключено использование proxy
    Тогда системное свойство "http.proxyHost" не задано
    И системное свойство "http.proxyPort" не задано
    И системное свойство "https.proxyHost" не задано
    И системное свойство "https.proxyPort" не задано
    И RestAssured не использует proxy

  Сценарий: Пустой ответ через proxy считается ошибкой
    Дано запущен локальный HTTP proxy с пустым ответом, адрес в "local_proxy_host" и порт в "local_proxy_port"
    И используется proxy: "local_proxy_host" port: "local_proxy_port"
    Тогда выполнение шага завершается ошибкой, содержащей "Expected: not """:
      """
      через прокси отправлен запрос "http://local-proxy.test/probe"
      """

  Сценарий: Адрес и порт proxy можно передать литералами
    Дано запущен локальный HTTP proxy с адресом в "local_proxy_host" и портом в "local_proxy_port"
    Когда используется proxy: "127.0.0.1" port: "8888"
    Тогда системное свойство "http.proxyHost" равно "127.0.0.1"
    И системное свойство "http.proxyPort" равно "8888"
    И RestAssured использует proxy с адресом "127.0.0.1" и портом "8888"
    Когда выключено использование proxy
    Тогда RestAssured не использует proxy
