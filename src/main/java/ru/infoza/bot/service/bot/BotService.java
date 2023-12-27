package ru.infoza.bot.service.bot;


import org.springframework.stereotype.Service;
import ru.infoza.bot.model.bot.BotUser;
import ru.infoza.bot.repository.bot.BotUserRepository;

import java.sql.Timestamp;
import java.util.Optional;

@Service
public class BotService {

    private final BotUserRepository botUserRepository;


    public BotService(BotUserRepository botUserRepository) {
        this.botUserRepository = botUserRepository;
    }

    public Iterable<BotUser> findUserList(){
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
}
