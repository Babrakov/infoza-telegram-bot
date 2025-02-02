package ru.infoza.bot.bot;

import static ru.infoza.bot.bot.BotUtils.getInlineKeyboardMarkupWithCancelButton;
import static ru.infoza.bot.bot.BotUtils.getReplyKeyboardMarkup;
import static ru.infoza.bot.bot.BotUtils.mainFunctionsKeyboardMarkup;
import static ru.infoza.bot.util.BotConstants.ASK_PHONE;
import static ru.infoza.bot.util.BotConstants.CANCEL_BUTTON;
import static ru.infoza.bot.util.BotConstants.CANCEL_REQUEST;
import static ru.infoza.bot.util.BotConstants.CARS_BUTTON;
import static ru.infoza.bot.util.BotConstants.EMAILS_BUTTON;
import static ru.infoza.bot.util.BotConstants.EMPLOYEES_BUTTON;
import static ru.infoza.bot.util.BotConstants.ERROR_TEXT;
import static ru.infoza.bot.util.BotConstants.FLS_BUTTON;
import static ru.infoza.bot.util.BotConstants.HELP_TEXT;
import static ru.infoza.bot.util.BotConstants.PHONES_BUTTON;
import static ru.infoza.bot.util.BotConstants.SEARCH_START;
import static ru.infoza.bot.util.BotConstants.ULS_BUTTON;

