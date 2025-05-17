package ru.infoza.bot.bot.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.infoza.bot.model.infoza.InfozaIst;
import ru.infoza.bot.model.infoza.InfozaPhysicalPersonRem;
import ru.infoza.bot.model.infoza.InfozaPhysicalPersonRequest;
import ru.infoza.bot.service.bot.BotService;
import ru.infoza.bot.service.infoza.InfozaPhysicalPersonService;
import ru.infoza.bot.service.infoza.InfozaUserService;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import static ru.infoza.bot.util.BotMessages.*;
import static ru.infoza.bot.util.HelperUtils.getFormattedDate;
import static ru.infoza.bot.util.HelperUtils.getRemark;
import static ru.infoza.bot.util.PhysicalPersonUtils.md5Hash;
import static ru.infoza.bot.util.PhysicalPersonUtils.resolvedInFls;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlsHelper implements BotHelper {

    private final InfozaPhysicalPersonService infozaPhysicalPersonService;
    private final InfozaUserService infozaUserService;
    private final BotService botService;

    public void showInfo(String query,
                         long chatId,
                         Integer messageToDelete,
                         Consumer<String> sendMessage,
                         Consumer<DeleteMessage> executeMessage,
                         Consumer<String> sendMessageWithKeyboard) {
        String hash = md5Hash(query.trim().toUpperCase());
        List<InfozaPhysicalPersonRem> physics = infozaPhysicalPersonService.findRemarkListByHash(hash);

        List<InfozaPhysicalPersonRequest> requests = infozaPhysicalPersonService.findRequestListByHash(hash);

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
        if (!answer.isEmpty()) {
            sendMessage.accept(REQUESTS_HEADER + answer);
        }
        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageToDelete);
        executeMessage.accept(deleteMessage);
        sendMessageWithKeyboard.accept(SEARCH_COMPLETE);
        long currentUserIst = botService.getCurrentUserIst(chatId);
        if (currentUserIst != 773)
            saveFlsRequest(currentUserIst, hash, requests);
    }

    private void saveFlsRequest(long ist, String hash, List<InfozaPhysicalPersonRequest> requests) {
        if (requests.isEmpty() || infozaPhysicalPersonService.getTodayRequestByIst(hash, ist) == null) {
            InfozaPhysicalPersonRequest infozaPhysicalPersonRequest = new InfozaPhysicalPersonRequest();
            infozaPhysicalPersonRequest.setVcHASH(hash);
            infozaPhysicalPersonRequest.setInTIP(666); // 666 - иное
            infozaPhysicalPersonRequest.setInIST(ist);
            infozaPhysicalPersonRequest.setDtCRE(Instant.now());
            infozaPhysicalPersonService.saveInfozaPhysicalPersonRequest(infozaPhysicalPersonRequest);
        }
    }

}
