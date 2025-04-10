package ru.infoza.bot.bot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.infoza.bot.model.infoza.InfozaIst;
import ru.infoza.bot.model.infoza.InfozaUser;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.service.bot.MessageService;
import ru.infoza.bot.service.infoza.InfozaUserService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContactHandler {

    private final InfozaUserService infozaUserService;
    private final MessageService messageService;
    private final BotService botService;

    public void handleUpdateContact(TelegramLongPollingBot bot, Update update) {
        long chatId = update.getMessage().getChatId();
        String userPhoneNumber = update.getMessage().getContact().getPhoneNumber();
        String formattedPhoneNumber = userPhoneNumber.replaceAll("[^0-9]", "");

        // Проверяем, если номер начинается с "7" и имеет длину 11 символов, чтобы привести его к нужному формату
        if (formattedPhoneNumber.startsWith("7") && formattedPhoneNumber.length() == 11) {
            formattedPhoneNumber = formattedPhoneNumber.substring(1);
        }

        List<InfozaIst> results = infozaUserService.findIstListByPhone(formattedPhoneNumber);

        if (!results.isEmpty()) {
            if (results.size() > 1) {
                messageService.sendMessage(bot, chatId, "Номер телефона указан у нескольких пользователей");
            } else {
                // Номер телефона указан в z_ist
                InfozaUser infoUser = infozaUserService.findUserByUserName(results.get(0).getVcUSR());
                String registerResult =
                        botService.registerUser(update.getMessage(), infoUser, results.get(0).getIdIST());
                log.info(registerResult);
                messageService.sendMessageWithKeyboard(bot, chatId, "Номер телефона подтвержден");
            }
        } else {
            // Номер телефона отсутствует в z_ist
            messageService.sendMessage(bot, chatId, "Введенный номер телефона не подтвержден");
        }
    }

}
