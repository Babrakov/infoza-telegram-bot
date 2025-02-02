package ru.infoza.bot.bot;

import static ru.infoza.bot.util.BotConstants.CANCEL_BUTTON;
import static ru.infoza.bot.util.BotConstants.CARS_BUTTON;
import static ru.infoza.bot.util.BotConstants.EMAILS_BUTTON;
import static ru.infoza.bot.util.BotConstants.EMPLOYEES_BUTTON;
import static ru.infoza.bot.util.BotConstants.FLS_BUTTON;
import static ru.infoza.bot.util.BotConstants.PHONES_BUTTON;
import static ru.infoza.bot.util.BotConstants.ULS_BUTTON;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class BotUtils {

    static InlineKeyboardMarkup getInlineKeyboardMarkupWithCancelButton() {
        // Создаем инлайн-клавиатуру
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        // Создаем кнопку "Отмена" с колбэк-данными "cancel"
        InlineKeyboardButton cancelButton = new InlineKeyboardButton("Отмена");
        cancelButton.setCallbackData(CANCEL_BUTTON);

        // Добавляем кнопку в строку и строку в клавиатуру
        row.add(cancelButton);
        rows.add(row);

        // Устанавливаем клавиатуру для сообщения
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    static ReplyKeyboardMarkup mainFunctionsKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(EMPLOYEES_BUTTON);
        keyboardRows.add(row);
        row.add(FLS_BUTTON);
        row.add(ULS_BUTTON);

        row = new KeyboardRow();
        row.add(EMAILS_BUTTON);
        row.add(CARS_BUTTON);
        row.add(PHONES_BUTTON);

        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    static ReplyKeyboardMarkup getReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true); // Клавиатура скроется после использования

        // Create a list of keyboard rows
        List<KeyboardRow> keyboard = new ArrayList<>();

        // First keyboard row
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardButton button = new KeyboardButton("Разрешить");
        button.setRequestContact(true); // Запросить номер телефона у пользователя

        // Add buttons to the first keyboard row
        keyboardFirstRow.add(button);

        // Add all the keyboard rows to the list
        keyboard.add(keyboardFirstRow);
        // and assign this list to our keyboard
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

}
