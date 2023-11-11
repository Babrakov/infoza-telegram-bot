package ru.infoza.simplebot.service.bot;


import org.springframework.stereotype.Service;
import ru.infoza.simplebot.model.bot.BotUser;
import ru.infoza.simplebot.repository.bot.BotUserRepository;

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
        botUserRepository.findById(chatId).ifPresent(botUserRepository::delete);
    }

    public Optional<BotUser> findUserById(Long chatId) {
        return botUserRepository.findById(chatId);
    }

    public void saveUser(BotUser botUser) {
        botUserRepository.save(botUser);
    }
}
