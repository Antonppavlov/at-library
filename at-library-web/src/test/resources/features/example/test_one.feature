# language: ru
@all
@web
Функция: Создание демо аккаунта

  Сценарий: Проверка отображения формы "Ввод SMS" 1
    Когда совершен переход на страницу "BCS demo аккаунт" по ссылке "https://broker.ru/demo"

    И в поле "ФИО" набирается значение "Иванов Иван Иванович"
    И значение поля "ФИО" равно "Иванов Иван Иванович"

    И в поле "Номер телефона" набирается значение
    """
    9123456789
    """

#    И значение элемента "Номер телефона" равно
#    """
#    +7 903 670-42-71
#    """
#
#    И в поле "Email" введено значение "anton.it.pavlov@gmail.com"


    И заголовок страницы равен "Открыть демо-счет на бирже в БКС Брокер, торговля с демонстрационного счета через Quik (Квик)"


  Сценарий: Проверка отображения формы "Ввод SMS" 2
    Когда совершен переход на страницу "BCS demo аккаунт" по ссылке "https://broker.ru/demo"

    И в поле "ФИО" набирается значение "Иванов Иван Иванович"
    И значение поля "ФИО" равно "Иванов Иван Иванович"

    И в поле "Номер телефона" набирается значение
    """
    9123456789
    """

#    И значение элемента "Номер телефона" равно
#    """
#    +7 903 670-42-71
#    """
#
#    И в поле "Email" введено значение "anton.it.pavlov@gmail.com"


    И заголовок страницы равен "Открыть демо-счет на бирже в БКС Брокер, торговля с демонстрационного счета через Quik (Квик)"


  Сценарий: Проверка отображения формы "Ввод SMS" 3
    Когда совершен переход на страницу "BCS demo аккаунт" по ссылке "https://broker.ru/demo"

    И в поле "ФИО" набирается значение "Иванов Иван Иванович"
    И значение поля "ФИО" равно "Иванов Иван Иванович"

    И в поле "Номер телефона" набирается значение
    """
    9123456789
    """


#    И значение элемента "Номер телефона" равно
#    """
#    +7 903 670-42-71
#    """
#
#    И в поле "Email" введено значение "anton.it.pavlov@gmail.com"


    И заголовок страницы равен "Открыть демо-счет на бирже в БКС Брокер, торговля с демонстрационного счета через Quik (Квик)"

  Сценарий: Перезагрузка страницы
    Когда совершен переход на страницу "BCS demo аккаунт" по ссылке "https://broker.ru/demo"
    Тогда выполнено обновление текущей страницы каждые 5 секунд в течении 30 секунд