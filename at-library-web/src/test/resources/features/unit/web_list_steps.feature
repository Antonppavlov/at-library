# language: ru
@unit
Функционал: WebListSteps
  @logger-test
  Сценарий: список элементов "([^"]*)" отображается на странице
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    Тогда список элементов "Список ссылок" отображается на странице

####################################################################################
  @logger-test
  Сценарий: список элементов "([^"]*)" включает в себя список из таблицы
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    * установлено значение переменной "text_warning" равным "Сообщить об ошибке"
    Тогда список элементов "Список ссылок" включает в себя список из таблицы
      | Случайная статья                     |
      | text_warning                         |
      | עברית                                |
      | Свежие правки                        |
      | Свежие правки                        |
      | text.wikipedia.first.element.in.list |

####################################################################################
  @logger-test
  Сценарий: список элементов "([^"]*)" равен списку из таблицы
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    * установлено значение переменной "text_var" равным "Рубрикация"
    Тогда список элементов "Заглавная страница" равен списку из таблицы
      | text_var            |
      | text.wikipedia.link |
      | Указатель А — Я     |
      | Избранные статьи    |
      | Случайная статья    |
      | Заглавная страница  |

####################################################################################
  @logger-test
  Сценарий: нажатие на элемент с (?:текстом|значением) "(.*)" в списке "([^"]*)" [HARDCODING]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    Тогда нажатие на элемент с текстом "Избранные статьи" в списке "Список ссылок"
    * заголовок страницы равен "Википедия:Избранные статьи — Википедия"
  @logger-test
  Сценарий: нажатие на элемент с (?:текстом|значением) "(.*)" в списке "([^"]*)" [VARIABLE]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    * установлено значение переменной "text_var" равным "Избранные статьи"
    Тогда нажатие на элемент с текстом "text_var" в списке "Список ссылок"
    * заголовок страницы равен "Википедия:Избранные статьи — Википедия"
  @logger-test
  Сценарий: нажатие на элемент с (?:текстом|значением) "(.*)" в списке "([^"]*)" [PROPERTIES]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    Тогда нажатие на элемент с текстом "text.wikipedia.featured.articles" в списке "Список ссылок"
    * заголовок страницы равен "Википедия:Избранные статьи — Википедия"

####################################################################################
  @logger-test
  Сценарий: нажатие на элемент содержащий (?:текст|значение) "(.*)" в списке "([^"]*)" [HARDCODING]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    Тогда нажатие на элемент содержащий текст "ранные статьи" в списке "Список ссылок"
    * заголовок страницы равен "Википедия:Избранные статьи — Википедия"

  Сценарий: нажатие на элемент содержащий (?:текст|значение) "(.*)" в списке "([^"]*)" [VARIABLE]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    * установлено значение переменной "text_var" равным "ранные статьи"
    Тогда нажатие на элемент содержащий текст "text_var" в списке "Список ссылок"
    * заголовок страницы равен "Википедия:Избранные статьи — Википедия"

  Сценарий: нажатие на элемент содержащий (?:текст|значение) "(.*)" в списке "([^"]*)" [PROPERTIES]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    Тогда нажатие на элемент содержащий текст "text.wikipedia.featured.articles.incorrect" в списке "Список ссылок"
    * заголовок страницы равен "Википедия:Избранные статьи — Википедия"

####################################################################################

  Сценарий: список элементов "([^"]*)" не отображается на странице
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    Тогда список элементов "Нет списка" не отображается на странице

####################################################################################

  Сценарий: нажатие на "([^"]*)"-й элемент в списке "([^"]*)"
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    * текущий URL равен "url.wikipedia.home"
    Тогда нажатие на "1" элемент в списке "Список ссылок"
    * текущий URL равен "url.wikipedia.home"
    Тогда нажатие на "2" элемент в списке "Список ссылок"
    * текущий URL равен "url.wikipedia.rubrication"

####################################################################################

  Сценарий: нажатие на случайный элемент в списке "([^"]*)"
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    * текущий URL равен "url.wikipedia.home"
    Тогда нажатие на случайный элемент в списке "Инструменты"
    * текущий URL не равен "url.wikipedia.home"

