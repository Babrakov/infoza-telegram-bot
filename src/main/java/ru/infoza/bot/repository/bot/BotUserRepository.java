package ru.infoza.bot.repository.bot;

import org.springframework.data.repository.CrudRepository;
import ru.infoza.bot.model.bot.BotUser;

public interface BotUserRepository extends CrudRepository<BotUser,Long> {
}
