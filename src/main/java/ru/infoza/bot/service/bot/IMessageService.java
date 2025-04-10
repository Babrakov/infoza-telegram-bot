package ru.infoza.bot.service.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public interface IMessageService {

    void sendMessage(TelegramLongPollingBot bot, Long chatId, String messageText);

    void executeMessage(TelegramLongPollingBot bot, BotApiMethod<?> message);

    void sendMessageWithKeyboard(TelegramLongPollingBot bot, Long chatId, String messageText);

    void sendMessageWithKeyboard(TelegramLongPollingBot bot, Long chatId, String messageText,
                                 ReplyKeyboard keyboardMarkup);

    Integer sendMessageWithRemoveKeyboardAndGetId(TelegramLongPollingBot bot, Long chatId);

    void sendMessageWithRemoveKeyboard(TelegramLongPollingBot bot, Long chatId, String messageText);
}
