package ru.infoza.bot.bot.helper;

import static ru.infoza.bot.bot.helper.HelperUtils.getFormattedDate;
import static ru.infoza.bot.bot.helper.HelperUtils.getRemark;
import static ru.infoza.bot.util.BotConstants.INFO_NOT_FOUND;
import static ru.infoza.bot.util.BotConstants.SEARCH_COMPLETE;
import static ru.infoza.bot.util.PhysicalPersonUtils.md5Hash;
import static ru.infoza.bot.util.PhysicalPersonUtils.resolvedInFls;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.infoza.bot.model.infoza.InfozaIst;
import ru.infoza.bot.model.infoza.InfozaPhysicalPersonRem;
import ru.infoza.bot.model.infoza.InfozaPhysicalPersonRequest;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.service.infoza.InfozaPhysicalPersonService;
import ru.infoza.bot.service.infoza.InfozaUserService;

@Slf4j
@Component
public class FlsHelper {

    private final InfozaPhysicalPersonService infozaPhysicalPersonService;
    private final InfozaUserService infozaUserService;
    private final BotService botService;


    public FlsHelper(InfozaPhysicalPersonService infozaPhysicalPersonService,
            InfozaUserService infozaUserService, BotService botService) {
        this.infozaPhysicalPersonService = infozaPhysicalPersonService;
        this.infozaUserService = infozaUserService;
        this.botService = botService;
    }

    public void showFlsInfo(String query, long chatId, Integer messageToDelete,
            Consumer<String> sendMessage, Consumer<DeleteMessage> executeMessage,
            Consumer<String> sendMessageWithKeyboard) {
        String hash = md5Hash(query.trim().toUpperCase());
        List<InfozaPhysicalPersonRem> physics = infozaPhysicalPersonService.findRemarkListByHash(
                hash);

        List<InfozaPhysicalPersonRequest> requests = infozaPhysicalPersonService.findRequestListByHash(
                hash);

        if (physics.isEmpty() && requests.isEmpty()) {
            sendMessage.accept(INFO_NOT_FOUND);
        }
        for (InfozaPhysicalPersonRem person : physics) {
            InfozaIst ist = infozaUserService.findIstById(person.getInIST()).orElseThrow();
            String info = person.getVcREM();
            Long inFls = person.getInFLS();
            if (inFls != 0) {
                info += "\n" + resolvedInFls(inFls);
            }
            String date = getFormattedDate(person.getDtCRE());
            String answer = getRemark(info, ist.getVcORG(), date);
            sendMessage.accept(answer);
        }

        StringBuilder answer = new StringBuilder();
        for (InfozaPhysicalPersonRequest request : requests) {
            InfozaIst ist = infozaUserService.findIstById(request.getInIST()).orElseThrow();

            String date = getFormattedDate(request.getDtCRE());

            answer.append(date).append(" ").append(ist.getVcORG()).append("\n");
        }
        if (answer.length() > 0) {
            sendMessage.accept("Запросы:\n" + answer);
        }
        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageToDelete);
        executeMessage.accept(deleteMessage);
        sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
        long currentUserIst = botService.getCurrentUserIst(chatId);
        saveFlsRequest(currentUserIst, hash, requests);
    }

    private void saveFlsRequest(long ist, String hash, List<InfozaPhysicalPersonRequest> requests) {
        if (requests.isEmpty()
                || infozaPhysicalPersonService.getTodayRequestByIst(hash, ist) == null) {
            // Создаем новый объект InfozaPhysicalPersonRequest
            InfozaPhysicalPersonRequest infozaPhysicalPersonRequest = new InfozaPhysicalPersonRequest();
            infozaPhysicalPersonRequest.setVcHASH(hash);
            infozaPhysicalPersonRequest.setInTIP(666); // 666 - иное
            infozaPhysicalPersonRequest.setInIST(ist);
            infozaPhysicalPersonRequest.setDtCRE(Instant.now());
            infozaPhysicalPersonService.saveInfozaPhysicalPersonRequest(
                    infozaPhysicalPersonRequest);
        }
    }

}
