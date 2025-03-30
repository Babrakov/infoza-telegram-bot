package ru.infoza.bot.util;

import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import static ru.infoza.bot.util.BotMessages.*;

public class BotUtils {

    public static InlineKeyboardMarkup getInlineKeyboardMarkupWithCancelButton() {
        // Create inline-keyboard
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        // Create a "Cancel" button with "cancel" callback data
        InlineKeyboardButton cancelButton = new InlineKeyboardButton(CANCEL_BUTTON);
        cancelButton.setCallbackData(CANCEL_CALLBACK);

        // Add the button to a row and the row to the keyboard
        row.add(cancelButton);
        rows.add(row);

        // Set the keyboard for the message
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup mainFunctionsKeyboardMarkup() {
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

    public static ReplyKeyboardMarkup getReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true); // The keyboard will be hidden after use

        // Create a list of keyboard rows
        List<KeyboardRow> keyboard = new ArrayList<>();

        // First keyboard row
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(ALLOW_BUTTON);
        button.setRequestContact(true); // Request the user's phone number

        // Add buttons to the first keyboard row
        keyboardFirstRow.add(button);

        // Add all the keyboard rows to the list
        keyboard.add(keyboardFirstRow);
        // and assign this list to our keyboard
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

}
