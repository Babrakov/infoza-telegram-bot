package ru.infoza.bot.bot.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.infoza.bot.dto.EmailRequestDTO;
import ru.infoza.bot.dto.InfozaPhoneRequestDTO;
import ru.infoza.bot.model.infoza.Email;
import ru.infoza.bot.model.infoza.InfozaIst;
import ru.infoza.bot.model.infoza.InfozaPhone;
import ru.infoza.bot.model.infoza.InfozaPhoneRem;
import ru.infoza.bot.service.cldb.CldbCarService;
import ru.infoza.bot.service.cldb.CldbEmailService;
import ru.infoza.bot.service.cldb.CldbPhoneService;
import ru.infoza.bot.service.infoza.EmailService;
import ru.infoza.bot.service.infoza.InfozaPhoneService;
import ru.infoza.bot.service.infoza.InfozaUserService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ru.infoza.bot.util.BotMessages.*;
import static ru.infoza.bot.util.HelperUtils.getFormattedDate;
import static ru.infoza.bot.util.HelperUtils.getRemark;
import static ru.infoza.bot.util.ServiceMessages.ERROR_FETCHING_DATA;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncHelper {

    private final InfozaUserService infozaUserService;
    private final CldbPhoneService cldbPhoneService;
    private final CldbEmailService cldbEmailService;
    private final CldbCarService cldbCarService;
    private final InfozaPhoneService infozaPhoneService;
    private final EmailService emailService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public CompletableFuture<Integer> fetchRemInfoAsync(String formattedPhoneNumber, Consumer<String> sendMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<InfozaPhoneRem> phoneRems = infozaPhoneService.findRemarksByPhoneNumber(formattedPhoneNumber);

                String answer = phoneRems.stream()
                        .map(remark -> {
                            InfozaIst ist = infozaUserService.findIstById(remark.getInIST()).orElseThrow();
                            return getRemark(remark.getVcREM(), ist.getVcORG(), getFormattedDate(remark.getDtCRE()));
                        })
                        .collect(Collectors.joining());

                if (!answer.isEmpty()) {
                    sendMessage.accept(COMMENTS_HEADER + answer);
                }
                return 1;
            } catch (Exception e) {
                log.error(ERROR_TEXT, e.getMessage());
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

            if (!answer.isEmpty()) {
                sendMessage.accept(REQUESTS_HEADER + answer);
            }

            return 0;
        }, executorService);
    }

    public CompletableFuture<Integer> fetchRequestInfoAsync(Email email,
                                                            Consumer<String> sendMessage) {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder answer = new StringBuilder();

            List<EmailRequestDTO> requestList = emailService.findRequestListByEmail(
                    email.getEmail());
            for (EmailRequestDTO request : requestList) {
                answer.append(getFormattedDate(request.getDate())).append(" ")
                        .append(request.getOrg()).append("\n");
            }

            if (!answer.isEmpty()) {
                sendMessage.accept(REQUESTS_HEADER + answer);
            }

            return 0;
        }, executorService);
    }

    public CompletableFuture<Integer> fetchCloudInfoAsync(String formattedPhoneNumber, Consumer<String> sendMessage) {
        return fetchCloudInfoAsync(cldbPhoneService.getCloudPhoneInfo("7" + formattedPhoneNumber), sendMessage);
    }

    public CompletableFuture<Integer> fetchCloudEmailInfoAsync(String email, Consumer<String> sendMessage) {
        return fetchCloudInfoAsync(cldbEmailService.getCloudEmailInfo(email), sendMessage);
    }

    public CompletableFuture<Integer> fetchCloudCarInfoAsync(String car, Consumer<String> sendMessage) {
        return fetchCloudInfoAsync(cldbCarService.getCloudCarInfo(car), sendMessage);
    }

    private CompletableFuture<Integer> fetchCloudInfoAsync(Mono<String> cloudInfoMono,
                                                           Consumer<String> sendMessage) {
        return cloudInfoMono
                .doOnNext(cloudInfo -> {
                    if (!cloudInfo.isEmpty()) {
                        sendMessage.accept(CLOUD_DB_HEADER + cloudInfo);
                    }
                })
                .doOnError((e) -> log.error(ERROR_FETCHING_DATA, String.valueOf(e)))
                //                .doOnTerminate(() -> log.info("doOnTerminate"))
                .map(cloudInfo -> cloudInfo.isEmpty() ? 0 : 1)
                .onErrorReturn(0)
                .defaultIfEmpty(0)
                .toFuture();
    }

}
