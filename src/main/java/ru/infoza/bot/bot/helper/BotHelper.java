package ru.infoza.bot.bot.helper;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import java.util.function.Consumer;

public interface BotHelper {

    void showInfo(String query,
                  long chatId,
                  Integer messageToDelete,
                  Consumer<String> sendMessage,
                  Consumer<DeleteMessage> executeMessage,
                  Consumer<String> sendMessageWithKeyboard);
}
