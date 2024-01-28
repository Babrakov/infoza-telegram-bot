package ru.infoza.bot.util;
public class BotConstants {

    public static final String EMPLOYEES_BUTTON = "Безопасники";
    public static final String FLS_BUTTON = "Физики";
    public static final String ULS_BUTTON = "Организации";
    public static final String PHONES_BUTTON = "Телефоны";
    public static final String EMAILS_BUTTON = "Email";
    public static final String CARS_BUTTON = "Авто";
    public static final String CANCEL_BUTTON = "CANCEL_BUTTON";

    public static final String CANCEL_REQUEST = "Запрос отменен";

    public static final String ERROR_TEXT = "Произошла ошибка: ";
    public static final String INFO_NOT_FOUND = "Информация не найдена";
    public static final String SEARCH_COMPLETE = "Поиск завершен";
    public static final String SEARCH_START = "Идет поиск...";

    public static final String HELP_TEXT = "Бот предназначен для работы с ИНФОЗА.\n\n" +
            "Вы можете выполнить команду из основного меню слева или набрать команду вручную:\n\n" +
            "Введите /start чтобы показать приветствие\n\n" +
            "Введите /help чтобы показать данное сообщение\n\n" +
            "Введите /login чтобы зарегистрироваться\n\n" +
            "Введите /main чтобы показать основное меню\n\n" +
            "Введите /logout чтобы выйти";

    public static final String ASK_PHONE = "Вы не авторизованы. Пожалуйста, предоставьте боту доступ к Вашему номеру телефона - нажмите кнопку \"Разрешить\". \n" +
            "Важно: номер телефона должен быть указан у Вас в \"Настройках\" на сайте infoza.ru";

    // Private constructor to prevent instantiation
    private BotConstants() {
        throw new UnsupportedOperationException("Невозможно создать экземпляр этого класса");
    }

}
