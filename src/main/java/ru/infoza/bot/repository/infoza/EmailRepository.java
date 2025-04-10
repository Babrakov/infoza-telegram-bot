package ru.infoza.bot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.bot.model.infoza.Email;

import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {

    Optional<Email> findByEmail(String email);
}
