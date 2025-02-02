package ru.infoza.bot.bot.helper;

import static ru.infoza.bot.util.BotConstants.INFO_NOT_FOUND;
import static ru.infoza.bot.util.BotConstants.SEARCH_COMPLETE;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.infoza.bot.model.bot.BotUser;
import ru.infoza.bot.repository.bot.BotUserRepository;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.util.EmailUtils;

@Slf4j
@Component
public class EmailHelper {

    private final BotService botService;
    private final BotUserRepository botUserRepository;
    private final AsyncHelper asyncHelper;

    public EmailHelper(BotService botService, BotUserRepository botUserRepository,
            AsyncHelper asyncHelper) {
        this.botService = botService;
        this.botUserRepository = botUserRepository;
        this.asyncHelper = asyncHelper;
    }

    public void showEmailInfo(String query, long chatId, Integer messageToDelete,
            Consumer<String> sendMessage, Consumer<DeleteMessage> executeMessage,
            Consumer<String> sendMessageWithKeyboard) {
        log.info("Запрос от {}: {}", chatId, query);

        String email = query.toLowerCase();

        if (!EmailUtils.isValidEmail(email)) {
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId),
                    messageToDelete);
            executeMessage.accept(deleteMessage);
            sendMessage.accept("Указан невалидный email");
            sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
            return;
        }

        List<CompletableFuture<Integer>> futures = new ArrayList<>();
        CompletableFuture<Integer> cloudFuture = asyncHelper.fetchCloudEmailInfoAsync(email,
                sendMessage);
        futures.add(cloudFuture);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        allOf.thenRun(() -> {
            boolean anySucceed = futures.stream().anyMatch(future -> future.join() == 1);

            if (!anySucceed) {
                sendMessageWithKeyboard.accept(INFO_NOT_FOUND);
            } else {
                BotUser user = botService.findUserById(chatId).orElseThrow();
                if (user.getTip() <= 3) {
                    user.setRemainEmailReqs(user.getRemainEmailReqs() - 1);
                    botUserRepository.save(user);
                }
            }
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId),
                    messageToDelete);
            executeMessage.accept(deleteMessage);
            sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
        });

    }

}
