package ru.infoza.bot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.infoza.bot.bot.handler.CallbackHandler;
import ru.infoza.bot.bot.handler.ContactHandler;
import ru.infoza.bot.bot.handler.TextHandler;
import ru.infoza.bot.config.BotConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final TextHandler textHandler;
    private final ContactHandler contactHandler;
    private final CallbackHandler callbackHandler;

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            textHandler.handleUpdateText(this, update);
        } else if (update.hasMessage() && update.getMessage().hasContact()) {
            contactHandler.handleUpdateContact(this, update);
        } else if (update.hasCallbackQuery()) {
            callbackHandler.handleCallbackQuery(this, update);
        }
    }

}