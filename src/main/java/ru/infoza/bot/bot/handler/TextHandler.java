package ru.infoza.bot.bot.handler;

import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.infoza.bot.bot.helper.BotHelper;
import ru.infoza.bot.bot.helper.HelperManager;
import ru.infoza.bot.config.BotConfig;
import ru.infoza.bot.config.state.BotState;
import ru.infoza.bot.config.state.BotStateContext;
import ru.infoza.bot.model.bot.BotUser;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.service.bot.IMessageService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static ru.infoza.bot.bot.BotUtils.getInlineKeyboardMarkupWithCancelButton;
import static ru.infoza.bot.bot.BotUtils.getReplyKeyboardMarkup;
import static ru.infoza.bot.util.BotMessages.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class TextHandler {

    private final BotConfig config;
    private final BotStateContext botStateContext;
    private final BotService botService;
    private final HelperManager helperManager;
    private final IMessageService messageService;

    public void handleUpdateText(TelegramLongPollingBot bot, Update update) {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        BotState botState = botStateContext.getUserState(chatId);

        if (messageText.contains("/send") && config.getOwnerId() == chatId) {
            var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
            var users = botService.findUserList();
            for (BotUser botUser : users) {
                messageService.sendMessage(bot, botUser.getChatId(), textToSend);
            }
        } else if (botState != BotState.START) {
            if (messageText.equals("/cancel")) {
                botStateContext.setUserState(chatId, BotState.START);
                messageService.sendMessageWithKeyboard(bot, chatId, CANCEL_REQUEST);
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                handleBotStates(bot, update, botState, chatId);
                // После завершения обработки сообщения сбрасываем состояние обратно в START
                botStateContext.setUserState(chatId, BotState.START);
            }
        } else {
            InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupWithCancelButton();
            handleGeneralCommands(bot, update, messageText, chatId, inlineKeyboardMarkup);
        }
    }

    private void handleBotStates(TelegramLongPollingBot bot, Update update, BotState botState, long chatId) {

        Map<BotState, BiConsumer<String, Integer>> handlers = Map.of(
                BotState.WAITING_FOR_NAME_OR_COMPANY,
                (query, msgId) -> showInfoFromHelper(helperManager.getEmployeeHelper(), query, chatId, msgId, bot),
                BotState.WAITING_FOR_FLS,
                (query, msgId) -> showInfoFromHelper(helperManager.getFlsHelper(), query, chatId, msgId, bot),
                BotState.WAITING_FOR_ULS,
                (query, msgId) -> showInfoFromHelper(helperManager.getUlsHelper(), query, chatId, msgId, bot),
                BotState.WAITING_FOR_PHONE,
                (query, msgId) -> showInfoFromHelper(helperManager.getPhoneHelper(), query, chatId, msgId, bot),
                BotState.WAITING_FOR_EMAIL,
                (query, msgId) -> showInfoFromHelper(helperManager.getEmailHelper(), query, chatId, msgId, bot),
                BotState.WAITING_FOR_CAR,
                (query, msgId) -> showInfoFromHelper(helperManager.getCarHelper(), query, chatId, msgId, bot)
        );

        Integer messageId = messageService.sendMessageWithRemoveKeyboardAndGetId(bot, chatId);
        handlers.getOrDefault(botState, (q, m) -> {}).accept(update.getMessage().getText(), messageId);
    }

    private void showInfoFromHelper(BotHelper helper, String query, long chatId, Integer messageId, TelegramLongPollingBot bot) {
        helper.showInfo(query, chatId, messageId,
                msg -> messageService.sendMessage(bot, chatId, msg),
                msg -> messageService.executeMessage(bot, msg),
                msg -> messageService.sendMessageWithKeyboard(bot, chatId, msg));
    }

    private void handleGeneralCommands(TelegramLongPollingBot bot,
                                       Update update,
                                       String messageText,
                                       long chatId,
                                       InlineKeyboardMarkup inlineKeyboardMarkup) {
        Long userId = update.getMessage().getChatId();
        Map<String, Runnable> commandMap = new HashMap<>();

        commandMap.put(MENU_START_BUTTON, () -> startCommandReceived(bot, chatId, update.getMessage().getChat().getFirstName()));
        commandMap.put(MENU_HELP_BUTTON, () -> handleHelpCommand(bot, chatId, userId));
        commandMap.put(MENU_LOGIN_BUTTON, () -> handleLoginCommand(bot, chatId, userId));
        commandMap.put(MENU_MAIN_BUTTON, () -> handleMainCommand(bot, chatId, userId));
        commandMap.put(MENU_LOGOUT_BUTTON, () -> handleLogoutCommand(bot, chatId, userId));
        commandMap.put(MENU_CANCEL_BUTTON, () -> handleCancelCommand(bot, chatId));
        commandMap.put(EMPLOYEES_BUTTON, () -> handleEmployeesCommand(bot, chatId, inlineKeyboardMarkup));

        Map<String, BotState> extendedActions = Map.of(
                FLS_BUTTON, BotState.WAITING_FOR_FLS,
                ULS_BUTTON, BotState.WAITING_FOR_ULS,
                PHONES_BUTTON, BotState.WAITING_FOR_PHONE,
                EMAILS_BUTTON, BotState.WAITING_FOR_EMAIL,
                CARS_BUTTON, BotState.WAITING_FOR_CAR
        );

        if (commandMap.containsKey(messageText)) {
            commandMap.get(messageText).run();
        } else if (extendedActions.containsKey(messageText)) {
            proceedExtendedAction(bot, chatId, inlineKeyboardMarkup, extendedActions.get(messageText), messageText);
        } else {
            messageService.sendMessage(bot, chatId, "Извините, данная команда не поддерживается");
            messageService.sendMessageWithKeyboard(bot, chatId, "Основные функции");
        }
    }

    private void handleHelpCommand(TelegramLongPollingBot bot, long chatId, Long userId) {
        if (botService.isUserRegistered(userId)) {
            messageService.sendMessage(bot, chatId, HELP_TEXT);
        } else {
            messageService.sendMessage(bot, chatId, "Вы не авторизованы. Пожалуйста, выполните регистрацию с помощью команды /login");
        }
    }

    private void handleLoginCommand(TelegramLongPollingBot bot, long chatId, Long userId) {
        if (botService.isUserRegistered(userId)) {
            messageService.sendMessage(bot, chatId, "Вы уже авторизованы");
        } else {
            sendRequestContactMessage(bot, chatId);
        }
    }

    private void handleMainCommand(TelegramLongPollingBot bot, long chatId, Long userId) {
        if (botService.isUserRegistered(userId)) {
            messageService.sendMessageWithKeyboard(bot, chatId, "Основные функции");
        } else {
            sendRequestContactMessage(bot, chatId);
        }
    }

    private void handleLogoutCommand(TelegramLongPollingBot bot, long chatId, Long userId) {
        if (botService.isUserRegistered(userId)) {
            botService.logout(userId);
        }
        sendRequestContactMessage(bot, chatId);
    }

    private void handleCancelCommand(TelegramLongPollingBot bot, long chatId) {
        botStateContext.setUserState(chatId, BotState.START);
        messageService.sendMessageWithKeyboard(bot, chatId, CANCEL_REQUEST);
    }

    private void handleEmployeesCommand(TelegramLongPollingBot bot, long chatId, InlineKeyboardMarkup inlineKeyboardMarkup) {
        botStateContext.setUserState(chatId, BotState.WAITING_FOR_NAME_OR_COMPANY);
        messageService.sendMessageWithRemoveKeyboard(bot, chatId, "Поиск сотрудников СБ");
        messageService.sendMessageWithKeyboard(bot, chatId, "Введите фамилию или название компании для поиска:", inlineKeyboardMarkup);
    }

    private void proceedExtendedAction(TelegramLongPollingBot bot,
                                       long chatId,
                                       InlineKeyboardMarkup inlineKeyboardMarkup,
                                       BotState botState,
                                       String queryName) {
        int remainingRequests = botService.checkIfAccessPermitted(chatId, botState);
        if (remainingRequests > 0) {
            messageService.sendMessageWithRemoveKeyboard(bot, chatId,
                    "Осталось запросов по " + queryName + ": " + remainingRequests);
            performSearchActions(bot, chatId, inlineKeyboardMarkup, queryName, botState);
        } else if (remainingRequests == 0) {
            messageService.sendMessageWithKeyboard(bot, chatId, "Недоступно в бесплатной версии");
        } else {
            performSearchActions(bot, chatId, inlineKeyboardMarkup, queryName, botState);
        }
    }

    private void performSearchActions(TelegramLongPollingBot bot,
                                      long chatId,
                                      InlineKeyboardMarkup inlineKeyboardMarkup,
                                      String queryName,
                                      BotState botState) {
        botStateContext.setUserState(chatId, botState);
        messageService.sendMessageWithRemoveKeyboard(bot, chatId, "Поиск в базе данных по " + queryName);
        messageService.sendMessageWithKeyboard(bot, chatId, "Введите " + queryName + " для поиска:",
                inlineKeyboardMarkup);
    }

    private void startCommandReceived(TelegramLongPollingBot bot,
                                      Long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Добро пожаловать, " + name + "! :blush: \nВыберите пункт меню");
        log.info("Отправлен ответ пользователю {}", name);
        messageService.sendMessage(bot, chatId, answer);
    }

    private void sendRequestContactMessage(TelegramLongPollingBot bot, long chatId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getReplyKeyboardMarkup();
        messageService.sendMessageWithKeyboard(bot, chatId, ASK_PHONE, replyKeyboardMarkup);
    }

}