import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.infoza.bot.bot.helper.CarHelper;
import ru.infoza.bot.bot.helper.EmailHelper;
import ru.infoza.bot.bot.helper.EmployeeHelper;
import ru.infoza.bot.bot.helper.FlsHelper;
import ru.infoza.bot.bot.helper.PhoneHelper;
import ru.infoza.bot.bot.helper.UlsHelper;
import ru.infoza.bot.config.BotConfig;
import ru.infoza.bot.config.state.BotState;
import ru.infoza.bot.config.state.BotStateContext;
import ru.infoza.bot.model.bot.BotUser;
import ru.infoza.bot.model.infoza.InfozaIst;
import ru.infoza.bot.model.infoza.InfozaUser;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.service.infoza.InfozaUserService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {


    private final BotConfig config;
    private final BotStateContext botStateContext;
    private final BotService botService;
    private final InfozaUserService infozaUserService;
    private final FlsHelper flsHelper;
    private final CarHelper carHelper;
    private final EmailHelper emailHelper;
    private final EmployeeHelper employeeHelper;
    private final PhoneHelper phoneHelper;
    private final UlsHelper ulsHelper;


    @PostConstruct
    private void init() {
        try {
            this.execute(new SetMyCommands(getBotCommands(), new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка в настройке списка команд ботов: {}", e.getMessage());
        }
    }

    private static List<BotCommand> getBotCommands() {
        List<BotCommand> listCommands = new ArrayList<>();
        listCommands.add(new BotCommand("/start", "Запустить бота"));
        listCommands.add(new BotCommand("/help", "Как пользоваться ботом"));
        listCommands.add(new BotCommand("/login", "Зарегистрироваться"));
        listCommands.add(new BotCommand("/main", "Показать основные команды"));
        listCommands.add(new BotCommand("/logout", "Выйти"));
        return listCommands;
    }

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
            handleUpdateText(update);
        } else if (update.hasMessage() && update.getMessage().hasContact()) {
            handleUpdateContact(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (callbackData.equals(CANCEL_BUTTON)) {
            botStateContext.setUserState(chatId, BotState.START);
            EditMessageText message = new EditMessageText();
            message.setChatId(String.valueOf(chatId));
            message.setText(CANCEL_REQUEST);
            message.setMessageId((int) messageId);
            executeMessage(message);
            sendMessageWithKeyboard(chatId, "Выберите команду");
        }
    }

    private void handleUpdateContact(Update update) {
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
                sendMessage(chatId, "Номер телефона указан у нескольких пользователей");
            } else {
                // Номер телефона указан в z_ist
                InfozaUser infoUser = infozaUserService.findUserByUserName(
                        results.get(0).getVcUSR());
                String registerResult = botService.registerUser(update.getMessage(), infoUser,
                        results.get(0).getIdIST());
                log.info(registerResult);
                sendMessageWithKeyboard(chatId, "Номер телефона подтвержден");
            }
        } else {
            // Номер телефона отсутствует в z_ist
            sendMessage(chatId, "Введенный номер телефона не подтвержден");
        }
    }

    private void handleUpdateText(Update update) {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        BotState botState = botStateContext.getUserState(chatId);

        if (messageText.contains("/send") && config.getOwnerId() == chatId) {
            var textToSend = EmojiParser.parseToUnicode(
                    messageText.substring(messageText.indexOf(" ")));
            var users = botService.findUserList();
            for (BotUser botUser : users) {
                sendMessage(botUser.getChatId(), textToSend);
            }
        } else if (botState != BotState.START) {
            if (messageText.equals("/cancel")) {
                botStateContext.setUserState(chatId, BotState.START);
                sendMessageWithKeyboard(chatId, CANCEL_REQUEST);
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                handleBotStates(update, botState, chatId);
                // После завершения обработки сообщения сбрасываем состояние обратно в START
                botStateContext.setUserState(chatId, BotState.START);
            }
        } else {
            InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupWithCancelButton();
            handleGeneralCommands(update, messageText, chatId, inlineKeyboardMarkup);
        }
    }

    private void handleBotStates(Update update, BotState botState, long chatId) {
        String query = update.getMessage().getText();
        Integer messageId;

        switch (botState) {
            case WAITING_FOR_NAME_OR_COMPANY:
                employeeHelper.showEmployeeInfo(query, chatId,
                        message -> sendMessageWithKeyboard(chatId, message));
                break;
            case WAITING_FOR_FLS:
                messageId = sendMessageWithRemoveKeyboardAndGetId(chatId, SEARCH_START);
                flsHelper.showFlsInfo(query, chatId, messageId,
                        message -> sendMessage(chatId, message),
                        this::executeMessage, message -> sendMessageWithKeyboard(chatId, message));
                break;
            case WAITING_FOR_ULS:
                messageId = sendMessageWithRemoveKeyboardAndGetId(chatId, SEARCH_START);
                ulsHelper.showUlsInfo(query, chatId, messageId,
                        message -> sendMessage(chatId, message),
                        this::executeMessage, message -> sendMessageWithKeyboard(chatId, message));
                break;
            case WAITING_FOR_PHONE:
                messageId = sendMessageWithRemoveKeyboardAndGetId(chatId, SEARCH_START);
                phoneHelper.showPhoneInfo(query, chatId, messageId,
                        message -> sendMessage(chatId, message),
                        this::executeMessage, message -> sendMessageWithKeyboard(chatId, message));
                break;
            case WAITING_FOR_EMAIL:
                messageId = sendMessageWithRemoveKeyboardAndGetId(chatId, SEARCH_START);
                emailHelper.showEmailInfo(query, chatId, messageId,
                        message -> sendMessage(chatId, message), this::executeMessage,
                        message -> sendMessageWithKeyboard(chatId, message));
                break;
            case WAITING_FOR_CAR:
                messageId = sendMessageWithRemoveKeyboardAndGetId(chatId, SEARCH_START);
                carHelper.showCarInfo(query, chatId, messageId,
                        message -> sendMessage(chatId, message), this::executeMessage,
                        message -> sendMessageWithKeyboard(chatId, message));
                break;
        }
    }

    private void handleGeneralCommands(Update update, String messageText, long chatId,
            InlineKeyboardMarkup inlineKeyboardMarkup) {
        Long userId = update.getMessage().getChatId();
        switch (messageText) {
            case "/start":
                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                break;
            case "/help":
                if (botService.isUserRegistered(userId)) {
                    sendMessage(chatId, HELP_TEXT);
                } else {
                    sendMessage(chatId,
                            "Вы не авторизованы. Пожалуйста, выполните регистрацию с помощью команды /login");
                }
                break;
            case "/login":
                if (botService.isUserRegistered(userId)) {
                    sendMessage(chatId, "Вы уже авторизованы");
                } else {
                    sendRequestContactMessage(chatId);
                }
                break;

            case "/main":
                if (botService.isUserRegistered(userId)) {
                    sendMessageWithKeyboard(chatId, "Основные функции");
                } else {
                    sendRequestContactMessage(chatId);
                }
                break;

            case "/logout":
                if (botService.isUserRegistered(userId)) {
                    botService.logout(userId);
                }
                sendRequestContactMessage(chatId);
                break;
            case "/cancel":
                botStateContext.setUserState(chatId, BotState.START);
                sendMessageWithKeyboard(chatId, CANCEL_REQUEST);
                break;
            case EMPLOYEES_BUTTON:
                botStateContext.setUserState(chatId, BotState.WAITING_FOR_NAME_OR_COMPANY);
                sendMessageWithRemoveKeyboard(chatId, "Поиск сотрудников СБ");
                sendMessageWithKeyboard(chatId, "Введите фамилию или название компании для поиска:",
                        inlineKeyboardMarkup);
                break;
            case FLS_BUTTON:
                proceedExtendedAction(chatId, inlineKeyboardMarkup, BotState.WAITING_FOR_FLS,
                        "Ф.И.О. год рождения");
                break;
            case ULS_BUTTON:
                proceedExtendedAction(chatId, inlineKeyboardMarkup, BotState.WAITING_FOR_ULS,
                        "ИНН");
                break;
            case PHONES_BUTTON:
                proceedExtendedAction(chatId, inlineKeyboardMarkup, BotState.WAITING_FOR_PHONE,
                        "№ телефона");
                break;
            case EMAILS_BUTTON:
                proceedExtendedAction(chatId, inlineKeyboardMarkup, BotState.WAITING_FOR_EMAIL,
                        "email");
                break;
            case CARS_BUTTON:
                proceedExtendedAction(chatId, inlineKeyboardMarkup, BotState.WAITING_FOR_CAR,
                        "№ авто");
                break;
            default:
                sendMessage(chatId, "Извините, данная команда не поддерживается");
                sendMessageWithKeyboard(chatId, "Основные функции");

        }
    }

    private void proceedExtendedAction(long chatId, InlineKeyboardMarkup inlineKeyboardMarkup,
            BotState botState, String queryName) {
        int remainingRequests = botService.checkIfAccessPermitted(chatId, botState);
        if (remainingRequests > 0) {
            sendMessageWithRemoveKeyboard(chatId,
                    "Осталось запросов по " + queryName + ": " + remainingRequests);
            performSearchActions(chatId, inlineKeyboardMarkup, queryName, botState);
        } else if (remainingRequests == 0) {
            sendMessageWithKeyboard(chatId, "Недоступно в бесплатной версии");
        } else {
            performSearchActions(chatId, inlineKeyboardMarkup, queryName, botState);
        }
    }

    private void performSearchActions(long chatId, InlineKeyboardMarkup inlineKeyboardMarkup,
            String queryName, BotState botState) {
        botStateContext.setUserState(chatId, botState);
        sendMessageWithRemoveKeyboard(chatId, "Поиск в базе данных по " + queryName);
        sendMessageWithKeyboard(chatId, "Введите " + queryName + " для поиска:",
                inlineKeyboardMarkup);
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = EmojiParser.parseToUnicode(
                "Добро пожаловать, " + name + "! :blush: \nВыберите пункт меню");
        log.info("Отправлен ответ пользователю {}", name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(Long chatId, String messageText) {
        int maxMessageLength = 4095;
        // Разбиваем сообщение на части по двойному переводу строки (перевод строки, затем пустая строка)
        String[] paragraphs = messageText.split("(?<=\n\n)");
        StringBuilder chunk = new StringBuilder();
        for (String paragraph : paragraphs) {
            // Если добавление следующего абзаца превысит максимальную длину, отправляем текущий chunk
            if (chunk.length() + paragraph.length() > maxMessageLength) {
                sendChunk(chatId, chunk.toString());
                chunk.setLength(0); // Сбрасываем текущий chunk
            }
            chunk.append(paragraph); // Добавляем абзац в chunk
        }
        // Отправляем остаток, если он есть
        if (chunk.length() > 0) {
            sendChunk(chatId, chunk.toString());
        }
    }

    private void sendChunk(Long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setParseMode("html");
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        executeMessage(message);
    }

    private void sendMessageWithKeyboard(Long chatId, String messageText) {
        sendMessageWithKeyboard(chatId, messageText, mainFunctionsKeyboardMarkup());
    }

    private void sendMessageWithKeyboard(Long chatId, String messageText,
            ReplyKeyboard keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        message.setParseMode("html");
        message.setReplyMarkup(keyboardMarkup);
        executeMessage(message);
    }

    private void sendMessageWithRemoveKeyboard(Long chatId, String messageText) {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);
        sendMessageWithKeyboard(chatId, messageText, replyKeyboardRemove);
    }

    private Integer sendMessageWithRemoveKeyboardAndGetId(Long chatId, String messageText) {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        message.setReplyMarkup(replyKeyboardRemove);

        Integer result;
        try {
            result = execute(message).getMessageId();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private void executeMessage(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void sendRequestContactMessage(long chatId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getReplyKeyboardMarkup();
        sendMessageWithKeyboard(chatId, ASK_PHONE, replyKeyboardMarkup);
    }

}