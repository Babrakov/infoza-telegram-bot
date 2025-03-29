package ru.infoza.bot.service.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static ru.infoza.bot.bot.BotUtils.mainFunctionsKeyboardMarkup;
import static ru.infoza.bot.util.BotMessages.ERROR_TEXT;

@Slf4j
@Service
public class MessageService implements IMessageService {

    @Override
    public void sendMessage(TelegramLongPollingBot bot, Long chatId, String messageText) {
        int maxMessageLength = 4095;

        if (messageText.length() > 1024 * 100) {
            sendMessageAsFile(bot, chatId, messageText);
            return;
        }

        // Разбиваем сообщение на части по двойному переводу строки (перевод строки, затем пустая строка)
        String[] paragraphs = messageText.split("(?<=\n\n)");
        StringBuilder chunk = new StringBuilder();
        for (String paragraph : paragraphs) {
            // Если добавление следующего абзаца превысит максимальную длину, отправляем текущий chunk
            if (chunk.length() + paragraph.length() > maxMessageLength) {
                sendChunk(bot, chatId, chunk.toString());
                chunk.setLength(0); // Сбрасываем текущий chunk
                try {
                    Thread.sleep(1000); // Пауза 1 секунда
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Восстановить прерывание, если оно произошло
                }
            }
            chunk.append(paragraph); // Добавляем абзац в chunk
        }
        // Отправляем остаток, если он есть
        if (!chunk.isEmpty()) {
            sendChunk(bot, chatId, chunk.toString());
            try {
                Thread.sleep(1000); // Пауза 1 секунда
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Восстановить прерывание, если оно произошло
            }
        }
    }

    @Override
    public void executeMessage(TelegramLongPollingBot bot, BotApiMethod<?> message) {
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT, e.getMessage());
        }
    }

    @Override
    public void sendMessageWithKeyboard(TelegramLongPollingBot bot, Long chatId, String messageText) {
        sendMessageWithKeyboard(bot, chatId, messageText, mainFunctionsKeyboardMarkup());
    }

    @Override
    public void sendMessageWithKeyboard(TelegramLongPollingBot bot,
                                        Long chatId,
                                        String messageText,
                                        ReplyKeyboard keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        message.setParseMode("html");
        message.setReplyMarkup(keyboardMarkup);
        executeMessage(bot, message);
    }

    @Override
    public Integer sendMessageWithRemoveKeyboardAndGetId(TelegramLongPollingBot bot, Long chatId) {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(ru.infoza.bot.util.BotMessages.SEARCH_START);
        message.setReplyMarkup(replyKeyboardRemove);

        Integer result;
        try {
            result = bot.execute(message).getMessageId();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void sendMessageWithRemoveKeyboard(TelegramLongPollingBot bot, Long chatId, String messageText) {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);
        sendMessageWithKeyboard(bot, chatId, messageText, replyKeyboardRemove);
    }

    private void sendMessageAsFile(TelegramLongPollingBot bot, Long chatId, String messageText) {
        String formattedMessage = messageText.replace("\n", "<br>");
        // Создаем временный файл для хранения текста
        File file = new File("results_" + chatId + ".html");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(formattedMessage); // Записываем текст в файл
        } catch (IOException e) {
            log.error("Ошибка при создании файла: {}", e.getMessage());
            return;
        }

        // Отправляем файл через Telegram API
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(String.valueOf(chatId));
        sendDocument.setDocument(new org.telegram.telegrambots.meta.api.objects.InputFile(file,"results.html"));
        sendDocument.setCaption(
                "Из-за большого количества найденных записей, сообщение отправлено в виде файла"); // Можешь добавить
        // подпись

        try {
            bot.execute(sendDocument); // Отправляем файл
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке файла: {}", e.getMessage());
        } finally {
            // Удаляем файл после отправки
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void sendChunk(TelegramLongPollingBot bot, Long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setParseMode("html");
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        executeMessage(bot, message);
    }

}
