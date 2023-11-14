package ru.infoza.bot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
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
import ru.infoza.bot.config.BotConfig;
import ru.infoza.bot.config.state.BotState;
import ru.infoza.bot.config.state.BotStateContext;
import ru.infoza.bot.dto.GetcontactDTO;
import ru.infoza.bot.dto.GrabContactDTO;
import ru.infoza.bot.dto.NumbusterDTO;
import ru.infoza.bot.model.bot.BotUser;
import ru.infoza.bot.model.infoza.*;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.service.infoza.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.infoza.bot.util.BotConstants.*;
import static ru.infoza.bot.util.JuridicalPersonUtils.isValidINN;
import static ru.infoza.bot.util.PhoneUtils.formatPhoneNumber;
import static ru.infoza.bot.util.PhoneUtils.formatPhoneNumberTenDigits;
import static ru.infoza.bot.util.PhysicalPersonUtils.md5Hash;
import static ru.infoza.bot.util.PhysicalPersonUtils.resolvedInFls;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final BotConfig config;
    private final BotStateContext botStateContext;
    private final BotService botService;
    private final InfozaPhoneService infozaPhoneService;
    private final InfozaUserService infozaUserService;
    private final InfozaPhysicalPersonService infozaPhysicalPersonService;
    private final InfozaJuridicalPersonService infozaJuridicalPersonService;
    private final InfozaBankService infozaBankService;


    public TelegramBot(BotConfig config, BotService botService, BotStateContext botStateContext,
                       InfozaPhoneService infozaPhoneService, InfozaUserService infozaUserService,
                       InfozaPhysicalPersonService infozaPhysicalPersonService,
                       InfozaJuridicalPersonService infozaJuridicalPersonService,
                       InfozaBankService infozaBankService) {
        this.config = config;
        this.infozaPhoneService = infozaPhoneService;
        this.botService = botService;
        this.infozaUserService = infozaUserService;
        this.infozaPhysicalPersonService = infozaPhysicalPersonService;
        this.infozaJuridicalPersonService = infozaJuridicalPersonService;
        this.botStateContext = botStateContext;
        this.infozaBankService = infozaBankService;

        List<BotCommand> listCommands = new ArrayList<>();
        listCommands.add(new BotCommand("/start", "Запустить бота"));
        listCommands.add(new BotCommand("/help", "Как пользоваться ботом"));
        listCommands.add(new BotCommand("/login", "Зарегистрироваться"));
        listCommands.add(new BotCommand("/main", "Показать основные команды"));
        listCommands.add(new BotCommand("/logout", "Выйти"));
        try {
            this.execute(new SetMyCommands(listCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка в настройке списка команд ботов: " + e.getMessage());
        }
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
                var users = botService.findUserList();
                for (BotUser botUser : users) {
                    sendMessage(botUser.getChatId(), textToSend);
                }
            } else if (botState != BotState.START) {
                if (messageText.equals("/cancel")) {
                    botStateContext.setUserState(chatId, BotState.START);
                    sendMessageWithKeyboard(chatId, CANCEL_REQUEST);
                } else if (update.hasMessage() && update.getMessage().hasText()) {
                    String query = update.getMessage().getText();
                    Integer messageId;

                    switch (botState) {
                        case WAITING_FOR_NAME_OR_COMPANY:
                            showEmployeeInfo(query, chatId);
                            break;
                        case WAITING_FOR_FLS:
                            messageId = sendMessageWithRemoveKeyboardAndGetId(chatId, SEARCH_START);
                            showFlsInfo(query, chatId, messageId);
                            break;
                        case WAITING_FOR_ULS:
                            messageId = sendMessageWithRemoveKeyboardAndGetId(chatId, SEARCH_START);
                            showUlsInfo(query, chatId, messageId);
                            break;
                        case WAITING_FOR_PHONE:
                            messageId = sendMessageWithRemoveKeyboardAndGetId(chatId, SEARCH_START);
                            showPhoneInfo(query, chatId, messageId);
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
                            botService.logout(update.getMessage().getChatId());
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
                        sendMessageWithKeyboard(chatId, "Введите фамилию или название компании для поиска:", inlineKeyboardMarkup);
                        break;
                    case FLS_BUTTON:
                        proceedExtendedAction(chatId, inlineKeyboardMarkup, BotState.WAITING_FOR_FLS, "Ф.И.О. год рождения");
                        break;
                    case ULS_BUTTON:
                        proceedExtendedAction(chatId, inlineKeyboardMarkup, BotState.WAITING_FOR_ULS, "ИНН");
                        break;
                    case PHONES_BUTTON:
                        proceedExtendedAction(chatId, inlineKeyboardMarkup, BotState.WAITING_FOR_PHONE, "№ телефона");
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
                sendMessageWithKeyboard(chatId, "Выберите команду");
            }
        } else if (update.hasMessage() && update.getMessage().hasContact()) {

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
                    InfozaUser infoUser = infozaUserService.findUserByUserName(results.get(0).getVcUSR());
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
        BotUser user = botService.findUserById(chatId).orElseThrow();
        return user.getTip() > 3;
    }

    private CompletableFuture<Integer> fetchIstInfoAsync(InfozaPhoneRem phone, long chatId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InfozaIst ist = infozaUserService.findIstById(phone.getInIST()).orElseThrow();
                String date = getFormattedDate(phone.getDtCRE());
                String answer = getRemark(phone.getVcREM(), ist.getVcORG(), date);
                sendMessage(chatId, answer);
                return 1;
            } catch (Exception e) {
                log.error(ERROR_TEXT + e.getMessage());
                return 0;
            }
        }, executorService);
    }

    private CompletableFuture<Integer> fetchSaveRuDataInfoAsync(String formattedPhoneNumber, long chatId) {
        return CompletableFuture.supplyAsync(() -> {
            String saveRuDataInfo = infozaPhoneService.getPhoneInfo(formattedPhoneNumber);
            if (!saveRuDataInfo.isEmpty()) {
                sendMessage(chatId, "SaveRuData:\n" + saveRuDataInfo);
                return 1;
            } else {
                return 0;
            }
        }, executorService);
    }

    private CompletableFuture<Integer> fetchGrabContactInfoAsync(String formattedPhoneNumber, long chatId) {
        return CompletableFuture.supplyAsync(() -> {
            List<GrabContactDTO> grabContactDTOList  = infozaPhoneService.getGrabcontactInfo(formattedPhoneNumber);
            List<NumbusterDTO> numbusterDTOList  = infozaPhoneService.getNumbusterInfo(7+formattedPhoneNumber);
            List<GetcontactDTO> getcontactDTOList  = infozaPhoneService.getGetcontactInfo(7+formattedPhoneNumber);
            if (!grabContactDTOList .isEmpty() || !numbusterDTOList.isEmpty() || !getcontactDTOList.isEmpty()) {
                StringBuilder messageBuilder = new StringBuilder("GetContact & NumBuster:\n");
                for (GrabContactDTO contactDTO : grabContactDTOList) {
                    messageBuilder
                            .append(contactDTO.getFio());
                    if (contactDTO.getBorn() != null && !contactDTO.getBorn().isEmpty()) {
                        // Assuming born is in the format "yyyy-MM-dd"
                        LocalDate birthDate = LocalDate.parse(contactDTO.getBorn());
                        messageBuilder
                                .append(" ")
                                .append(birthDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    }
                    messageBuilder.append("\n");
                }
                for (NumbusterDTO numbusterDTO : numbusterDTOList) {
                    messageBuilder
                            .append(numbusterDTO.getName())
                            .append("\n");
                }
                for (GetcontactDTO getcontactDTO : getcontactDTOList) {
                    messageBuilder
                            .append(getcontactDTO.getName())
                            .append("\n");
                }
                sendMessage(chatId, messageBuilder.toString());
                return 1;
            } else {
                return 0;
            }
        }, executorService);
    }

    private void showPhoneInfo(String query, long chatId, Integer messageToDelete) {
        List<CompletableFuture<Integer>> futures = new ArrayList<>();

        String formattedPhoneNumber = formatPhoneNumberTenDigits(query);
        if (formattedPhoneNumber.length() != 10) {
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageToDelete);
            executeMessage(deleteMessage);
            sendMessage(chatId, "Номер телефона не соответствует формату номеров РФ");
            sendMessageWithKeyboard(chatId, SEARCH_COMPLETE);
            return;
        }
        CompletableFuture<Integer> infoFuture = fetchSaveRuDataInfoAsync(formattedPhoneNumber, chatId);
        futures.add(infoFuture);

        List<InfozaPhoneRem> phones = infozaPhoneService.findRemarksByPhoneNumber(formattedPhoneNumber);

        InfozaPhone infozaPhone = infozaPhoneService.findPhoneByPhoneNumber(formattedPhoneNumber);


        for (InfozaPhoneRem phone : phones) {
            CompletableFuture<Integer> istFuture = fetchIstInfoAsync(phone, chatId);
            futures.add(istFuture);
        }

        if (infozaPhone != null) {
            CompletableFuture<Integer> requestFuture = fetchRequestInfoAsync(chatId, infozaPhone);
            futures.add(requestFuture);
        }

        CompletableFuture<Integer> grabContactFuture = fetchGrabContactInfoAsync(formattedPhoneNumber, chatId);
        futures.add(grabContactFuture);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        allOf.thenRun(() -> {
            boolean anySucceed = futures.stream().anyMatch(future -> future.join() == 1);

            if (phones.isEmpty() && !anySucceed) {
                sendMessageWithKeyboard(chatId, INFO_NOT_FOUND);
            }
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageToDelete);
            executeMessage(deleteMessage);
            sendMessageWithKeyboard(chatId, SEARCH_COMPLETE);
        });
    }

    private CompletableFuture<Integer> fetchRequestInfoAsync(long chatId, InfozaPhone infozaPhone) {
        return CompletableFuture.supplyAsync(() -> {

            List<InfozaPhoneRequest> phoneRequests = infozaPhoneService.findRequestsByPhoneId(infozaPhone.getId());
            StringBuilder answer = new StringBuilder();
            for (InfozaPhoneRequest request : phoneRequests) {
                InfozaIst ist = infozaUserService.findIstById(request.getInIST()).orElseThrow();
                String date = getFormattedDate(request.getDtCRE());
                answer.append(date).append(" ").append(ist.getVcORG()).append("\n");
            }
            if (answer.length() > 0) {
                sendMessage(chatId, "Запросы:\n" + answer);
            }

            return 1;
        }, executorService);
    }

    private static String getRemark(String rem, String ist, String date) {
        return rem +
                " \n(источник: <b>" +
                ist +
                "</b> " +
                date +
                ")";
    }

    private static String getFormattedDate(Instant phone) {
        String pattern = "dd.MM.yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(java.util.Date.from(phone));
    }

    private void showUlsInfo(String query, long chatId, Integer messageToDelete) {
        String inn = query.trim();
        if (isValidINN(inn)) {
            List<InfozaJuridicalPersonRem> orgs = infozaJuridicalPersonService.findRemarkListByINN(query);

            for (InfozaJuridicalPersonRem org : orgs) {
                InfozaIst ist = infozaUserService.findIstById(org.getInIST()).orElseThrow();

                String date = getFormattedDate(org.getDtCRE());
                String answer = getRemark(org.getVcREM(), ist.getVcORG(), date);
                sendMessage(chatId, answer);
            }

            List<InfozaJuridicalPersonAccount> accounts = infozaJuridicalPersonService.findAccountListByINN(inn);

            List<InfozaJuridicalPerson> infozaJuridicalPerson = infozaJuridicalPersonService.findJuridicalPersonByINN(inn);

            if (infozaJuridicalPerson.isEmpty() && accounts.isEmpty() && orgs.isEmpty()) {
                sendMessage(chatId, INFO_NOT_FOUND);
            }

            StringBuilder answer = new StringBuilder();
            for (InfozaJuridicalPerson person : infozaJuridicalPerson) {
                InfozaIst ist = infozaUserService.findIstById(person.getInIST()).orElseThrow();

                String date = getFormattedDate(person.getDtCRE());

                answer.append(date).append(" ").append(ist.getVcORG()).append("\n");

                List<InfozaJuridicalPersonRequest> requests = infozaJuridicalPersonService.findRequestListByPersonId(person.getId());
                for (InfozaJuridicalPersonRequest request : requests) {
                    InfozaIst requestIst = infozaUserService.findIstById(request.getInIST()).orElseThrow();
                    String requestDate = getFormattedDate(request.getDtCRE());
                    if (!requestIst.equals(ist) && !requestDate.equals(date))
                        answer.append(date).append(" ").append(ist.getVcORG()).append("\n");
                }

            }
            if (answer.length() > 0) {
                sendMessage(chatId, "Запросы:\n" + answer);
            }

            StringBuilder accountAnswer = new StringBuilder();
            for (InfozaJuridicalPersonAccount account : accounts) {
                InfozaBank bank = infozaBankService.findBankByBIK(account.getVcBIK());
                if (bank != null) {
                    String bankName = account.getVcBIK() + " " + bank.getVcNAZ();
                    if (bank.getDaDEL() != null) {
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
                sendMessage(chatId, "Счета:\n" + accountAnswer);
            }

        } else {
            sendMessage(chatId, "Указан несуществующий ИНН");
        }
        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageToDelete);
        executeMessage(deleteMessage);
        sendMessageWithKeyboard(chatId, SEARCH_COMPLETE);
    }

    private void showFlsInfo(String query, long chatId, Integer messageToDelete) {
        String hash = md5Hash(query.trim().toUpperCase());
        List<InfozaPhysicalPersonRem> physics = infozaPhysicalPersonService.findRemarkListByHash(hash);

        List<InfozaPhysicalPersonRequest> requests = infozaPhysicalPersonService.findRequestListByHash(hash);

        if (physics.isEmpty() && requests.isEmpty()) {
            sendMessage(chatId, INFO_NOT_FOUND);
        }
        for (InfozaPhysicalPersonRem person : physics) {
            InfozaIst ist = infozaUserService.findIstById(person.getInIST()).orElseThrow();
            String info = person.getVcREM();
            Long inFls = person.getInFLS();
            if (inFls != 0) {
                info += "\n" + resolvedInFls(inFls);
            }
            String date = getFormattedDate(person.getDtCRE());
            String answer = getRemark(info, ist.getVcORG(), date);
            sendMessage(chatId, answer);
        }

        StringBuilder answer = new StringBuilder();
        for (InfozaPhysicalPersonRequest request : requests) {
            InfozaIst ist = infozaUserService.findIstById(request.getInIST()).orElseThrow();

            String date = getFormattedDate(request.getDtCRE());

            answer.append(date).append(" ").append(ist.getVcORG()).append("\n");
        }
        if (answer.length() > 0) {
            sendMessage(chatId, "Запросы:\n" + answer);
        }
        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageToDelete);
        executeMessage(deleteMessage);
        sendMessageWithKeyboard(chatId, SEARCH_COMPLETE);
    }

    private void showEmployeeInfo(String query, long chatId) {
        List<InfozaUser> users;
        users = infozaUserService.findEnabledUserListByFullName(query);
        if (users.isEmpty()) {
            users = infozaUserService.findEnabledUserListByOrganisation(query);
        }
        if (users.isEmpty()) {
            sendMessageWithKeyboard(chatId, INFO_NOT_FOUND);
        }
        for (InfozaUser user : users) {
            InfozaIst info = infozaUserService.findIstByUserName(user.getVcUSR());

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

            String city =  (user.getVcSITY()!=null) ? user.getVcSITY() + "\n" : "";

            String answer = info.getVcFIO() + "\n" +
                    info.getVcORG() + "\n" +
                    city +
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
        if (botService.findUserById(message.getChatId()).isEmpty()) {
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

            botService.saveUser(botUser);

            log.info("Пользователь сохранен: " + botUser);
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Добро пожаловать, " + name + "! :blush: \nВыберите пункт меню");
        log.info("Отправлен ответ пользователю " + name);
        sendMessage(chatId, answer);
    }

    private void sendMessage(Long chatId, String messageText) {

        int maxMessageLength = 4095;
        if (messageText.length() <= maxMessageLength) {
            // Отправляем сообщение целиком, так как оно короче 4096 символов
            sendChunk(chatId, messageText);
        } else {
            // Разбиваем сообщение на куски по 4095 символов и отправляем их поочередно
            for (int i = 0; i < messageText.length(); i += maxMessageLength) {
                int endIndex = Math.min(i + maxMessageLength, messageText.length());
                String chunk = messageText.substring(i, endIndex);
                sendChunk(chatId, chunk);
            }
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
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        message.setParseMode("html");
        message.setReplyMarkup(mainFunctionsKeyboardMarkup());
        executeMessage(message);

    }

    private void sendMessageWithRemoveKeyboard(Long chatId, String messageText) {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        message.setReplyMarkup(replyKeyboardRemove);

        executeMessage(message);
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

    private void executeMessage(DeleteMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private boolean isUserRegistered(Long chatId) {
        return botService.findUserById(chatId).isPresent();
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

        // Add all the keyboard rows to the list
        keyboard.add(keyboardFirstRow);
        // and assign this list to our keyboard
        replyKeyboardMarkup.setKeyboard(keyboard);
        sendMessageWithKeyboard(chatId, ASK_PHONE, replyKeyboardMarkup);
    }

    private ReplyKeyboardMarkup mainFunctionsKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(EMPLOYEES_BUTTON);
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(FLS_BUTTON);
        row.add(ULS_BUTTON);
        row.add(PHONES_BUTTON);

        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

}
