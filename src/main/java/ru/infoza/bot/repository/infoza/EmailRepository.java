package ru.infoza.bot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.bot.model.infoza.Email;

public interface EmailRepository extends JpaRepository<Email, Long> {

    Email findByEmail(String email);
}
