package ru.infoza.bot.bot.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.infoza.bot.model.RequestType;
import ru.infoza.bot.model.infoza.Email;
import ru.infoza.bot.model.infoza.EmailRequest;
import ru.infoza.bot.repository.bot.BotUserRepository;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.service.infoza.EmailService;
import ru.infoza.bot.util.EmailUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static ru.infoza.bot.util.BotMessages.*;
import static ru.infoza.bot.util.ServiceMessages.USER_REQUEST_LOG;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailHelper implements BotHelper {

    private final BotService botService;
    private final BotUserRepository botUserRepository;
    private final AsyncHelper asyncHelper;
    private final EmailService emailService;

    @Override
    public void showInfo(String query,
                         long chatId,
                         Integer messageToDelete,
                         Consumer<String> sendMessage,
                         Consumer<DeleteMessage> executeMessage,
                         Consumer<String> sendMessageWithKeyboard) {
        log.info(USER_REQUEST_LOG, chatId, query);

        String email = query.toLowerCase();
        if (!EmailUtils.isValidEmail(email)) {
            handleInvalidEmail(chatId, messageToDelete, sendMessage, executeMessage, sendMessageWithKeyboard);
            return;
        }

        Email infozaEmail = emailService.findOrCreateEmail(email);

        CompletableFuture<Integer> requestFuture = asyncHelper.fetchRequestInfoAsync(infozaEmail, sendMessage);
        CompletableFuture<Integer> cloudFuture = asyncHelper.fetchCloudEmailInfoAsync(email, sendMessage);

        CompletableFuture.allOf(requestFuture, cloudFuture).thenRun(() -> {
            boolean anySucceed = requestFuture.join() == 1 || cloudFuture.join() == 1;
            if (!anySucceed) {
                sendMessageWithKeyboard.accept(INFO_NOT_FOUND);
            } else {
                botService.decrementUserRequests(chatId, RequestType.PHONE);
            }
            executeMessage.accept(new DeleteMessage(String.valueOf(chatId), messageToDelete));
            sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
            long currentUserIst = botService.getCurrentUserIst(chatId);
            saveEmailRequest(currentUserIst, infozaEmail);
        });
    }

    private void saveEmailRequest(long currentUserIst, Email infozaEmail) {
        EmailRequest emailRequest = getTodayRequestByIst(infozaEmail.getId(), currentUserIst);
        if (emailRequest == null) {
            emailRequest = new EmailRequest();
            emailRequest.setIdEmail(infozaEmail.getId());
            emailRequest.setInIst(currentUserIst);
            emailRequest.setCreatedAt(Instant.now());
            emailService.saveEmailRequest(emailRequest);
        }

    }

    private void handleInvalidEmail(long chatId,
                                    Integer messageToDelete,
                                    Consumer<String> sendMessage,
                                    Consumer<DeleteMessage> executeMessage,
                                    Consumer<String> sendMessageWithKeyboard) {
        executeMessage.accept(new DeleteMessage(String.valueOf(chatId), messageToDelete));
        sendMessage.accept(INVALID_EMAIL);
        sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
    }

    private EmailRequest getTodayRequestByIst(Long id, Long ist) {
        Instant now = Instant.now();
        Instant startOfDay =
                now.atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        return emailService.findByIdEmailAndInIstAndCreatedAtAfter(id, ist, startOfDay);
    }

}
