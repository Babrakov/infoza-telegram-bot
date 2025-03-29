package ru.infoza.bot.bot.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.infoza.bot.repository.bot.BotUserRepository;
import ru.infoza.bot.service.bot.BotService;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static ru.infoza.bot.util.BotMessages.*;
import static ru.infoza.bot.util.HelperUtils.isValidCar;
import static ru.infoza.bot.util.HelperUtils.replaceLatinWithCyrillic;
import static ru.infoza.bot.util.ServiceMessages.USER_REQUEST_LOG;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarHelper implements BotHelper {

    private final AsyncHelper asyncHelper;
    private final BotService botService;
    private final BotUserRepository botUserRepository;

    @Override
    public void showInfo(String query,
                         long chatId,
                         Integer messageToDelete,
                         Consumer<String> sendMessage,
                         Consumer<DeleteMessage> executeMessage,
                         Consumer<String> sendMessageWithKeyboard) {
        log.info(USER_REQUEST_LOG, chatId, query);

        String car = processCarNumber(replaceLatinWithCyrillic(query.toUpperCase()));

        if (!isValidCar(car)) {
            handleInvalidCar(chatId, messageToDelete, sendMessage, executeMessage, sendMessageWithKeyboard);
            return;
        }

        CompletableFuture<Integer> cloudFuture = asyncHelper.fetchCloudCarInfoAsync(car, sendMessage);

        CompletableFuture.anyOf(cloudFuture)
                .thenApply(result -> (Integer) result == 1)
                .thenAccept(success -> handleResult(chatId, success, messageToDelete, executeMessage,
                        sendMessageWithKeyboard));
    }

    private void handleInvalidCar(long chatId,
                                  Integer messageToDelete,
                                  Consumer<String> sendMessage,
                                  Consumer<DeleteMessage> executeMessage,
                                  Consumer<String> sendMessageWithKeyboard) {
        executeMessage.accept(new DeleteMessage(String.valueOf(chatId), messageToDelete));
        sendMessage.accept(INVALID_CAR_NUMBER);
        sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
    }

    private void handleResult(long chatId,
                              boolean success,
                              Integer messageToDelete,
                              Consumer<DeleteMessage> executeMessage,
                              Consumer<String> sendMessageWithKeyboard) {
        if (!success) {
            sendMessageWithKeyboard.accept(INFO_NOT_FOUND);
        } else {
            botService.findUserById(chatId).ifPresent(user -> {
                if (user.getTip() <= 3) {
                    user.setRemainCarReqs(user.getRemainCarReqs() - 1);
                    botUserRepository.save(user);
                }
            });
        }

        executeMessage.accept(new DeleteMessage(String.valueOf(chatId), messageToDelete));
        sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
    }

    private String processCarNumber(String input) {
        return (input != null && (input.length() == 11 || input.length() == 12) && input.endsWith("RUS"))
                ? input.substring(0, input.length() - 3)
                : input;
    }
}