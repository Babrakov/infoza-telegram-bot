package ru.infoza.bot.bot.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.infoza.bot.model.RequestType;
import ru.infoza.bot.model.infoza.InfozaPhone;
import ru.infoza.bot.model.infoza.InfozaPhoneRequest;
import ru.infoza.bot.repository.bot.BotUserRepository;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.service.infoza.InfozaPhoneService;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static ru.infoza.bot.util.BotMessages.*;
import static ru.infoza.bot.util.PhoneUtils.formatPhoneNumberTenDigits;
import static ru.infoza.bot.util.ServiceMessages.USER_REQUEST_LOG;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhoneHelper implements BotHelper {

    private final AsyncHelper asyncHelper;
    private final InfozaPhoneService infozaPhoneService;
    private final BotUserRepository botUserRepository;
    private final BotService botService;

    @Override
    public void showInfo(String query,
                         long chatId,
                         Integer messageToDelete,
                         Consumer<String> sendMessage,
                         Consumer<DeleteMessage> executeMessage,
                         Consumer<String> sendMessageWithKeyboard) {
        log.info(USER_REQUEST_LOG, chatId, query);

        String formattedPhoneNumber = formatPhoneNumberTenDigits(query);
        if (formattedPhoneNumber.length() != 10) {
            executeMessage.accept(new DeleteMessage(String.valueOf(chatId), messageToDelete));
            sendMessage.accept(INVALID_PHONE);
            sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
            return;
        }

        CompletableFuture<Integer> cloudFuture = asyncHelper.fetchCloudInfoAsync(formattedPhoneNumber, sendMessage);

        CompletableFuture<Integer> istFuture = asyncHelper.fetchRemInfoAsync(formattedPhoneNumber, sendMessage);

        InfozaPhone infozaPhone = infozaPhoneService.findPhoneByPhoneNumber(formattedPhoneNumber);
        CompletableFuture<Integer> requestFuture = infozaPhone != null
                ? asyncHelper.fetchRequestInfoAsync(infozaPhone, sendMessage)
                : CompletableFuture.completedFuture(0);

        CompletableFuture.allOf(cloudFuture, istFuture, requestFuture).thenRun(() -> {
            boolean anySucceed = cloudFuture.join() == 1 || istFuture.join() == 1 || requestFuture.join() == 1;

            if (!anySucceed) {
                sendMessageWithKeyboard.accept(INFO_NOT_FOUND);
            } else {
                botService.decrementUserRequests(chatId, RequestType.EMAIL);
            }
            executeMessage.accept(new DeleteMessage(String.valueOf(chatId), messageToDelete));
            sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
            long currentUserIst = botService.getCurrentUserIst(chatId);
            savePhoneRequest(currentUserIst, formattedPhoneNumber, infozaPhone);
        });

    }

    private void savePhoneRequest(long ist, String formattedPhoneNumber, InfozaPhone infozaPhone) {
        Instant date = Instant.now();

        if (infozaPhone == null) {
            infozaPhone = new InfozaPhone();
            infozaPhone.setVcPHO(formattedPhoneNumber);
            infozaPhone.setInIST(ist);
            infozaPhone.setDtCRE(date);
            infozaPhoneService.saveInfozaPhone(infozaPhone);
        }

        InfozaPhoneRequest infozaPhoneRequest = infozaPhoneService.getTodayRequestByIst(infozaPhone.getId(), ist);
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
