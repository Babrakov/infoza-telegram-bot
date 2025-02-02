package ru.infoza.bot.bot.helper;

import static ru.infoza.bot.bot.helper.HelperUtils.getFormattedDate;
import static ru.infoza.bot.bot.helper.HelperUtils.getRemark;
import static ru.infoza.bot.util.BotConstants.INFO_NOT_FOUND;
import static ru.infoza.bot.util.BotConstants.SEARCH_COMPLETE;
import static ru.infoza.bot.util.JuridicalPersonUtils.isValidINN;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.infoza.bot.model.infoza.InfozaBank;
import ru.infoza.bot.model.infoza.InfozaIst;
import ru.infoza.bot.model.infoza.InfozaJuridicalPerson;
import ru.infoza.bot.model.infoza.InfozaJuridicalPersonAccount;
import ru.infoza.bot.model.infoza.InfozaJuridicalPersonRem;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.service.infoza.InfozaBankService;
import ru.infoza.bot.service.infoza.InfozaJuridicalPersonService;
import ru.infoza.bot.service.infoza.InfozaUserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UlsHelper {

    private final InfozaJuridicalPersonService juridicalPersonService;
    private final InfozaBankService bankService;
    private final InfozaUserService userService;
    private final BotService botService;

    public void showUlsInfo(String query, long chatId, Integer messageToDelete,
            Consumer<String> sendMessage, Consumer<DeleteMessage> executeMessage,
            Consumer<String> sendMessageWithKeyboard) {
        String inn = query.trim();
        if (!isValidINN(inn)) {
            sendMessage.accept("Указан несуществующий ИНН");
            cleanupAfterProcessing(chatId, messageToDelete, executeMessage,
                    sendMessageWithKeyboard);
            return;
        }

        List<InfozaJuridicalPersonRem> remarks = juridicalPersonService.findRemarkListByINN(inn);
        remarks.forEach(org -> sendMessage.accept(
                getRemark(org.getVcREM(),
                        userService.findIstById(org.getInIST()).orElseThrow().getVcORG(),
                        getFormattedDate(org.getDtCRE()))));

        List<InfozaJuridicalPersonAccount> accounts = juridicalPersonService.findAccountListByINN(
                inn);
        List<InfozaJuridicalPerson> juridicalPersons = juridicalPersonService.findJuridicalPersonByINN(
                inn);

        if (juridicalPersons.isEmpty() && accounts.isEmpty() && remarks.isEmpty()) {
            sendMessage.accept(INFO_NOT_FOUND);
        } else {
            sendMessage.accept("Запросы:\n" + buildJuridicalPersonInfo(juridicalPersons));
            sendMessage.accept("Счета:\n" + buildAccountsInfo(accounts));
            saveUlsRequest(botService.getCurrentUserIst(chatId), inn, juridicalPersons);
        }
        cleanupAfterProcessing(chatId, messageToDelete, executeMessage, sendMessageWithKeyboard);
    }

    private void cleanupAfterProcessing(long chatId, Integer messageToDelete,
            Consumer<DeleteMessage> executeMessage, Consumer<String> sendMessageWithKeyboard) {
        executeMessage.accept(new DeleteMessage(String.valueOf(chatId), messageToDelete));
        sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
    }

    private String buildJuridicalPersonInfo(List<InfozaJuridicalPerson> juridicalPersons) {
        StringBuilder result = new StringBuilder();
        juridicalPersons.forEach(person -> {
            InfozaIst ist = userService.findIstById(person.getInIST()).orElseThrow();
            String date = getFormattedDate(person.getDtCRE());
            result.append(date).append(" ").append(ist.getVcORG()).append("\n");
            juridicalPersonService.findRequestListByPersonId(person.getId()).forEach(request -> {
                InfozaIst requestIst = userService.findIstById(request.getInIST()).orElseThrow();
                String requestDate = getFormattedDate(request.getDtCRE());
                if (!requestIst.equals(ist) && !requestDate.equals(date)) {
                    result.append(requestDate).append(" ").append(requestIst.getVcORG())
                            .append("\n");
                }
            });
        });
        return result.toString();
    }

    private String buildAccountsInfo(List<InfozaJuridicalPersonAccount> accounts) {
        StringBuilder result = new StringBuilder();
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2);

        accounts.forEach(account -> {
            InfozaBank bank = bankService.findBankByBIK(account.getVcBIK());
            if (bank != null) {
                String bankName = formatBankName(account.getVcBIK(), bank, account.getDCHE(),
                        sixMonthsAgo, twoMonthsAgo);
                result.append(bankName).append("\n");
            }
        });
        return result.toString();
    }

    private String formatBankName(String bik, InfozaBank bank, LocalDate accountDate,
            LocalDate sixMonthsAgo, LocalDate twoMonthsAgo) {
        String bankName = bik + " " + bank.getVcNAZ();
        if (bank.getDaDEL() != null) {
            bankName = "<s>" + bankName + "</s>";
        }
        if (accountDate.isAfter(sixMonthsAgo)) {
            bankName = "<b>" + bankName + "</b>";
        }
        if (accountDate.isAfter(twoMonthsAgo)) {
            bankName = "<u>" + bankName + "</u>";
        }
        return bankName;
    }

    private void saveUlsRequest(long ist, String inn,
            List<InfozaJuridicalPerson> juridicalPersons) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.DAYS);
        boolean existsToday = juridicalPersons.stream()
                .anyMatch(person -> person.getDtCRE().truncatedTo(ChronoUnit.DAYS).equals(now));
        // Если список пуст или нет элемента с dtCRE=сегодня, то добавляем новый объект
        if (!existsToday) {
            InfozaJuridicalPerson newPerson = new InfozaJuridicalPerson();
            newPerson.setVcINN(inn);
            newPerson.setInTIP(775);
            newPerson.setInIST(ist);
            newPerson.setDtCRE(now);
            juridicalPersonService.saveJuridicalPerson(newPerson);
        }
    }

}
