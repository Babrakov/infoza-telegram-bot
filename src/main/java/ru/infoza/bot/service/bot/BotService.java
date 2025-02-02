package ru.infoza.bot.service.bot;


import java.sql.Timestamp;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.infoza.bot.config.state.BotState;
import ru.infoza.bot.model.bot.BotUser;
import ru.infoza.bot.model.infoza.InfozaUser;
import ru.infoza.bot.repository.bot.BotUserRepository;

@Service
public class BotService {

    private final BotUserRepository botUserRepository;


    public BotService(BotUserRepository botUserRepository) {
        this.botUserRepository = botUserRepository;
    }

    public Iterable<BotUser> findUserList() {
        return botUserRepository.findAll();
    }

    public void logout(Long chatId) {
//        botUserRepository.findById(chatId).ifPresent(botUserRepository::delete);
        Optional<BotUser> optionalUser = botUserRepository.findById(chatId);
        if (optionalUser.isPresent()) {
            BotUser user = optionalUser.get();
            user.setDeletedAt(new Timestamp(System.currentTimeMillis()));
            botUserRepository.save(user);
        }
    }

    public Optional<BotUser> findUserById(Long chatId) {
        return botUserRepository.findById(chatId);
    }

    public void saveUser(BotUser botUser) {
        botUserRepository.save(botUser);
    }

    public boolean isUserRegistered(Long chatId) {
        return botUserRepository.findById(chatId)
                .map(user -> user.getDeletedAt() == null)
                .orElse(false);
    }

    public String registerUser(Message message, InfozaUser infoUser, Long idIST) {
        Optional<BotUser> userById = findUserById(message.getChatId());
        if (userById.isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            BotUser botUser = new BotUser();

            botUser.setChatId(chatId);
            botUser.setFirstName(chat.getFirstName());
            botUser.setLastName(chat.getLastName());
            botUser.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            botUser.setTip(infoUser.getInTIP());
            botUser.setGrp(infoUser.getInGRP());
            botUser.setIst(idIST.intValue());
            botUser.setRemainPhoneReqs(20);
            botUser.setRemainEmailReqs(20);
            botUser.setRemainCarReqs(20);
            saveUser(botUser);
            return "Пользователь сохранен: {}" + botUser;
        } else {
            BotUser botUser = userById.get();
            botUser.setDeletedAt(null);
            saveUser(botUser);
            return "Пользователь обновлен: {}" + botUser;
        }
    }

    public int checkIfAccessPermitted(Long chatId, BotState botState) {
        BotUser user = findUserById(chatId).orElseThrow();
        if (user.getTip() > 3) {
            return -1; // no restrictions
        } else {
            return getFreeRequestsCount(botState, user);

        }
    }

    private int getFreeRequestsCount(BotState botState, BotUser user) {
        int cnt = 0;
        switch (botState) {
            case WAITING_FOR_FLS:
                break;
            case WAITING_FOR_ULS:
                break;
            case WAITING_FOR_PHONE:
                cnt = user.getRemainPhoneReqs();
                break;
            case WAITING_FOR_EMAIL:
                cnt = user.getRemainEmailReqs();
                break;
            case WAITING_FOR_CAR:
                cnt = user.getRemainCarReqs();
                break;
        }
        return cnt;
    }

    public long getCurrentUserIst(Long chatId) {
        BotUser user = findUserById(chatId).orElseThrow();
        return user.getIst();
    }

}
