at-library-web
=========================

Настройка проекта
====================
Подключите зависимость
```xml
<dependency>
      <groupId>ru</groupId>
      <artifactId>at-library-web</artifactId>
      <version>17.02.2025</version>
</dependency>
```

Примеры шагов
=======================
```gherkin
# language: ru
@web
Функция: Создание демо аккаунта

  Сценарий: Проверка валидации поля Email
    Когда совершен переход на страницу "BCS demo аккаунт" по ссылке "https://broker.ru/demo"
    И в поле "ФИО" введено значение "Иванов Иван Иванович"
    И в поле "Номер телефона" введено значение "9123456789"
    И в поле "Email" введено значение "555.mail.ru"
    И выполнено нажатие на кнопку "Открыть счет"
    Тогда форма "Окно ввода SMS-кода" скрыта
```

Работа с страницами
====================
Для работы с элементами страницы ее необходимо задать как текущую.
Таким образом можно получить доступ к методам взаимодействия с элементами, описанным в CorePage.

Новую текущую страницу можно установить шагом
```gherkin
Когда страница "<Имя страницы>" загрузилась
```

Для страницы BCS demo аккаунт шаг может выглядеть так
```gherkin
Когда страница "BCS demo аккаунт" загрузилась
```

- Каждая страница, с которой предполагается взаимодействие, должна быть описана в классе наследующемся от CorePage.
- Для страницы и ее элементов следует задать имя на русском, через аннотацию Name, чтобы искать можно было именно по русскому описанию.
- Элементы страницы ищутся по локаторам, указанным в аннотации FindBy и должны иметь тип SelenideElement или List<SelenideElement>.

Пример описания страницы:
```java
   @Name("BCS demo аккаунт")
   public class BrokerDemoPage extends CorePage {
   
       private static final String demoAccountForm = "[class='become-demo__form form js-demo-quik-form'] ";
   
       @Name("ФИО")
       @FindBy(css = demoAccountForm + "[name=\"name\"]")
       private SelenideElement inputFIO;
   
       @Name("Номер телефона")
       @FindBy(css = demoAccountForm + "[name=\"phone\"]")
       private SelenideElement inputPhone;
   
       @Name("Email")
       @FindBy(css = demoAccountForm + "[name=\"email\"]")
       private SelenideElement inputEmail;
   
       @Name("Открыть счет")
       @FindBy(css = demoAccountForm + "[class='become-demo__form-submit'] button")
       private SelenideElement buttonOpenScore;
   }
```

Инициализация страницы
=================================
Если непобходимо создавать собсвенные шаги по работе с web элементами'

Пример инициализации страницы "BCS demo аккаунт":
```java
public class BrokerDemoPageSteps {
    
    private CoreScenario coreScenario = CoreScenario.getInstance();

    @И("создание пользователя с ФИО: \"([^\"]*)\" телефон: \"([^\"]*)\" email: \"([^\"]*)\"")
    public void loginSystem(String fio, String password, String email) {
        fio = getPropertyOrStringVariableOrValue(fio);
        password = getPropertyOrStringVariableOrValue(password);
        email = getPropertyOrStringVariableOrValue(email);

        BrokerDemoPage brokerDemoPage =
                (BrokerDemoPage) coreScenario.getPage("BCS demo аккаунт");

        brokerDemoPage.getElement("ФИО").sendKeys(fio);
        brokerDemoPage.getElement("Номер телефона").sendKeys(password);
        brokerDemoPage.getElement("Email").sendKeys(email);
        
        brokerDemoPage.getElement("Открыть счет").click();
    }
}
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
brokerDemoPage.getElement("ФИО").sendKeys(fio);
brokerDemoPage.getElement("Номер телефона").sendKeys(password);
brokerDemoPage.getElement("Email").sendKeys(email);

brokerDemoPage.getElement("Открыть счет").click();
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
- Имена ключей для прописавание path к разным браузерам:
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
-Dcucumber.options="--tags @api --tags @web --plugin io.qameta.allure.cucumber4jvm.AllureCucumber4Jvm --plugin com.epam.reportportal.cucumber.ScenarioReporter"
allure:serve 
```
- Запуск тестов с тегами (ИЛИ)
```mvn
clean
test
-Dcucumber.options="--tags @api,@web --plugin io.qameta.allure.cucumber4jvm.AllureCucumber4Jvm --plugin com.epam.reportportal.cucumber.ScenarioReporter"
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
или передать параметром (если передан параметр и присутсивует в properties то будет использован тот что передан параметром)

```mvn
-DbaseURI=https://url.you.need
```