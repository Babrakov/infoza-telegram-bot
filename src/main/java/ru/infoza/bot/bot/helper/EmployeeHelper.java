package ru.infoza.bot.bot.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import ru.infoza.bot.model.infoza.InfozaIst;
import ru.infoza.bot.model.infoza.InfozaUser;
import ru.infoza.bot.service.infoza.InfozaUserService;

import java.util.List;
import java.util.function.Consumer;

import static ru.infoza.bot.util.BotMessages.*;
import static ru.infoza.bot.util.PhoneUtils.formatPhoneNumber;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeHelper implements BotHelper {

    private final InfozaUserService infozaUserService;

    @Override
    public void showInfo(String query,
                         long chatId,
                         Integer messageToDelete,
                         Consumer<String> sendMessage,
                         Consumer<DeleteMessage> executeMessage,
                         Consumer<String> sendMessageWithKeyboard) {
        List<InfozaUser> users = getInfozaUsers(query);
        if (users.isEmpty()) {
            sendMessageWithKeyboard.accept(INFO_NOT_FOUND);
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();

        users.forEach(user -> {
            InfozaIst info = infozaUserService.findIstByUserName(user.getVcUSR());
            String phone = info.getVcSOT();

            StringBuilder formattedPhone = new StringBuilder(phone.isBlank() ? EMPLOYEE_NO_PHONE : EMPLOYEE_PHONE);

            if (!phone.isBlank()) {
                if (phone.contains(",")) {
                    for (String s : phone.split(",")) {
                        formattedPhone.append(formatPhoneNumber(s)).append(" ");
                    }
                } else {
                    formattedPhone.append(formatPhoneNumber(phone));
                }
            }

            String city = (user.getVcSITY() != null) ? user.getVcSITY() + "\n" : "";

            messageBuilder.append(info.getVcFIO()).append("\n")
                    .append(info.getVcORG()).append("\n")
                    .append(city)
                    .append(formattedPhone).append("\n\n");
        });

        sendMessageWithKeyboard.accept(messageBuilder.toString().trim());
    }

    private List<InfozaUser> getInfozaUsers(String query) {
        List<InfozaUser> users = infozaUserService.findEnabledUserListByFullName(query);
        return users.isEmpty() ? infozaUserService.findEnabledUserListByOrganisation(query) : users;
    }

}
