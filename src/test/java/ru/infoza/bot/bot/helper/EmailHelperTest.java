package ru.infoza.bot.bot.helper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.infoza.bot.util.BotConstants.INFO_NOT_FOUND;
import static ru.infoza.bot.util.BotConstants.SEARCH_COMPLETE;

import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.infoza.bot.model.bot.BotUser;
import ru.infoza.bot.repository.bot.BotUserRepository;
import ru.infoza.bot.service.bot.BotService;

@ExtendWith(MockitoExtension.class)
class EmailHelperTest {

    private final long chatId = 12345L;
    private final int messageToDelete = 1;
    @Mock
    private BotService botService;
    @Mock
    private BotUserRepository botUserRepository;
    @Mock
    private AsyncHelper asyncHelper;
    @Mock
    private Consumer<String> sendMessage;
    @Mock
    private Consumer<DeleteMessage> executeMessage;
    @Mock
    private Consumer<String> sendMessageWithKeyboard;
    @InjectMocks
    private EmailHelper emailHelper;

    @BeforeEach
    void setUp() {
        lenient().when(asyncHelper.fetchCloudEmailInfoAsync(anyString(), any()))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(1));
    }

    @Test
    void shouldHandleInvalidEmail() {
        emailHelper.showEmailInfo("invalid_email", chatId, messageToDelete, sendMessage,
                executeMessage, sendMessageWithKeyboard);

        verify(sendMessage).accept("Указан невалидный email");
        verify(executeMessage).accept(any(DeleteMessage.class));
        verify(sendMessageWithKeyboard).accept(SEARCH_COMPLETE);
    }

    @Test
    void shouldProcessValidEmailAndDecreaseRequests() {
        BotUser user = new BotUser();
        user.setTip(3);
        user.setRemainEmailReqs(5);

        when(botService.findUserById(chatId)).thenReturn(Optional.of(user));
        emailHelper.showEmailInfo("test@example.com", chatId, messageToDelete, sendMessage,
                executeMessage, sendMessageWithKeyboard);

        verify(botUserRepository).save(user);
        verify(sendMessageWithKeyboard).accept(SEARCH_COMPLETE);
    }

    @Test
    void shouldHandleNotFoundEmailInfo() {
        when(asyncHelper.fetchCloudEmailInfoAsync(anyString(), any())).thenReturn(
                java.util.concurrent.CompletableFuture.completedFuture(0));

        emailHelper.showEmailInfo("notfound@example.com", chatId, messageToDelete, sendMessage,
                executeMessage, sendMessageWithKeyboard);

        verify(sendMessageWithKeyboard).accept(INFO_NOT_FOUND);
        verify(sendMessageWithKeyboard).accept(SEARCH_COMPLETE);
    }
}