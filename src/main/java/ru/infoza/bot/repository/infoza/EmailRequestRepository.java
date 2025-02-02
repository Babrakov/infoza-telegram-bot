package ru.infoza.bot.repository.infoza;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.bot.model.infoza.EmailRequest;

public interface EmailRequestRepository extends JpaRepository<EmailRequest, Long> {

    EmailRequest findByIdEmailAndInIstAndCreatedAtBetween(Long id, Long inIst, Instant startOfDay, Instant endOfDay);

}
