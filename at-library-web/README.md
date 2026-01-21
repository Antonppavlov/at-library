at-library-web
=========================

Настройка проекта
====================
Подключите зависимость
```xml
<dependency>
      <groupId>ru</groupId>
      <artifactId>at-library-web</artifactId>
      <version>21.02.2026</version>
</dependency>
```

Примеры шагов
=======================
```gherkin
# language: ru
@web
Функция: Нажатие на элемент

  Сценарий: Нажатие на элемент с текстом в списке "Список ссылок" [VARIABLE]
    Когда совершен переход на страницу "Википедия" по ссылке "url.wikipedia"
    И установлено значение переменной "text_var" равным "Избранные статьи"
    Тогда в списке элементов "Список ссылок" выполнено нажатие на элемент с текстом "text_var"
    И заголовок страницы равен "Википедия:Избранные статьи — Википедия"
```

Работа с страницами
====================
Для работы с элементами страницы ее необходимо задать как текущую.
Таким образом можно получить доступ к методам взаимодействия с элементами, описанным в CorePage.

Новую текущую страницу можно установить шагом
```gherkin
Когда страница "<Имя страницы>" загрузилась
```


- Каждая страница, с которой предполагается взаимодействие, должна быть описана в классе наследующемся от CorePage.
- Для страницы и ее элементов следует задать имя на русском, через аннотацию Name, чтобы искать можно было именно по русскому описанию.
- Элементы страницы ищутся по локаторам, указанным в аннотации FindBy и должны иметь тип SelenideElement или List<SelenideElement>.

Пример описания страницы:
```java
    @Name("Google")
    public class GooglePage extends CorePage {
    
        @Name("Поиск")
        @FindBy(css = "[title=\"Поиск\"]")
        public SelenideElement searchInput;
    
        @Name("Кнопка Почта")
        @FindBy(css = "header [aria-label*=\"Почта\"]")
        public SelenideElement menuBtn;
    
        @Name("Google Header")
        @FindBy(css = "header")
        public GoogleHeader googleHeader;
    }
```

Инициализация страницы
=================================
Если непобходимо создавать собсвенные шаги по работе с web элементами'

```java

```

- Для страницы инициализируется карта ее элементов - это те поля, что помечены аннотацией Name.
- Кроме того, осуществляется проверка, что загружена требуемая страница.
- Страница считается загруженной корректно, если за отведенное по умолчанию время были загружены основные ее элементы. (по умолчанию проверка загрузки элментов отключена) Вклчается параметром:
```mvn
-Dappeared=true
```
- Основными элементами являются поля класса страницы с аннотацией Name, но без аннотации Optional.
- Аннотация Optional указывает на то, что элемент является не обязательным для принятия решения о загрузке страницы.
- Например, если на странице есть список, который раскрывается после нажатия не него, т.е. видим не сразу после загрузки страницы, его можно пометить как Optional.
- Реализована возможность управления временем ожидания появления элемента на странице.
- Чтобы установить timeout, отличный от базового, нужно добавить в properties строку: waitingAppearTimeout=150000

Доступ к элементам страницы
============================
Данные строки позволяют по имени элемента найти его в карте элементов текущей страницы.

```java

 ```


Блоки на странице
============================
Реализована возможность описывать блоки на странице (Page Element)

Например:
```java
@FindBy(className = "header")
@Name("Шапка страницы")
public HeaderBlock header;
```
При загрузке страницы будут учитываться элементы, описанные в блоке


После подключения всех плагинов и зависимостей вы можете запускать проект автотестов командами:
=========================
- Запуск локально на ubuntu
```mvn
clean 
test 
-Dselenide.browser=chrome  
-Djava.net.useSystemProxies=true 
allure:serve
```

- Запуск локально на windows
```mvn
clean 
test 
-Dselenide.browser="internet explorer" 
-Dwebdriver.ie.driver="C:\\Program Files\\Selenium\\Drivers\\IEDriver\\IEDriverServer.exe" 
allure:serve
```
- Имена ключей для прописывания path к разным браузерам:
```
"webdriver.chrome.driver"
"webdriver.edge.driver"
"webdriver.ie.driver"
"webdriver.opera.driver"
"phantomjs.binary.path"
"webdriver.gecko.driver"
``` 

- Запуск удаленно на Selenoid chrome
```mvn
clean 
test 
-Dselenide.browser="chrome" 
-Dselenide.remote=http://localhost:4444/wd/hub/ 
-Dproxy=172.18.62.68:8080 
allure:serve
```
- Запуск удаленно на Selenoid "internet explorer"
```mvn
clean 
test 
-Dselenide.browser="internet explorer" 
-Dselenide.remote=http://localhost:4444/wd/hub/ 
-Dproxy=172.18.62.68:8080 
allure:serve
```
- Запуск тестов с тегами (И)
```mvn
clean 
test 
-Dcucumber.options="--tags @api --tags @web --plugin io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm --plugin com.epam.reportportal.cucumber.ScenarioReporter"
allure:serve 
```
- Запуск тестов с тегами (ИЛИ)
```mvn
clean
test
-Dcucumber.options="--tags @api,@web --plugin io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm --plugin com.epam.reportportal.cucumber.ScenarioReporter"
allure:serve 
```

Пояснение к командам:
=========================

```mvn
clean - очистка проекта
```

```mvn
test - запуск тестов
```

```mvn
allure:serve - запуск allure отчетов
```

```mvn
-Dselenide.browser=chrome - использовать браузер chrome для прогона тестов
```

```mvn
-Djava.net.useSystemProxies=true - установив для этого свойства значение true, использовать настройки прокси-сервера системы
```
```mvn
-Dselenide.remote=http://localhost:4444/wd/hub/ -Dproxy=172.18.62.68:8080 - для запуска тестов на selenoid
```
- Чтобы установить базовый url(для api и ui тестов) его можно указать в properties по ключу baseURI=https://ef.tusvc.ru
или передать параметром (если передан параметр и присутсвует в properties то будет использован тот что передан параметром)

```mvn
-DbaseURI=https://url.you.need
```