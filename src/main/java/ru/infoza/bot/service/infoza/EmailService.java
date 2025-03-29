package ru.infoza.bot.service.infoza;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.infoza.bot.dto.EmailRequestDTO;
import ru.infoza.bot.dto.InfozaPhoneRequestDTO;
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

    public EmailRequest findByIdEmailAndInIstAndCreatedAtAfter(Long id, Long inIst, Instant startOfDay){
        return emailRequestRepository.findByIdEmailAndInIstAndCreatedAtAfter(id, inIst, startOfDay);
    }

    @Transactional
    public void saveEmailRequest(EmailRequest emailRequest) {
        // Сохраняем объект в базе данных
        emailRequestRepository.save(emailRequest);
    }

    public Email getEmailByEmail(String email) {
        return emailRepository.findByEmail(email).orElse(null);
    }

    public Email findOrCreateEmail(String email) {
        return emailRepository.findByEmail(email)
                .orElseGet(() -> emailRepository.save(new Email(email)));
    }

    public List<EmailRequestDTO> findRequestListByEmail(String email) {
        List<Object[]> result = emailRequestRepository.findRequestListByEmail(email);
        List<EmailRequestDTO> emailRequestList = new ArrayList<>();

        for (Object[] row : result) {
            String org = (String) row[0];

            Timestamp timestamp = (Timestamp) row[1];
            Instant instant = timestamp.toInstant();
            LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            EmailRequestDTO dto = new EmailRequestDTO(org, date);
            emailRequestList.add(dto);
        }
        return emailRequestList;
    }


}
