package ru.infoza.bot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.infoza.bot.bot.TelegramBot;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static ru.infoza.bot.util.ServiceMessages.ERROR_SETTING_BOT_COMMAND;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotInitializer {

    private final TelegramBot bot;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);

    }

    @PostConstruct
    private void setCommands() {
        try {
            bot.execute(new SetMyCommands(getBotCommands(), new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(ERROR_SETTING_BOT_COMMAND, e.getMessage());
        }
    }

    private List<BotCommand> getBotCommands() {
        List<BotCommand> listCommands = new ArrayList<>();
        listCommands.add(new BotCommand("/start", "Запустить бота"));
        listCommands.add(new BotCommand("/help", "Как пользоваться ботом"));
        listCommands.add(new BotCommand("/login", "Зарегистрироваться"));
        listCommands.add(new BotCommand("/main", "Показать основные команды"));
        listCommands.add(new BotCommand("/logout", "Выйти"));
        return listCommands;
    }

}
