package ru.infoza.simplebot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.infoza.simplebot.config.BotConfig;
import ru.infoza.simplebot.config.state.BotState;
import ru.infoza.simplebot.config.state.BotStateContext;
import ru.infoza.simplebot.model.bot.BotUser;
import ru.infoza.simplebot.model.info.*;
import ru.infoza.simplebot.repository.bot.BotUserRepository;
import ru.infoza.simplebot.repository.info.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static ru.infoza.simplebot.util.JuridicalPersonUtils.isValidINN;
import static ru.infoza.simplebot.util.PhoneUtils.formatPhoneNumber;
import static ru.infoza.simplebot.util.PhoneUtils.formatPhoneNumberTenDigits;
import static ru.infoza.simplebot.util.PhysicalPersonUtils.md5Hash;
import static ru.infoza.simplebot.util.PhysicalPersonUtils.resolvedInFls;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final InfozaJuridicalPersonRepository infozaJuridicalPersonRepository;
    private final InfozaPhoneRepository infozaPhoneRepository;

    public static final String EMPLOYEES = "Безопасники";
    public static final String FLS = "Физики";
    public static final String ULS = "Организации";
    public static final String PHONES = "Телефоны";
    public static final String CANCEL_REQUEST = "Запрос отменен";
    final BotConfig config;
    private final BotUserRepository botUserRepository;
    private final InfozaIstRepository infozaIstRepository;
    private final InfozaUserRepository infozaUserRepository;
    private final InfozaPhoneRemRepository infozaPhoneRemRepository;
    private final InfozaJuridicalPersonRemRepository infozaJuridicalPersonRemRepository;
    private final InfozaPhysicalPersonRemRepository infozaPhysicalPersonRemRepository;
    private final InfozaPhoneRequestRepository infozaPhoneRequestRepository;
    private final InfozaPhysicalPersonRequestRepository infozaPhysicalPersonRequestRepository;
    private final InfozaJuridicalPersonRequestRepository infozaJuridicalPersonRequestRepository;
    private final InfozaJuridicalPersonAccountRepository infozaJuridicalPersonAccountRepository;
    private final InfozaBankRepository infozaBankRepository;
    private final BotStateContext botStateContext;

    static final String HELP_TEXT = "Бот предназначен для работы с ИНФОЗА.\n\n" +
            "Вы можете выполнить команду из основного меню слева или набрать команду вручную:\n\n" +
            "Введите /start чтобы показать приветствие\n\n" +
            "Введите /help чтобы показать данное сообщение\n\n" +
            "Введите /login чтобы зарегистрироваться\n\n" +
            "Введите /main чтобы показать основное меню\n\n" +
            "Введите /logout чтобы выйти";

    public static final String CANCEL_BUTTON = "CANCEL_BUTTON";

    static final String ERROR_TEXT = "Error occurred: ";


    public TelegramBot(BotConfig config, BotUserRepository botUserRepository,
                       InfozaUserRepository infozaUserRepository, InfozaIstRepository infozaIstRepository,
                       InfozaPhoneRemRepository infozaPhoneRemRepository, InfozaJuridicalPersonRemRepository infozaJuridicalPersonRemRepository, InfozaPhysicalPersonRemRepository infozaPhysicalPersonRemRepository, InfozaPhysicalPersonRequestRepository infozaPhysicalPersonRequestRepository, BotStateContext botStateContext,
                       InfozaPhoneRepository infozaPhoneRepository, InfozaPhoneRequestRepository infozaPhoneRequestRepository, InfozaJuridicalPersonRequestRepository infozaJuridicalPersonRequestRepository,
                       InfozaJuridicalPersonRepository infozaJuridicalPersonRepository, InfozaJuridicalPersonAccountRepository infozaJuridicalPersonAccountRepository, InfozaBankRepository infozaBankRepository) {
        this.config = config;
        this.botUserRepository = botUserRepository;
        this.infozaUserRepository = infozaUserRepository;
        this.infozaIstRepository = infozaIstRepository;
        this.infozaPhoneRemRepository = infozaPhoneRemRepository;
        this.infozaJuridicalPersonRemRepository = infozaJuridicalPersonRemRepository;
        this.infozaPhysicalPersonRemRepository = infozaPhysicalPersonRemRepository;
        this.infozaPhysicalPersonRequestRepository = infozaPhysicalPersonRequestRepository;
        this.botStateContext = botStateContext;
        this.infozaPhoneRequestRepository = infozaPhoneRequestRepository;
        this.infozaJuridicalPersonRequestRepository = infozaJuridicalPersonRequestRepository;
        this.infozaJuridicalPersonAccountRepository = infozaJuridicalPersonAccountRepository;
        this.infozaBankRepository = infozaBankRepository;

        List<BotCommand> listCommands = new ArrayList<>();
        listCommands.add(new BotCommand("/start", "Запустить бота"));
        listCommands.add(new BotCommand("/help", "Как пользоваться ботом"));
        listCommands.add(new BotCommand("/login", "Зарегистрироваться"));
        listCommands.add(new BotCommand("/main", "Показать основные команды"));
        listCommands.add(new BotCommand("/logout", "Выйти"));
        try {
            this.execute(new SetMyCommands(listCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bots command list: " + e.getMessage());
        }
        this.infozaPhoneRepository = infozaPhoneRepository;
        this.infozaJuridicalPersonRepository = infozaJuridicalPersonRepository;
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
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            BotState botState = botStateContext.getUserState(chatId);


            if (messageText.contains("/send") && config.getOwnerId() == chatId) {
                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                var users = botUserRepository.findAll();
                for (BotUser botUser : users) {
                    sendMessage(botUser.getChatId(), textToSend);
                }
            } else if (botState != BotState.START) {
                if (messageText.equals("/cancel")) {
                    botStateContext.setUserState(chatId, BotState.START);
                    sendMessageWithKeyboard(chatId, CANCEL_REQUEST);
                } else if (update.hasMessage() && update.getMessage().hasText()) {
                    String query = update.getMessage().getText();
                    switch (botState) {
                        case WAITING_FOR_NAME_OR_COMPANY:
                            showEmployeeInfo(query, chatId);
                            break;
                        case WAITING_FOR_FLS:
                            showFlsInfo(query, chatId);
                            break;
                        case WAITING_FOR_ULS:
                            showUlsInfo(query, chatId);
                            break;
                        case WAITING_FOR_PHONE:
                            showPhoneInfo(query, chatId);
                            break;
                    }
                    // После завершения обработки сообщения сбрасываем состояние обратно в START
                    botStateContext.setUserState(chatId, BotState.START);
                }
            } else {
                InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupWithCancelButton();
                switch (messageText) {
                    case "/start":
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        break;
                    case "/help":
                        if (isUserRegistered(update.getMessage().getChatId())) {
                            sendMessage(chatId, HELP_TEXT);
                        } else {
                            sendMessage(chatId, "Вы не авторизованы. Пожалуйста, выполните регистрацию с помощью команды /login");
                        }
                        break;
                    case "/login":
                        if (isUserRegistered(update.getMessage().getChatId())) {
                            sendMessage(chatId, "Вы уже авторизованы");
                        } else {
                            sendRequestContactMessage(chatId);
                        }
                        break;

                    case "/main":
                        if (isUserRegistered(update.getMessage().getChatId())) {
                            sendMessageWithKeyboard(chatId, "Основные функции");
                        } else {
                            sendRequestContactMessage(chatId);
                        }
                        break;

                    case "/logout":
                        if (isUserRegistered(update.getMessage().getChatId())) {
                            botUserRepository.findById(update.getMessage().getChatId()).ifPresent(botUserRepository::delete);
                        }
                        sendRequestContactMessage(chatId);
                        break;
                    case "/cancel":
                        botStateContext.setUserState(chatId, BotState.START);
                        sendMessageWithKeyboard(chatId, CANCEL_REQUEST);
                        break;
                    case EMPLOYEES:
                        botStateContext.setUserState(chatId, BotState.WAITING_FOR_NAME_OR_COMPANY);
                        sendMessageWithRemoveKeyboard(chatId,"Поиск сотрудников СБ");
                        sendMessageWithKeyboard(chatId, "Введите фамилию или название компании для поиска:",inlineKeyboardMarkup);
                        break;
                    case FLS:
                        proceedExtendedAction(chatId, inlineKeyboardMarkup, BotState.WAITING_FOR_FLS, "Ф.И.О. год рождения");
                        break;
                    case ULS:
                        proceedExtendedAction(chatId, inlineKeyboardMarkup, BotState.WAITING_FOR_ULS, "ИНН");
                        break;
                    case PHONES:
                        proceedExtendedAction(chatId, inlineKeyboardMarkup, BotState.WAITING_FOR_PHONE, "ном. телеф.");
                        break;
                    default:
                        sendMessage(chatId, "Извините, данная команда не поддерживается");

                }
            }


        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals(CANCEL_BUTTON)) {
                botStateContext.setUserState(chatId, BotState.START);
                EditMessageText message = new EditMessageText();
                message.setChatId(String.valueOf(chatId));
                message.setText(CANCEL_REQUEST);
                message.setMessageId((int) messageId);
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    log.error(ERROR_TEXT + e.getMessage());
                }
                sendMessageWithKeyboard(chatId,"Выберите команду");
            }
        } else if (update.hasMessage() && update.getMessage().hasContact()) {

            long chatId = update.getMessage().getChatId();
            String userPhoneNumber = update.getMessage().getContact().getPhoneNumber();
            String formattedPhoneNumber = userPhoneNumber.replaceAll("[^0-9]", "");

            // Проверяем, если номер начинается с "7" и имеет длину 11 символов, чтобы привести его к нужному формату
            if (formattedPhoneNumber.startsWith("7") && formattedPhoneNumber.length() == 11) {
                formattedPhoneNumber = formattedPhoneNumber.substring(1);
            }

            List<InfozaIst> results = infozaIstRepository.findInfoIstByPhoneNumber(formattedPhoneNumber);

            if (!results.isEmpty()) {
                if (results.size()>1) {
                    sendMessage(chatId, "Номер телефона указан у нескольких пользователей");
                } else {
                    // Номер телефона указан в z_ist
                    InfozaUser infoUser = infozaUserRepository.findByVcUSR(results.get(0).getVcUSR());
                    registerUser(update.getMessage(), infoUser, results.get(0).getIdIST());
                    sendMessageWithKeyboard(chatId, "Номер телефона подтвержден");
                }
            } else {
                // Номер телефона отсутствует в z_ist
                sendMessage(chatId, "Введенный номер телефона не подтвержден");
            }
        }
    }

    private void proceedExtendedAction(long chatId, InlineKeyboardMarkup inlineKeyboardMarkup,
                                       BotState botState, String queryName) {
        if (checkIfExtendedAccessPermitted(chatId)) {
            botStateContext.setUserState(chatId, botState);
            sendMessageWithRemoveKeyboard(chatId, "Поиск в базе данных по " + queryName);
            sendMessageWithKeyboard(chatId, "Введите " + queryName + " для поиска:", inlineKeyboardMarkup);
        } else {
            sendMessageWithKeyboard(chatId, "Недоступно в бесплатной версии");
        }
    }

    private boolean checkIfExtendedAccessPermitted(Long chatId) {
        BotUser user = botUserRepository.findById(chatId).orElseThrow();
        return user.getTip()>3;
    }

    private void showPhoneInfo(String query, long chatId) {
        String formattedPhoneNumber = formatPhoneNumberTenDigits(query);
        List<InfozaPhoneRem> phones = infozaPhoneRemRepository.findByVcPHO(formattedPhoneNumber);

        InfozaPhone infozaPhone = infozaPhoneRepository
                .findByVcPHO(formattedPhoneNumber);

        for (InfozaPhoneRem phone: phones) {
            InfozaIst ist = infozaIstRepository.findById(phone.getInIST()).orElseThrow();

            String pattern = "dd.MM.yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            String date = simpleDateFormat.format(java.util.Date.from(phone.getDtCRE()));

            String answer = phone.getVcREM() +
                    " \n(источник: <b>" +
                    ist.getVcORG() +
                    "</b> " +
                    date +
                    ")";
            sendMessageWithKeyboard(chatId, answer);
        }
        if (infozaPhone!=null) {
            List<InfozaPhoneRequest> phoneRequests = infozaPhoneRequestRepository
                    .findByIdZP(infozaPhone.getId());
            StringBuilder answer = new StringBuilder();
            for (InfozaPhoneRequest request: phoneRequests) {
                InfozaIst ist = infozaIstRepository.findById(request.getInIST()).orElseThrow();

                String pattern = "dd.MM.yyyy";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String date = simpleDateFormat.format(java.util.Date.from(request.getDtCRE()));

                answer.append(date).append(" ").append(ist.getVcORG()).append("\n");
            }
            if (answer.length() > 0) {
                sendMessageWithKeyboard(chatId, "Запросы:\n" + answer);
            }

        } else if (phones.isEmpty()) {
            sendMessageWithKeyboard(chatId, "Информация не найдена");
        }

    }

    private void showUlsInfo(String query, long chatId) {
        String inn = query.trim();
        if (isValidINN(inn)) {
            List<InfozaJuridicalPersonRem> orgs = infozaJuridicalPersonRemRepository.findByVcINN(query);

            for (InfozaJuridicalPersonRem org: orgs) {
                InfozaIst ist = infozaIstRepository.findById(org.getInIST()).orElseThrow();

                String pattern = "dd.MM.yyyy";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String date = simpleDateFormat.format(java.util.Date.from(org.getDtCRE()));

                String answer = org.getVcREM() +
                        " \n(источник: <b>" +
                        ist.getVcORG() +
                        "</b> " +
                        date +
                        ")";
                sendMessageWithKeyboard(chatId, answer);
            }

            List<InfozaJuridicalPersonAccount> accounts = infozaJuridicalPersonAccountRepository.findByVcINN(inn);

            List<InfozaJuridicalPerson> infozaJuridicalPerson = infozaJuridicalPersonRepository.findByVcINN(inn);

            if (infozaJuridicalPerson.isEmpty() && accounts.isEmpty() && orgs.isEmpty()) {
                sendMessageWithKeyboard(chatId, "Информация не найдена");
            }

            StringBuilder answer = new StringBuilder();
            for (InfozaJuridicalPerson person: infozaJuridicalPerson) {
                InfozaIst ist = infozaIstRepository.findById(person.getInIST()).orElseThrow();

                String pattern = "dd.MM.yyyy";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String date = simpleDateFormat.format(java.util.Date.from(person.getDtCRE()));

                answer.append(date).append(" ").append(ist.getVcORG()).append("\n");

                List<InfozaJuridicalPersonRequest> requests = infozaJuridicalPersonRequestRepository
                        .findByIdZO(person.getId());
                for (InfozaJuridicalPersonRequest request: requests) {
                    InfozaIst requestIst = infozaIstRepository.findById(request.getInIST()).orElseThrow();
                    String requestDate = simpleDateFormat.format(java.util.Date.from(request.getDtCRE()));
                    if (!requestIst.equals(ist) && !requestDate.equals(date))
                        answer.append(date).append(" ").append(ist.getVcORG()).append("\n");
                }

            }
            if (answer.length() > 0) {
                sendMessageWithKeyboard(chatId, "Запросы:\n" + answer);
            }

            StringBuilder accountAnswer = new StringBuilder();
            for (InfozaJuridicalPersonAccount account: accounts) {
                InfozaBank bank = infozaBankRepository.findTopByVcBIKOrderByDaIZM(account.getVcBIK());
                if (bank!=null) {
                    String bankName = account.getVcBIK() + " " + bank.getVcNAZ();
                    if (bank.getDaDEL()!=null) {
                        bankName = "<s>" + bankName + "</s>";
                    }
                    LocalDate currentDate = LocalDate.now();
                    // Получаем дату, которая находится на 6 месяцев назад
                    LocalDate sixMonthsAgo = currentDate.minusMonths(6);
                    LocalDate twoMonthsAgo = currentDate.minusMonths(2);

                    if (account.getDCHE().isAfter(sixMonthsAgo)) {
                        bankName = "<b>" + bankName + "</b>";
                    }
                    if (account.getDCHE().isAfter(twoMonthsAgo)) {
                        bankName = "<u>" + bankName + "</u>";
                    }
                    accountAnswer
                            .append(bankName)
                            .append("\n");
                }

            }
            if (accountAnswer.length() > 0) {
                sendMessageWithKeyboard(chatId, "Счета:\n" + accountAnswer);
            }

        } else {
            sendMessageWithKeyboard(chatId, "Указан несуществующий ИНН");
        }
    }

    private void showFlsInfo(String query, long chatId) {
        String hash = md5Hash(query.trim().toUpperCase());
        List<InfozaPhysicalPersonRem> physics = infozaPhysicalPersonRemRepository.findByVcHASH(hash);

        List<InfozaPhysicalPersonRequest> requests = infozaPhysicalPersonRequestRepository.findByVcHASH(hash);

        if(physics.isEmpty() && requests.isEmpty()) {
            sendMessageWithKeyboard(chatId, "Информация не найдена");
        }
        for (InfozaPhysicalPersonRem person: physics) {
            InfozaIst ist = infozaIstRepository.findById(person.getInIST()).orElseThrow();
            String info = person.getVcREM();
            Long inFls = person.getInFLS();
            if (inFls != 0) {
                info += "\n" + resolvedInFls(inFls);
            }

            String pattern = "dd.MM.yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(java.util.Date.from(person.getDtCRE()));


            String answer = info +
                    " \n(источник: <b>" +
                    ist.getVcORG() +
                    "</b> " +
                    date +
                    ")";
            sendMessageWithKeyboard(chatId, answer);
        }

        StringBuilder answer = new StringBuilder();
        for (InfozaPhysicalPersonRequest request: requests) {
            InfozaIst ist = infozaIstRepository.findById(request.getInIST()).orElseThrow();

            String pattern = "dd.MM.yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(java.util.Date.from(request.getDtCRE()));

            answer.append(date).append(" ").append(ist.getVcORG()).append("\n");
        }
        if (answer.length() > 0) {
            sendMessageWithKeyboard(chatId, "Запросы:\n" + answer);
        }

    }

    private void showEmployeeInfo(String query, long chatId) {
        List<InfozaUser> users;
        users = infozaUserRepository.findByVcLNAAndInTIPGreaterThan(query, 1);
        if (users.isEmpty()) {
            users = infozaUserRepository.findByVcORGContainingAndInTIPGreaterThanAndInLSTOrderByVcLNA(query, 1, 1);
        }
        if (users.isEmpty()) {
            sendMessageWithKeyboard(chatId, "Информация не найдена");
        }
        for (InfozaUser user : users) {
            InfozaIst info = infozaIstRepository.findInfozaIstByVcUSR(user.getVcUSR());

            String phone = info.getVcSOT();

            StringBuilder formattedPhone;
            if (!phone.isEmpty()) {
                formattedPhone = new StringBuilder("Телефон: ");
                if (phone.contains(",")) {
                    String[] split = phone.split(",");
                    for (String s : split) {
                        formattedPhone.append(formatPhoneNumber(s)).append(" ");
                    }
                } else {
                    formattedPhone.append(formatPhoneNumber(phone));
                }
            } else {
                formattedPhone = new StringBuilder("Телефон не указан");
            }

            String answer = info.getVcFIO() + "\n" +
                    info.getVcORG() + "\n" +
                    formattedPhone;
            sendMessageWithKeyboard(chatId, answer);
        }
    }

    private static InlineKeyboardMarkup getInlineKeyboardMarkupWithCancelButton() {
        // Создаем инлайн-клавиатуру
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        // Создаем кнопку "Отмена" с колбэк-данными "cancel"
        InlineKeyboardButton cancelButton = new InlineKeyboardButton("Отмена");
        cancelButton.setCallbackData(CANCEL_BUTTON);

        // Добавляем кнопку в строку и строку в клавиатуру
        row.add(cancelButton);
        rows.add(row);

        // Устанавливаем клавиатуру для сообщения
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    private void registerUser(Message message, InfozaUser infoUser, Long idIST) {
        if (botUserRepository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            BotUser botUser = new BotUser();

            botUser.setChatId(chatId);
            botUser.setFirstName(chat.getFirstName());
            botUser.setLastName(chat.getLastName());
            botUser.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            botUser.setTip(infoUser.getInTIP());
            botUser.setGrp(infoUser.getInGRP());
            botUser.setIst(idIST.intValue());

            botUserRepository.save(botUser);

            log.info("user saved: " + botUser);
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Добрый день, " + name + "! \nДобро пожаловать!" + " :blush:");
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(Long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        executeMessage(message);
    }

    private void sendMessageWithKeyboard(Long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        message.setParseMode("html");
        message.setReplyMarkup(mainFuncKeyboardMarkup());
        executeMessage(message);
    }

    private void sendMessageWithRemoveKeyboard(Long chatId, String messageText){
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        message.setReplyMarkup(replyKeyboardRemove);

        executeMessage(message);
    }

    private void sendMessageWithKeyboard(Long chatId, String messageText, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        message.setReplyMarkup(keyboardMarkup);
        executeMessage(message);
    }

    private void sendMessageWithKeyboard(Long chatId, String messageText, InlineKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);

        message.setReplyMarkup(keyboardMarkup);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private boolean isUserRegistered(Long chatId) {
        return botUserRepository.findById(chatId).isPresent();
    }

    private void sendRequestContactMessage(long chatId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true); // Клавиатура скроется после использования

        // Create a list of keyboard rows
        List<KeyboardRow> keyboard = new ArrayList<>();

        // First keyboard row
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardButton button = new KeyboardButton("Разрешить");
        button.setRequestContact(true); // Запросить номер телефона у пользователя

        // Add buttons to the first keyboard row
        keyboardFirstRow.add(button);

        // Add all of the keyboard rows to the list
        keyboard.add(keyboardFirstRow);
        // and assign this list to our keyboard
        replyKeyboardMarkup.setKeyboard(keyboard);
        sendMessageWithKeyboard(chatId, "Вы не авторизованы. Пожалуйста, предоставьте боту доступ к Вашему номеру телефона", replyKeyboardMarkup);
    }

    private ReplyKeyboardMarkup mainFuncKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(EMPLOYEES);
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(FLS);
        row.add(ULS);
        row.add(PHONES);

        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

}