####################################################################################

  Сценарий: выбран любой элемент из списка "([^"]*)" и его значение сохранено в переменную "([^"]*)"
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    * выбран любой элемент из списка "Список ссылок" и его значение сохранено в переменную "случайная"

####################################################################################

  Сценарий: текст в "(\d+)" элементе списка "([^"]*)" равен тексту "([^"]*)" [HARDCODING] [VARIABLE] [PROPERTIES]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    * установлено значение переменной "text_var" равным "Избранные статьи"
    Тогда текст в "1" элементе списка "Заглавная страница" равен тексту "Заглавная страница"
    Тогда текст в "2" элементе списка "Заглавная страница" равен тексту "Рубрикация"
    Тогда текст в "3" элементе списка "Заглавная страница" равен тексту "Указатель А — Я"
    Тогда текст в "4" элементе списка "Заглавная страница" равен тексту "Избранные статьи"
    Тогда текст в "4" элементе списка "Заглавная страница" равен тексту "text_var"
    Тогда текст в "4" элементе списка "Заглавная страница" равен тексту "text.wikipedia.featured.articles"
    Тогда текст в "5" элементе списка "Заглавная страница" равен тексту "Случайная статья"
    Тогда текст в "6" элементе списка "Заглавная страница" равен тексту "Текущие события"

####################################################################################

  Сценарий: список элементов "([^"]*)" содержит элемент с текстом "([^"]*)" [HARDCODING]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    Тогда список элементов "Заглавная страница" содержит элемент с текстом "Случайная статья"

  Сценарий: список элементов "([^"]*)" содержит элемент с текстом "([^"]*)" [VARIABLE]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    * установлено значение переменной "text_var" равным "Избранные статьи"
    Тогда список элементов "Заглавная страница" содержит элемент с текстом "text_var"

  Сценарий: список элементов "([^"]*)" содержит элемент с текстом "([^"]*)" [PROPERTIES]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    Тогда нажатие на элемент с текстом "text.wikipedia.featured.articles" в списке "Список ссылок"
    Тогда список элементов "Заглавная страница" содержит элемент с текстом "text.wikipedia.featured.articles"

####################################################################################

  Сценарий: список элементов "([^"]*)" не содержит элемент с текстом "([^"]*)" [HARDCODING]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    Тогда список элементов "Заглавная страница" не содержит элемент с текстом "123 312"

  Сценарий: список элементов "([^"]*)" не содержит элемент с текстом "([^"]*)" [VARIABLE]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    * установлено значение переменной "text_var" равным "123 312"
    Тогда список элементов "Заглавная страница" не содержит элемент с текстом "text_var"

  Сценарий: список элементов "([^"]*)" не содержит элемент с текстом "([^"]*)" [PROPERTIES]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    Тогда список элементов "Заглавная страница" не содержит элемент с текстом "title.google.doodles"

####################################################################################

  Сценарий: список элементов "([^"]*)" состоит из "([^"]*)" элементов [HARDCODING]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    Тогда список элементов "Заглавная страница" состоит из "6" элементов

  Сценарий: список элементов "([^"]*)" состоит из "([^"]*)" элементов [VARIABLE]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    * установлено значение переменной "text_var" равным "6"
    Тогда список элементов "Заглавная страница" состоит из "text_var" элементов

  Сценарий: список элементов "([^"]*)" состоит из "([^"]*)" элементов [PROPERTIES]
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    Тогда список элементов "Заглавная страница" состоит из "text.wikipedia.count.link" элементов

####################################################################################

  Сценарий: в списке "([^"]*)" содержится (более|менее) (\d+) (?:элементов|элемента)
    * совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    Тогда в списке "Заглавная страница" содержится более 5 элемента
    Тогда в списке "Заглавная страница" содержится менее 7 элемента

####################################################################################