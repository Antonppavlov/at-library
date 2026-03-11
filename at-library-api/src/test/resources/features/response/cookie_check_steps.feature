# language: ru
@unit
@api
#noinspection NonAsciiCharacters
Функционал: Проверка шагов CookieCheckSteps
  Для сценария используется endpoint, который возвращает Set-Cookie в ответе.

  Предыстория: Получение ответа с cookies
    И отправлен HTTP GET на "https://httpbin.org/response-headers?Set-Cookie=session_id=abc123&Set-Cookie=theme=dark" код ответа 200 ответ сохранен в "cookie_response"

  Сценарий: Проверка cookies по таблице
    И в ответе "cookie_response" cookies равны значениям из таблицы:
      | session_id | abc123 |
      | theme      | dark   |

  Сценарий: Сохранение cookies в переменные
    И cookies ответа "cookie_response" сохранены в переменные из таблицы:
      | session_id | saved_session |
      | theme      | saved_theme   |
