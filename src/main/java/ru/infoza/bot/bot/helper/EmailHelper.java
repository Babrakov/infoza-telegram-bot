package ru.infoza.bot.bot.helper;

import static ru.infoza.bot.util.BotConstants.INFO_NOT_FOUND;
import static ru.infoza.bot.util.BotConstants.SEARCH_COMPLETE;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.infoza.bot.model.infoza.Email;
import ru.infoza.bot.model.infoza.EmailRequest;
import ru.infoza.bot.repository.bot.BotUserRepository;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.service.infoza.EmailService;
import ru.infoza.bot.util.EmailUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailHelper {

    private final BotService botService;
    private final BotUserRepository botUserRepository;
    private final AsyncHelper asyncHelper;
    private final EmailService emailService;

    public void showEmailInfo(String query, long chatId, Integer messageToDelete,
            Consumer<String> sendMessage, Consumer<DeleteMessage> executeMessage,
            Consumer<String> sendMessageWithKeyboard) {
        log.info("Запрос от {}: {}", chatId, query);

        String email = query.toLowerCase();
        if (!EmailUtils.isValidEmail(email)) {
            handleInvalidEmail(chatId, messageToDelete, sendMessage, executeMessage,
                    sendMessageWithKeyboard);
            return;
        }
        long currentUserIst = botService.getCurrentUserIst(chatId);
        saveEmailRequest(currentUserIst, email);
        CompletableFuture<Integer> cloudFuture = asyncHelper.fetchCloudEmailInfoAsync(email,
                sendMessage);
        cloudFuture.thenAccept(
                result -> processResult(result, chatId, messageToDelete, sendMessageWithKeyboard,
                        executeMessage));
    }

    private void saveEmailRequest(long currentUserIst, String email) {
        Email infozaEmail = emailService.getEmailByEmail(email);
        if (infozaEmail == null) {
            infozaEmail = new Email();
            infozaEmail.setEmail(email);
            emailService.saveEmail(infozaEmail);
        }

        EmailRequest emailRequest = getTodayRequestByIst(infozaEmail.getId(), currentUserIst);
        if (emailRequest == null) {
            emailRequest = new EmailRequest();
            emailRequest.setIdEmail(infozaEmail.getId());
            emailRequest.setInIst(currentUserIst);
            emailRequest.setCreatedAt(Instant.now());
            emailService.saveEmailRequest(emailRequest);
        }

    }

    private void handleInvalidEmail(long chatId, Integer messageToDelete,
            Consumer<String> sendMessage,
            Consumer<DeleteMessage> executeMessage, Consumer<String> sendMessageWithKeyboard) {
        executeMessage.accept(new DeleteMessage(String.valueOf(chatId), messageToDelete));
        sendMessage.accept("Указан невалидный email");
        sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
    }

    private void processResult(int result, long chatId, Integer messageToDelete,
            Consumer<String> sendMessageWithKeyboard,
            Consumer<DeleteMessage> executeMessage) {
        if (result != 1) {
            sendMessageWithKeyboard.accept(INFO_NOT_FOUND);
        } else {
            botService.findUserById(chatId).ifPresent(user -> {
                if (user.getTip() <= 3) {
                    user.setRemainEmailReqs(user.getRemainEmailReqs() - 1);
                    botUserRepository.save(user);
                }
            });
        }
        executeMessage.accept(new DeleteMessage(String.valueOf(chatId), messageToDelete));
        sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
    }

    private EmailRequest getTodayRequestByIst(Long id, Long ist) {
        // Получение текущей даты
        LocalDate today = LocalDate.now();
        // Преобразование в начало дня (00:00:00)
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        // Преобразование в конец дня (23:59:59)
        Instant endOfDay = today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
        return emailService.findByIdEmailAndInIstAndCreatedAtBetween(id, ist, startOfDay, endOfDay);
    }

}
