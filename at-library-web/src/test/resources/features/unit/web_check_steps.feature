# language: ru
@unit
@web
@web-check-steps
Функционал: WebCheckSteps

########################################################################################################################
################################################## Проверки страниц ####################################################
########################################################################################################################

  Сценарий: (?:страница|блок|форма|вкладка) "([^"]*)" отображается на странице
    Когда совершен переход на страницу "Главная" по ссылке "https://www.google.com/"
    И выполнено нажатие на кнопку "Кнопка Меню"
    То блок "Google Меню" отображается на странице

  Сценарий: (?:страница|блок|форма|вкладка) "([^"]*)" не отображается на странице
    Когда совершен переход на страницу "Главная" по ссылке "https://www.google.com/"
    То блок "Google Меню" не отображается на странице

########################################################################################################################
################################################# Проверки элементов ###################################################
########################################################################################################################

########################################### Проверки отображения элементов #############################################

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" отображается на странице
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    И нажатие на элемент с текстом "Сведения о странице" в списке "Список ссылок"
    То страница "Сведения о странице" загрузилась
    И элемент "Заголовок" отображается на странице
    И элемент "Заявление о куки" отображается на странице

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" отобразится на странице в течение (\d+) (?:секунд|секунды)
    Когда совершен переход на страницу "W3 Delay example" по ссылке "url.w3.delay"
    И выполнено переключение на фрейм с именем "iframeResult"
    И блок "Delay example frame" загрузился
    Тогда выполнено нажатие на кнопку "Show squares"
    То элемент "Red square" отобразится на странице в течение 3 секунд
    И элемент "Purple square" отобразится на странице в течение 5 секунд

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" не отображается на странице
    Когда совершен переход на страницу "W3 Delay example" по ссылке "url.w3.delay"
    И выполнено переключение на фрейм с именем "iframeResult"
    И блок "Delay example frame" загрузился
    То элемент "Red square" не отображается на странице
    И элемент "Purple square" не отображается на странице

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" не отобразится на странице в течение (\d+) (?:секунд|секунды)
    Когда совершен переход на страницу "W3 Delay example" по ссылке "url.w3.delay"
    И выполнено переключение на фрейм с именем "iframeResult"
    И блок "Delay example frame" загрузился
    Тогда выполнено нажатие на кнопку "Show squares"
    То элемент "Red square" не отобразится на странице в течение 1 секунд
    И элемент "Purple square" не отобразится на странице в течение 1 секунд

  Сценарий: элемент "([^"]*)" расположен в видимой части страницы
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    И страница прокручена до элемента "Платформа сайта"
    То элемент "Платформа сайта" расположен в видимой части страницы

  Сценарий: элемент содержащий текст "([^"]*)" расположен в видимой части страницы
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    И страница прокручена до появления элемента с текстом "to.scroll"
    То элемент содержащий текст "to.scroll" расположен в видимой части страницы

########################################## Проверки доступности для нажатия ############################################

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" (?:доступна|доступен) для нажатия
    Когда совершен переход на страницу "Главная" по ссылке "https://www.google.com/"
    То кнопка "Кнопка Меню" доступна для нажатия

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" (?:доступна|доступен) для нажатия в течение (\d+) (?:секунд|секунды)
    Когда совершен переход на страницу "Adding a Time Enabled Button" по ссылке "itgeared.timeout"
    И выполнено нажатие на элемент "View Example"
    Тогда выполнено переключение на вкладку с заголовком "Countdown Timer"
    И блок "Countdown Timer block" загрузился
    То кнопка "Disabled button" доступна для нажатия в течение 16 секунд

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" (?:недоступна|недоступен) для нажатия
    Когда совершен переход на страницу "Adding a Time Enabled Button" по ссылке "itgeared.timeout"
    И выполнено нажатие на элемент "View Example"
    Тогда выполнено переключение на вкладку с заголовком "Countdown Timer"
    И блок "Countdown Timer block" загрузился
    То кнопка "Disabled button" недоступна для нажатия

