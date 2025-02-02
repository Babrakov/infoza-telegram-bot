package ru.infoza.bot.bot.helper;

import static ru.infoza.bot.util.BotConstants.INFO_NOT_FOUND;
import static ru.infoza.bot.util.PhoneUtils.formatPhoneNumber;

import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.infoza.bot.model.infoza.InfozaIst;
import ru.infoza.bot.model.infoza.InfozaUser;
import ru.infoza.bot.service.infoza.InfozaUserService;

@Slf4j
@Component
public class EmployeeHelper {

    private final InfozaUserService infozaUserService;

    public EmployeeHelper(InfozaUserService infozaUserService) {
        this.infozaUserService = infozaUserService;
    }

    public void showEmployeeInfo(String query, long chatId,
            Consumer<String> sendMessageWithKeyboard) {
        List<InfozaUser> users;
        users = infozaUserService.findEnabledUserListByFullName(query);
        if (users.isEmpty()) {
            users = infozaUserService.findEnabledUserListByOrganisation(query);
        }
        if (users.isEmpty()) {
            sendMessageWithKeyboard.accept(INFO_NOT_FOUND);
        }
        for (InfozaUser user : users) {
            InfozaIst info = infozaUserService.findIstByUserName(user.getVcUSR());

            String phone = info.getVcSOT();

            StringBuilder formattedPhone;
            if (!phone.isEmpty()) {
                formattedPhone = new StringBuilder("Телефон: ");
                if (phone.contains(",")) {
                    String[] split = phone.split(",");
                    for (String s : split) {
                        formattedPhone.append(formatPhoneNumber(s)).append(" ");
                    }
                } else {
                    formattedPhone.append(formatPhoneNumber(phone));
                }
            } else {
                formattedPhone = new StringBuilder("Телефон не указан");
            }

            String city = (user.getVcSITY() != null) ? user.getVcSITY() + "\n" : "";

            String answer = info.getVcFIO() + "\n" +
                    info.getVcORG() + "\n" +
                    city +
                    formattedPhone;
            sendMessageWithKeyboard.accept(answer);
        }
    }

}
