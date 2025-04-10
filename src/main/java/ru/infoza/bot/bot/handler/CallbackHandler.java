package ru.infoza.bot.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.infoza.bot.config.state.BotState;
import ru.infoza.bot.config.state.BotStateContext;
import ru.infoza.bot.service.bot.MessageService;

import static ru.infoza.bot.util.BotMessages.*;

@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private final BotStateContext botStateContext;
    private final MessageService messageService;

    public void handleCallbackQuery(TelegramLongPollingBot bot, Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (callbackData.equals(CANCEL_CALLBACK)) {
            processCancelRequest(bot, chatId, messageId);
        }
    }

    private void processCancelRequest(TelegramLongPollingBot bot, long chatId, long messageId) {
        botStateContext.setUserState(chatId, BotState.START);

        EditMessageText message = createEditMessageText(chatId, messageId, CANCEL_REQUEST);
        messageService.executeMessage(bot, message);
        messageService.sendMessageWithKeyboard(bot, chatId, CHOOSE_COMMAND);
    }

    private EditMessageText createEditMessageText(long chatId, long messageId, String text) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setMessageId((int) messageId);
        message.setText(text);
        return message;
    }
}