########################################### Проверки содержимого элементов #############################################

  Сценарий: поле "([^"]*)" пусто
    Когда совершен переход на страницу "Главная" по ссылке "https://www.google.com/"
    То поле "Поиск" пусто

  Сценарий: значение (?:кнопки|ссылки|поля|чекбокса|радиокнопки|текста|элемента) "([^"]*)" сохранено в переменную "([^"]*)"
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    И значение ссылки "Заявление о куки" сохранено в переменную "linkText"
    То значение переменной "linkText" равно "Заявление о куки"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" содержит атрибут "([^"]*)" со значением "([^"]*)" [HARDCODING]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    То элемент "Заявление о куки" содержит атрибут "href" со значением "https://foundation.wikimedia.org/wiki/Cookie_statement"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" содержит атрибут "([^"]*)" со значением "([^"]*)" [VARIABLE]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    И установлено значение переменной "hrefContent" равным "https://foundation.wikimedia.org/wiki/Cookie_statement"
    То элемент "Заявление о куки" содержит атрибут "href" со значением "hrefContent"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" содержит атрибут "([^"]*)" со значением "([^"]*)" [PROPERTIES]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    То элемент "Заявление о куки" содержит атрибут "href" со значением "wiki.cookies-link.href.content"
  
  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" содержит css "([^"]*)" со значением "([^"]*)" [HARDCODING]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    То элемент "Заявление о куки" содержит css "cursor" со значением "pointer"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" содержит css "([^"]*)" со значением "([^"]*)" [VARIABLE]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    И установлено значение переменной "cssName" равным "cursor"
    И установлено значение переменной "cssValue" равным "pointer"
    То элемент "Заявление о куки" содержит css "cssName" со значением "cssValue"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" содержит css "([^"]*)" со значением "([^"]*)" [PROPERTIES]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    То элемент "Заявление о куки" содержит css "wiki.cookies-link.css.name" со значением "wiki.cookies-link.css.value"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" содержит текст "([^"]*)" [HARDCODING]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    И выполнено нажатие на элемент "Сведения о странице"
    И страница "Сведения о странице" загрузилась
    То элемент "Заголовок" содержит текст "Заглавная страница"
    И элемент "Заголовок" содержит текст "Сведения"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" содержит текст "([^"]*)" [VARIABLE]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    И выполнено нажатие на элемент "Сведения о странице"
    И страница "Сведения о странице" загрузилась
    И установлено значение переменной "expectedText1" равным "Заглавная страница"
    И установлено значение переменной "expectedText2" равным "Сведения"
    То элемент "Заголовок" содержит текст "expectedText1"
    И элемент "Заголовок" содержит текст "expectedText2"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" содержит текст "([^"]*)" [PROPERTIES]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    И выполнено нажатие на элемент "Сведения о странице"
    И страница "Сведения о странице" загрузилась
    То элемент "Заголовок" содержит текст "wiki.info.header.expected-text"
    И элемент "Заголовок" содержит текст "wiki.info.header.expected-text2"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" не содержит текст "([^"]*)" [HARDCODING]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    То элемент "Заголовок" не содержит текст "Заглавная страница"
    И элемент "Заголовок" не содержит текст "Сведения"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" не содержит текст "([^"]*)" [VARIABLE]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    И установлено значение переменной "expectedText1" равным "Заглавная страница"
    И установлено значение переменной "expectedText2" равным "Сведения"
    То элемент "Заголовок" не содержит текст "expectedText1"
    И элемент "Заголовок" не содержит текст "expectedText2"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" не содержит текст "([^"]*)" [PROPERTIES]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    То элемент "Заголовок" не содержит текст "wiki.info.header.expected-text"
    И элемент "Заголовок" не содержит текст "wiki.info.header.expected-text2"

  Сценарий: текст (?:кнопки|ссылки|поля|чекбокса|радиокнопки|текста|элемента) "([^"]*)" равен "([^"]*)" [HARDCODING]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    То текст элемента "Заявление о куки" равен "Заявление о куки"

  Сценарий: текст (?:кнопки|ссылки|поля|чекбокса|радиокнопки|текста|элемента) "([^"]*)" равен "([^"]*)" [VARIABLE]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    И установлено значение переменной "expectedText" равным "Заявление о куки"
    То текст элемента "Заявление о куки" равен "expectedText"

  Сценарий: текст (?:кнопки|ссылки|поля|чекбокса|радиокнопки|текста|элемента) "([^"]*)" равен "([^"]*)" [PROPERTIES]
    Когда совершен переход на страницу "Википедия" по ссылке "https://ru.wikipedia.org/"
    То текст элемента "Заявление о куки" равен "to.scroll"

  Сценарий: в (?:кнопке|ссылке|поле|чекбоксе|радиокнопке|тексте|элементе) "([^"]*)" содержится (\d+) символов
    Когда совершен переход на страницу "Главная" по ссылке "url.google"
    И в поле "Поиск" введено "7" случайных символов на кириллице
    То в поле "Поиск" содержится 7 символов

########################################### Проверки радиокнопок/чекбоксов #############################################

  Сценарий: радиокнопка "([^"]*)" выбрана
    Когда совершен переход на страницу "W3 Radio example" по ссылке "url.w3.radio"
    И выполнено переключение на фрейм с именем "iframeResult"
    И блок "RadioButton example frame" загрузился
    И выполнено нажатие на радиокнопу "Male radio button"
    То радиокнопка "Male radio button" выбрана

  Сценарий: радиокнопка "([^"]*)" не выбрана
    Когда совершен переход на страницу "W3 Radio example" по ссылке "url.w3.radio"
    И выполнено переключение на фрейм с именем "iframeResult"
    И блок "RadioButton example frame" загрузился
    То радиокнопка "Male radio button" не выбрана

  Сценарий: чекбокс "([^"]*)" выбран
    Когда совершен переход на страницу "W3 Checkbox example" по ссылке "url.w3.checkbox"
    И выполнено переключение на фрейм с именем "iframeResult"
    И блок "Checkbox example frame" загрузился
    И выполнено нажатие на чекбокс "Bike checkbox"
    То чекбокс "Bike checkbox" выбран

  Сценарий: чекбокс "([^"]*)" не выбран
    Когда совершен переход на страницу "W3 Checkbox example" по ссылке "url.w3.checkbox"
    И выполнено переключение на фрейм с именем "iframeResult"
    И блок "Checkbox example frame" загрузился
    То чекбокс "Bike checkbox" не выбран

