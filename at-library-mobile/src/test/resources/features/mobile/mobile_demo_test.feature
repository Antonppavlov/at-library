# language: ru
@mobile
Функция: Тестирование мобильного приложения

  Сценарий: Проверка общей суммы всех девайсов 1
    Когда экран "Главный" загрузился
    И выполнено нажатие на кнопку "iphone"
    И выполнено нажатие на кнопку "mouse"
    И выполнено нажатие на кнопку "ps4"
    И выполнено нажатие на кнопку "photo"
    И выполнено нажатие на кнопку "keyboard"
    Тогда значение поля "money" равно "117300"

  Сценарий: Проверка общей суммы всех девайсов 2
    Когда экран "Главный" загрузился
    То выбор всех товаров и проверка обшей суммы: "155300"

  Сценарий: Проверка перехода в корзину
    Когда экран "Главный" загрузился
    И выполнено нажатие на кнопку "iphone"
    Тогда значение поля "money" равно "60000"
    И выполнено нажатие на кнопку "В корзину"
    Когда экран "Корзина" загрузился
    И значение поля "money" равно "60000"

  Сценарий: Проверка покупки
    Когда экран "Главный" загрузился
    И выполнено нажатие на кнопку "mouse"
    Тогда значение поля "money" равно "300"
    И выполнено нажатие на кнопку "Купить"
    Когда экран "Покупки" загрузился
    И значение поля "money" равно "300"
    И выполнено нажатие на кнопку "Назад"
    Когда экран "Главный" загрузился
    И значение поля "money" равно "0"

  Сценарий: Проверка покупки в корзине
    Когда экран "Главный" загрузился
    И выполнено нажатие на кнопку "mouse"
    Тогда экран свайпается "UP" до элемента "mouse"
    И выполнено нажатие на кнопку "В корзину"
    Тогда экран "Корзина" загрузился
    То значение поля "money" равно "300"
    Тогда выполнено нажатие на кнопку "Купить"
    И экран "Покупки" загрузился
    То значение поля "money" равно "300"
    Тогда выполнено нажатие на кнопку "Назад"
    И экран "Главный" загрузился
    Тогда значение поля "money" равно "0"
