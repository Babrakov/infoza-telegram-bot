package ru.infoza.bot.bot.helper;

import static ru.infoza.bot.bot.helper.HelperUtils.getFormattedDate;
import static ru.infoza.bot.bot.helper.HelperUtils.getRemark;
import static ru.infoza.bot.util.BotConstants.ERROR_TEXT;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.infoza.bot.dto.GetcontactDTO;
import ru.infoza.bot.dto.GrabContactDTO;
import ru.infoza.bot.dto.InfozaPhoneRequestDTO;
import ru.infoza.bot.dto.NumbusterDTO;
import ru.infoza.bot.model.infoza.InfozaIst;
import ru.infoza.bot.model.infoza.InfozaPhone;
import ru.infoza.bot.model.infoza.InfozaPhoneRem;
import ru.infoza.bot.service.cldb.CldbCarService;
import ru.infoza.bot.service.cldb.CldbEmailService;
import ru.infoza.bot.service.infoza.InfozaPhoneService;
import ru.infoza.bot.service.infoza.InfozaUserService;

@Slf4j
@Component
public class AsyncHelper {

    private final InfozaUserService infozaUserService;
    private final CldbEmailService cldbEmailService;
    private final CldbCarService cldbCarService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final InfozaPhoneService infozaPhoneService;

    public AsyncHelper(InfozaUserService infozaUserService, CldbEmailService cldbEmailService,
            CldbCarService cldbCarService, InfozaPhoneService infozaPhoneService) {
        this.infozaUserService = infozaUserService;
        this.cldbEmailService = cldbEmailService;
        this.cldbCarService = cldbCarService;
        this.infozaPhoneService = infozaPhoneService;
    }

    public CompletableFuture<Integer> fetchIstInfoAsync(
            InfozaPhoneRem phone, long chatId, Consumer<String> sendMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InfozaIst ist = infozaUserService.findIstById(phone.getInIST()).orElseThrow();
                String date = getFormattedDate(phone.getDtCRE());
                String answer = getRemark(phone.getVcREM(), ist.getVcORG(), date);
                sendMessage.accept(answer);  // Вызываем sendMessage с передачей ответа
                return 1;
            } catch (Exception e) {
                log.error(ERROR_TEXT + e.getMessage());
                return 0;
            }
        }, executorService);
    }

    public CompletableFuture<Integer> fetchRequestInfoAsync(InfozaPhone infozaPhone,
            Consumer<String> sendMessage) {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder answer = new StringBuilder();

            List<InfozaPhoneRequestDTO> infozaPhoneRequestShortList = infozaPhoneService.findRequestListByPhone(
                    infozaPhone.getVcPHO());
            for (InfozaPhoneRequestDTO shortRequest : infozaPhoneRequestShortList) {
                answer.append(getFormattedDate(shortRequest.getDtCRE())).append(" ")
                        .append(shortRequest.getVcORG()).append("\n");
            }

            if (answer.length() > 0) {
                sendMessage.accept("<strong>Запросы</strong>\n" + answer);
            }

            return 0;
        }, executorService);
    }

    public CompletableFuture<Integer> fetchSaveRuDataInfoAsync(String formattedPhoneNumber,
            Consumer<String> sendMessage) {
        return CompletableFuture.supplyAsync(() -> {
            String saveRuDataInfo = infozaPhoneService.getPhoneInfo(formattedPhoneNumber);
            if (!saveRuDataInfo.isEmpty()) {
                sendMessage.accept("<strong>SaveRuData</strong>\n" + saveRuDataInfo);

                return 1;
            } else {
                return 0;
            }
        }, executorService);
    }

    public CompletableFuture<Integer> fetchCloudInfoAsync(String formattedPhoneNumber,
            Consumer<String> sendMessage) {
        return CompletableFuture.supplyAsync(() -> {
            String cloudInfo = infozaPhoneService.getCloudPhoneInfo("7" + formattedPhoneNumber);
            if (!cloudInfo.isEmpty()) {
                sendMessage.accept("<strong>CloudDB</strong>\n" + cloudInfo);
                return 1;
            } else {
                return 0;
            }
        }, executorService);
    }

    public CompletableFuture<Integer> fetchGrabContactInfoAsync(String formattedPhoneNumber,
            Consumer<String> sendMessage) {
        return CompletableFuture.supplyAsync(() -> {
            List<GrabContactDTO> grabContactDTOList = infozaPhoneService.getGrabContactInfo(
                    formattedPhoneNumber);
            List<NumbusterDTO> numbusterDTOList = infozaPhoneService.getNumbusterInfo(
                    7 + formattedPhoneNumber);
            List<GetcontactDTO> getcontactDTOList = infozaPhoneService.getGetcontactInfo(
                    7 + formattedPhoneNumber);
            if (!grabContactDTOList.isEmpty() || !numbusterDTOList.isEmpty()
                    || !getcontactDTOList.isEmpty()) {
                StringBuilder messageBuilder = new StringBuilder(
                        "<strong>GetContact & NumBuster</strong>\n");
                for (GrabContactDTO contactDTO : grabContactDTOList) {
                    messageBuilder
                            .append(contactDTO.getFio());
                    if (contactDTO.getBorn() != null && !contactDTO.getBorn().isEmpty()) {
                        // Assuming born is in the format "yyyy-MM-dd"
                        LocalDate birthDate = LocalDate.parse(contactDTO.getBorn());
                        messageBuilder
                                .append(" ")
                                .append(birthDate.format(
                                        DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    }
                    messageBuilder.append("\n");
                }
                for (NumbusterDTO numbusterDTO : numbusterDTOList) {
                    messageBuilder
                            .append(numbusterDTO.getName())
                            .append("\n");
                }
                for (GetcontactDTO getcontactDTO : getcontactDTOList) {
                    messageBuilder
                            .append(getcontactDTO.getName())
                            .append("\n");
                }
                sendMessage.accept(messageBuilder.toString());
                return 1;
            } else {
                return 0;
            }
        }, executorService);
    }


    public CompletableFuture<Integer> fetchCloudEmailInfoAsync(String email,
            Consumer<String> sendMessage) {
        return CompletableFuture.supplyAsync(() -> {
            String cloudInfo = cldbEmailService.getCloudEmailInfo(email);
            if (!cloudInfo.isEmpty()) {
                sendMessage.accept("<strong>CloudDB</strong>\n" + cloudInfo);
                return 1;
            } else {
                return 0;
            }
        }, executorService);
    }

    public CompletableFuture<Integer> fetchCloudCarInfoAsync(String car,
            Consumer<String> sendMessage) {
        return CompletableFuture.supplyAsync(() -> {
            String cloudInfo = cldbCarService.getCloudCarInfo(car);
            if (!cloudInfo.isEmpty()) {
                sendMessage.accept("<strong>CloudDB</strong>\n" + cloudInfo);
                return 1;
            } else {
                return 0;
            }
        }, executorService);
    }

}