########################################################################################################################
################################################## Проверки в блоке ####################################################
########################################################################################################################

####################################### Проверки отображения элементов в блоке #########################################

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" отображается на странице
    Когда совершен переход на страницу "W3 Delay example" по ссылке "url.w3.delay"
    И выполнено переключение на фрейм с именем "iframeResult"
    То кнопка "Show squares" в блоке "Delay example frame" отображается на странице

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" отображается на странице в течение (\d+) (?:секунд|секунды)
    Когда совершен переход на страницу "W3 Delay example" по ссылке "url.w3.delay"
    И выполнено переключение на фрейм с именем "iframeResult"
    Тогда выполнено нажатие на элемент "Show squares" в блоке "Delay example frame"
    То элемент "Red square" в блоке "Delay example frame" отображается на странице в течение 3 секунд

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" не отображается на странице
    Когда совершен переход на страницу "W3 Delay example" по ссылке "url.w3.delay"
    И выполнено переключение на фрейм с именем "iframeResult"
    То элемент "Red square" в блоке "Delay example frame" не отображается на странице

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" не отображается на странице в течение (\d+) (?:секунд|секунды)
    Когда совершен переход на страницу "W3 Delay example" по ссылке "url.w3.delay"
    И выполнено переключение на фрейм с именем "iframeResult"
    Тогда выполнено нажатие на элемент "Show squares" в блоке "Delay example frame"
    То элемент "Purple square" в блоке "Delay example frame" не отображается на странице в течение 3 секунд

  Сценарий: ожидается исчезновение (?:кнопки|ссылки|поля|чекбокса|радиокнопки|текста|элемента) "([^"]*)" в блоке "([^"]*)"
    Когда совершен переход на страницу "W3 FadeOut example" по ссылке "url.w3.fadeout"
    И выполнено переключение на фрейм с именем "iframeResult"
    Тогда выполнено нажатие на элемент "Click to fade out" в блоке "FadeOut example frame"
    То ожидается исчезновение элемента "Blue box" в блоке "FadeOut example frame"

  Сценарий: ожидается исчезновение (?:кнопки|ссылки|поля|чекбокса|радиокнопки|текста|элемента) "([^"]*)" в блоке "([^"]*)" в течение (\d+) (?:секунд|секунды)
    Когда совершен переход на страницу "W3 FadeOut example" по ссылке "url.w3.fadeout"
    И выполнено переключение на фрейм с именем "iframeResult"
    Тогда выполнено нажатие на элемент "Click to fade out" в блоке "FadeOut example frame"
    То ожидается исчезновение элемента "Blue box" в блоке "FadeOut example frame" в течение 5 секунд

###################################### Проверки доступности для нажатия в блоке ########################################

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" (?:доступна|доступно|доступен) для нажатия
    Когда совершен переход на страницу "W3 FadeOut example" по ссылке "url.w3.fadeout"
    И выполнено переключение на фрейм с именем "iframeResult"
    То кнопка "Click to fade out" в блоке "FadeOut example frame" доступна для нажатия

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" (?:доступна|доступно|доступен) для нажатия в течение (\d+) (?:секунд|секунды)
    Когда совершен переход на страницу "Adding a Time Enabled Button" по ссылке "itgeared.timeout"
    И выполнено нажатие на элемент "View Example"
    Тогда выполнено переключение на вкладку с заголовком "Countdown Timer"
    И страница "Countdown Timer" загрузилась
    То кнопка "Disabled button" в блоке "Countdown Timer block" доступна для нажатия в течение 16 секунд

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" (?:недоступна|недоступно|недоступен) для (?:нажатия|редактирования)
    Когда совершен переход на страницу "Adding a Time Enabled Button" по ссылке "itgeared.timeout"
    И выполнено нажатие на элемент "View Example"
    Тогда выполнено переключение на вкладку с заголовком "Countdown Timer"
    И страница "Countdown Timer" загрузилась
    То кнопка "Disabled button" в блоке "Countdown Timer block" недоступна для нажатия

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" (?:недоступна|недоступно|недоступен) для (?:нажатия|редактирования) в течение (\d+) (?:секунд|секунды)
    Когда совершен переход на страницу "Adding a Time Enabled Button" по ссылке "itgeared.timeout"
    И выполнено нажатие на элемент "View Example"
    Тогда выполнено переключение на вкладку с заголовком "Countdown Timer"
    И страница "Countdown Timer" загрузилась
    То кнопка "Disabled button" в блоке "Countdown Timer block" недоступна для нажатия в течение 5 секунд

####################################### Проверки содержимого элементов в блоке #########################################

  Сценарий: поле "([^"]*)" в блоке "([^"]*)" пусто
    Когда совершен переход на страницу "W3 Input example" по ссылке "url.w3.input"
    И выполнено переключение на фрейм с именем "iframeResult"
    То поле "First name input" в блоке "Input example frame" пусто

  Сценарий: значение (?:кнопки|ссылки|поля|чекбокса|радиокнопки|текста|элемента) "([^"]*)" в блоке "([^"]*)" сохранено в переменную "([^"]*)"
    Когда совершен переход на страницу "W3 Input example" по ссылке "url.w3.input"
    И выполнено переключение на фрейм с именем "iframeResult"
    И значение текста "Header" в блоке "Input example frame" сохранено в переменную "headerText"
    То значение переменной "headerText" равно "The input element"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" содержит атрибут "([^"]*)" со значением "([^"]*)" [HARDCODING]
    Когда совершен переход на страницу "W3 Input example" по ссылке "url.w3.input"
    И выполнено переключение на фрейм с именем "iframeResult"
    То элемент "First name input" в блоке "Input example frame" содержит атрибут "name" со значением "fname"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" содержит атрибут "([^"]*)" со значением "([^"]*)" [VARIABLE]
    Когда совершен переход на страницу "W3 Input example" по ссылке "url.w3.input"
    И установлено значение переменной "nameContent" равным "fname"
    И выполнено переключение на фрейм с именем "iframeResult"
    То элемент "First name input" в блоке "Input example frame" содержит атрибут "name" со значением "nameContent"

  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" содержит атрибут "([^"]*)" со значением "([^"]*)" [PROPERTIES]
    Когда совершен переход на страницу "W3 Input example" по ссылке "url.w3.input"
    И выполнено переключение на фрейм с именем "iframeResult"
    То элемент "First name input" в блоке "Input example frame" содержит атрибут "name" со значением "w3.input.name.content"

    #    TODO

