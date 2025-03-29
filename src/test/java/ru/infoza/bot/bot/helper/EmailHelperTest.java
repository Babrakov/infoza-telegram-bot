package ru.infoza.bot.bot.helper;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.infoza.bot.model.infoza.Email;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.service.infoza.EmailService;

@ExtendWith(MockitoExtension.class)
public class EmailHelperTest {

    private static final long CHAT_ID = 12345L;
    private static final Integer MESSAGE_ID = 6789;

    @InjectMocks
    private EmailHelper emailHelper;

    @Mock
    private BotService botService;

    @Mock
    private AsyncHelper asyncHelper;

    @Mock
    private EmailService emailService;

    @Mock
    private Consumer<String> sendMessage;

    @Mock
    private Consumer<DeleteMessage> executeMessage;

    @Mock
    private Consumer<String> sendMessageWithKeyboard;

    private Email createMockEmail(String email) {
        Email mockEmail = new Email();
        mockEmail.setEmail(email);
        return mockEmail;
    }

    private Instant[] getStartAndEndOfDay() {
        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
        return new Instant[]{startOfDay, endOfDay};
    }

    @Test
    public void testShowEmailInfo_Valid() {
        String email = "test@example.com";
        Email mockEmail = createMockEmail(email);

        when(emailService.getEmailByEmail(email)).thenReturn(mockEmail);
        when(botService.getCurrentUserIst(CHAT_ID)).thenReturn(CHAT_ID);

        Instant[] dayRange = getStartAndEndOfDay();
        when(emailService.findByIdEmailAndInIstAndCreatedAtBetween(mockEmail.getId(), CHAT_ID,
                dayRange[0], dayRange[1])).thenReturn(null);  // No records for today

        CompletableFuture<Integer> future = CompletableFuture.completedFuture(0);
        when(asyncHelper.fetchCloudEmailInfoAsync(email, sendMessage)).thenReturn(future);

        emailHelper.showInfo(email, CHAT_ID, MESSAGE_ID, sendMessage, executeMessage,
                sendMessageWithKeyboard);

        verify(executeMessage).accept(any(DeleteMessage.class));
        verify(sendMessageWithKeyboard).accept("Информация не найдена");
    }

    @Test
    public void testShowEmailInfo_Invalid() {
        String invalidEmail = "invalid-email";

        emailHelper.showInfo(invalidEmail, CHAT_ID, MESSAGE_ID, sendMessage, executeMessage,
                sendMessageWithKeyboard);

        verify(executeMessage).accept(any(DeleteMessage.class));
        verify(sendMessage).accept("Указан невалидный email");
        verify(sendMessageWithKeyboard).accept("Поиск завершен");
    }

    @Test
    public void testShowInfo_CloudRequestFailure() {
        String email = "test@example.com";
        Email mockEmail = createMockEmail(email);

        when(emailService.getEmailByEmail(email)).thenReturn(mockEmail);
        when(botService.getCurrentUserIst(CHAT_ID)).thenReturn(CHAT_ID);

        Instant[] dayRange = getStartAndEndOfDay();
        when(emailService.findByIdEmailAndInIstAndCreatedAtBetween(mockEmail.getId(), CHAT_ID,
                dayRange[0], dayRange[1])).thenReturn(null);  // No records for today

        CompletableFuture<Integer> future = CompletableFuture.completedFuture(0);
        when(asyncHelper.fetchCloudEmailInfoAsync(email, sendMessage)).thenReturn(future);

        emailHelper.showInfo(email, CHAT_ID, MESSAGE_ID, sendMessage, executeMessage,
                sendMessageWithKeyboard);

        verify(executeMessage).accept(any(DeleteMessage.class));
        verify(sendMessageWithKeyboard).accept("Информация не найдена");
    }
}
