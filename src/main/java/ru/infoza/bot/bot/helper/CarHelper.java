package ru.infoza.bot.bot.helper;

import static ru.infoza.bot.bot.helper.HelperUtils.isValidCar;
import static ru.infoza.bot.bot.helper.HelperUtils.replaceLatinWithCyrillic;
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

@Slf4j
@Component
public class CarHelper {

    private final AsyncHelper asyncHelper;
    private final BotService botService;
    private final BotUserRepository botUserRepository;

    public CarHelper(AsyncHelper asyncHelper, BotService botService,
            BotUserRepository botUserRepository) {
        this.asyncHelper = asyncHelper;
        this.botService = botService;
        this.botUserRepository = botUserRepository;
    }

    public void showCarInfo(String query, long chatId, Integer messageToDelete,
            Consumer<String> sendMessage, Consumer<DeleteMessage> executeMessage,
            Consumer<String> sendMessageWithKeyboard) {
        log.info("Запрос от {}: {}", chatId, query);

        String car = processCarNumber(replaceLatinWithCyrillic(query.toUpperCase()));

        if (!isValidCar(car)) {
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId),
                    messageToDelete);
            executeMessage.accept(deleteMessage);
            sendMessage.accept("Указан невалидный номер авто");
            sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
            return;
        }

        List<CompletableFuture<Integer>> futures = new ArrayList<>();
        CompletableFuture<Integer> cloudFuture = asyncHelper.fetchCloudCarInfoAsync(car,
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
                    user.setRemainCarReqs(user.getRemainCarReqs() - 1);
                    botUserRepository.save(user);
                }
            }
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId),
                    messageToDelete);
            executeMessage.accept(deleteMessage);
            sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
        });

    }

    private String processCarNumber(String input) {
        if (input != null && (input.length() == 11 || input.length() == 12) && input.endsWith(
                "RUS")) {
            return input.substring(0, input.length() - 3);
        } else {
            return input;
        }
    }

}