#  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) \"([^\"]*)\" в блоке \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$") [HARDCODING]
#
#  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) \"([^\"]*)\" в блоке \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$") [VARIABLE]
#
#  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) \"([^\"]*)\" в блоке \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$") [PROPERTIES]
#
#  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" содержит текст "([^"]*)" [HARDCODING]
#
#  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" содержит текст "([^"]*)" [VARIABLE]
#
#  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" содержит текст "([^"]*)" [PROPERTIES]
#
#  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" не содержит текст "([^"]*)" [HARDCODING]
#
#  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" не содержит текст "([^"]*)" [VARIABLE]
#
#  Сценарий: (?:кнопка|ссылка|поле|чекбокс|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" не содержит текст "([^"]*)" [PROPERTIES]
#
#  Сценарий: текст (?:кнопки|ссылки|поля|чекбокса|радиокнопки|текста|элемента) "([^"]*)" в блоке "([^"]*)" равен "([^"]*)" [HARDCODING]
#
#  Сценарий: текст (?:кнопки|ссылки|поля|чекбокса|радиокнопки|текста|элемента) "([^"]*)" в блоке "([^"]*)" равен "([^"]*)" [VARIABLE]
#
#  Сценарий: текст (?:кнопки|ссылки|поля|чекбокса|радиокнопки|текста|элемента) "([^"]*)" в блоке "([^"]*)" равен "([^"]*)" [PROPERTIES]
#
#  Сценарий: текст (?:кнопки|ссылки|поля|чекбокса|радиокнопки|текста|элемента) "([^"]*)" в блоке "([^"]*)" содержит (\d+) символовтекст (?:кнопки|ссылки|поля|чекбокса|радиокнопки|текста|элемента) "([^"]*)" в блоке "([^"]*)" содержит (\d+) символов
#
######################################## Проверки радиокнопок/чекбоксов в блоке #########################################
#
#  Сценарий: (?:кнопка|ссылка|поле|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" (?:выбрана|выбрано|выбран)
#
#  Сценарий: (?:кнопка|ссылка|поле|радиокнопка|текст|элемент) "([^"]*)" в блоке "([^"]*)" не (?:выбрана|выбрано|выбран)
#
#  Сценарий: чекбокс "([^"]*)" в блоке "([^"]*)" выбран
#
#  Сценарий: чекбокс "([^"]*)" в блоке "([^"]*)" не выбран