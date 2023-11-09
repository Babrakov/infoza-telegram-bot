package ru.infoza.simplebot.repository.bot;

import org.springframework.data.repository.CrudRepository;
import ru.infoza.simplebot.model.bot.BotUser;

public interface BotUserRepository extends CrudRepository<BotUser,Long> {
}
