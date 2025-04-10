package ru.infoza.bot.bot.helper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.infoza.bot.model.infoza.InfozaIst;
import ru.infoza.bot.model.infoza.InfozaJuridicalPerson;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.service.infoza.InfozaBankService;
import ru.infoza.bot.service.infoza.InfozaJuridicalPersonService;
import ru.infoza.bot.service.infoza.InfozaUserService;

class UlsHelperTest {

    @Mock
    private InfozaJuridicalPersonService juridicalPersonService;

    @Mock
    private InfozaBankService bankService;

    @Mock
    private InfozaUserService userService;

    @Mock
    private BotService botService;

    @Mock
    private Consumer<String> sendMessage;

    @Mock
    private Consumer<DeleteMessage> executeMessage;

    @Mock
    private Consumer<String> sendMessageWithKeyboard;

    @InjectMocks
    private UlsHelper ulsHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testShowInfoForInvalidINN() {
        String invalidInn = "123";
        ulsHelper.showInfo(invalidInn, 1L, 123, sendMessage, executeMessage,
                sendMessageWithKeyboard);
        verify(sendMessage).accept("Указан несуществующий ИНН");
        verifyNoMoreInteractions(juridicalPersonService, bankService, userService, botService);
    }

    @Test
    void testShowInfoForValidINNWithNoDataFound() {
        String validInn = "2311114445";
        when(juridicalPersonService.findRemarkListByINN(validInn)).thenReturn(List.of());
        when(juridicalPersonService.findAccountListByINN(validInn)).thenReturn(List.of());
        when(juridicalPersonService.findJuridicalPersonByINN(validInn)).thenReturn(List.of());

        ulsHelper.showInfo(validInn, 1L, 123, sendMessage, executeMessage,
                sendMessageWithKeyboard);

        verify(sendMessage).accept("Информация не найдена");
        verify(sendMessageWithKeyboard).accept("Поиск завершен");
    }

    @Test
    void testShowInfoForValidINNWithData() {
        String validInn = "2311114445";
        InfozaJuridicalPerson juridicalPerson = new InfozaJuridicalPerson();
        juridicalPerson.setVcINN(validInn);
        juridicalPerson.setDtCRE(Instant.now());
        when(juridicalPersonService.findJuridicalPersonByINN(validInn)).thenReturn(List.of(juridicalPerson));
        when(botService.getCurrentUserIst(anyLong())).thenReturn(1L);
        InfozaIst ist = new InfozaIst();
        when(userService.findIstById(any())).thenReturn(Optional.of(ist));

        ulsHelper.showInfo(validInn, 1L, 123, sendMessage, executeMessage,
                sendMessageWithKeyboard);

        verify(sendMessage, atLeastOnce()).accept(anyString());
        verify(sendMessageWithKeyboard).accept("Поиск завершен");
    }

}