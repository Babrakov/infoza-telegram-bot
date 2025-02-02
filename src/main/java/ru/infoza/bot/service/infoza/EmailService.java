package ru.infoza.bot.service.infoza;

import java.time.Instant;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.infoza.bot.model.infoza.Email;
import ru.infoza.bot.model.infoza.EmailRequest;
import ru.infoza.bot.repository.infoza.EmailRepository;
import ru.infoza.bot.repository.infoza.EmailRequestRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailRepository emailRepository;
    private final EmailRequestRepository emailRequestRepository;

    @Transactional
    public void saveEmail(Email email) {
        emailRepository.save(email);
    }

    public EmailRequest findByIdEmailAndInIstAndCreatedAtBetween(Long id, Long inIst, Instant startOfDay, Instant endOfDay){
        return emailRequestRepository.findByIdEmailAndInIstAndCreatedAtBetween(id, inIst, startOfDay, endOfDay);
    }

    @Transactional
    public void saveEmailRequest(EmailRequest emailRequest) {
        // Сохраняем объект в базе данных
        emailRequestRepository.save(emailRequest);
    }

    public Email getEmailByEmail(String email) {
        return emailRepository.findByEmail(email);  // или аналогичный запрос для вашего репозитория
    }

}
