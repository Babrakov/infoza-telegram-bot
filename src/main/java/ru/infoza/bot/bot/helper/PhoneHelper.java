package ru.infoza.bot.bot.helper;

import static ru.infoza.bot.util.BotConstants.INFO_NOT_FOUND;
import static ru.infoza.bot.util.BotConstants.SEARCH_COMPLETE;
import static ru.infoza.bot.util.PhoneUtils.formatPhoneNumberTenDigits;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.infoza.bot.model.bot.BotUser;
import ru.infoza.bot.model.infoza.InfozaPhone;
import ru.infoza.bot.model.infoza.InfozaPhoneRem;
import ru.infoza.bot.model.infoza.InfozaPhoneRequest;
import ru.infoza.bot.repository.bot.BotUserRepository;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.service.infoza.InfozaPhoneService;

@Slf4j
@Component
public class PhoneHelper {

    private final AsyncHelper asyncHelper;
    private final InfozaPhoneService infozaPhoneService;
    private final BotUserRepository botUserRepository;
    private final BotService botService;

    public PhoneHelper(AsyncHelper asyncHelper, InfozaPhoneService infozaPhoneService,
            BotUserRepository botUserRepository, BotService botService) {
        this.asyncHelper = asyncHelper;
        this.infozaPhoneService = infozaPhoneService;
        this.botUserRepository = botUserRepository;
        this.botService = botService;
    }

    public void showPhoneInfo(String query, long chatId, Integer messageToDelete,
            Consumer<String> sendMessage,Consumer<DeleteMessage> executeMessage,
            Consumer<String> sendMessageWithKeyboard) {
        log.info("Запрос от " + chatId + ": " + query);
        List<CompletableFuture<Integer>> futures = new ArrayList<>();

        String formattedPhoneNumber = formatPhoneNumberTenDigits(query);
        if (formattedPhoneNumber.length() != 10) {
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId),
                    messageToDelete);
            executeMessage.accept(deleteMessage);
            sendMessage.accept("Номер телефона не соответствует формату номеров РФ");
            sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
            return;
        }
        CompletableFuture<Integer> infoFuture = asyncHelper.fetchSaveRuDataInfoAsync(
                formattedPhoneNumber,
                sendMessage);
        futures.add(infoFuture);

        CompletableFuture<Integer> cloudFuture = asyncHelper.fetchCloudInfoAsync(
                formattedPhoneNumber, sendMessage);
        futures.add(cloudFuture);

        List<InfozaPhoneRem> phones = infozaPhoneService.findRemarksByPhoneNumber(
                formattedPhoneNumber);

        InfozaPhone infozaPhone = infozaPhoneService.findPhoneByPhoneNumber(formattedPhoneNumber);

        for (InfozaPhoneRem phone : phones) {
            CompletableFuture<Integer> istFuture = asyncHelper
                    .fetchIstInfoAsync(phone, chatId, sendMessage);
            futures.add(istFuture);
        }

        if (infozaPhone != null) {
            CompletableFuture<Integer> requestFuture = asyncHelper
                    .fetchRequestInfoAsync(infozaPhone, sendMessage);
            futures.add(requestFuture);
        }

        CompletableFuture<Integer> grabContactFuture = asyncHelper.fetchGrabContactInfoAsync(
                formattedPhoneNumber, sendMessage);
        futures.add(grabContactFuture);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        allOf.thenRun(() -> {
            boolean anySucceed = futures.stream().anyMatch(future -> future.join() == 1);

            if (phones.isEmpty() && !anySucceed) {
                sendMessageWithKeyboard.accept(INFO_NOT_FOUND);
            } else {
                BotUser user = botService.findUserById(chatId).orElseThrow();
                if (user.getTip() <= 3) {
                    user.setRemainPhoneReqs(user.getRemainPhoneReqs() - 1);
                    botUserRepository.save(user);
                }
            }
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId),
                    messageToDelete);
            executeMessage.accept(deleteMessage);
            sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
            long currentUserIst = botService.getCurrentUserIst(chatId);
            savePhoneRequest(currentUserIst, formattedPhoneNumber, infozaPhone);
        });

    }

    private void savePhoneRequest(long ist, String formattedPhoneNumber, InfozaPhone infozaPhone) {
        Instant date = Instant.now(); // Устанавливаем текущую дату и время

        if (infozaPhone == null) {
            // Создаем новый объект InfozaPhone
            infozaPhone = new InfozaPhone();
            infozaPhone.setVcPHO(formattedPhoneNumber);
            infozaPhone.setInIST(ist);
            infozaPhone.setDtCRE(date);
            infozaPhoneService.saveInfozaPhone(infozaPhone);
        }

        InfozaPhoneRequest infozaPhoneRequest = infozaPhoneService
                .getTodayRequestByIst(infozaPhone.getId(), ist);
        if (infozaPhoneRequest == null) {
            // Создаем новый объект InfozaPhoneRequest
            infozaPhoneRequest = new InfozaPhoneRequest();
            // Заполняем поля объекта InfozaPhoneRequest
            infozaPhoneRequest.setIdZZ(0L); // связь с запросом в z_zap, в данном случае 0
            infozaPhoneRequest.setIdZP(infozaPhone.getId());
            infozaPhoneRequest.setInTIP(0L); // мобильный/стационарный - опять же не знаем
            infozaPhoneRequest.setInIST(ist);
            infozaPhoneRequest.setDtCRE(date);

            infozaPhoneService.saveInfozaPhoneRequest(infozaPhoneRequest);
        }
    }

}
