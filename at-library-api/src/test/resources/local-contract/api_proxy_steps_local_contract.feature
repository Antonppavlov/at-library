# language: ru
@local-api-proxy
Функционал: Локальный контракт proxy-шагов

  Сценарий: Включение, использование и выключение proxy
    Дано запущен локальный HTTP proxy с адресом в "local_proxy_host" и портом в "local_proxy_port"
    И используется proxy: "local_proxy_host" port: "local_proxy_port"
    И через прокси отправлен запрос "http://local-proxy.test/probe"
    И выключено использование proxy
