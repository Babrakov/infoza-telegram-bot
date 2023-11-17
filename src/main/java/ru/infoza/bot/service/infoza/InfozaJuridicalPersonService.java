package ru.infoza.bot.service.infoza;

import org.springframework.stereotype.Service;
import ru.infoza.bot.model.infoza.*;
import ru.infoza.bot.repository.infoza.InfozaJuridicalPersonAccountRepository;
import ru.infoza.bot.repository.infoza.InfozaJuridicalPersonRemRepository;
import ru.infoza.bot.repository.infoza.InfozaJuridicalPersonRepository;
import ru.infoza.bot.repository.infoza.InfozaJuridicalPersonRequestRepository;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class InfozaJuridicalPersonService {

    private final InfozaJuridicalPersonRepository infozaJuridicalPersonRepository;
    private final InfozaJuridicalPersonRemRepository infozaJuridicalPersonRemRepository;
    private final InfozaJuridicalPersonRequestRepository infozaJuridicalPersonRequestRepository;
    private final InfozaJuridicalPersonAccountRepository infozaJuridicalPersonAccountRepository;

    public InfozaJuridicalPersonService(InfozaJuridicalPersonRepository infozaJuridicalPersonRepository, InfozaJuridicalPersonRemRepository infozaJuridicalPersonRemRepository, InfozaJuridicalPersonRequestRepository infozaJuridicalPersonRequestRepository, InfozaJuridicalPersonAccountRepository infozaJuridicalPersonAccountRepository) {
        this.infozaJuridicalPersonRepository = infozaJuridicalPersonRepository;
        this.infozaJuridicalPersonRemRepository = infozaJuridicalPersonRemRepository;
        this.infozaJuridicalPersonRequestRepository = infozaJuridicalPersonRequestRepository;
        this.infozaJuridicalPersonAccountRepository = infozaJuridicalPersonAccountRepository;
    }

    public List<InfozaJuridicalPerson> findJuridicalPersonByINN(String inn) {
        return infozaJuridicalPersonRepository.findByVcINN(inn);
    }

    public List<InfozaJuridicalPersonAccount> findAccountListByINN(String inn) {
        return infozaJuridicalPersonAccountRepository.findByVcINN(inn);
    }

    public List<InfozaJuridicalPersonRequest> findRequestListByPersonId(Long id) {
        return infozaJuridicalPersonRequestRepository.findByIdZO(id);
    }

    public List<InfozaJuridicalPersonRem> findRemarkListByINN(String query) {
        return infozaJuridicalPersonRemRepository.findByVcINN(query);
    }

    @Transactional
    public void saveJuridicalPerson(InfozaJuridicalPerson infozaJuridicalPerson) {
        infozaJuridicalPersonRepository.save(infozaJuridicalPerson);
    }

//    public InfozaJuridicalPersonRequest getTodayRequestByIst(Long id, long ist) {
//        // Получение текущей даты
//        LocalDate today = LocalDate.now();
//        // Преобразование в начало дня (00:00:00)
//        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
//        // Преобразование в конец дня (23:59:59)
//        Instant endOfDay = today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
//        return infozaJuridicalPersonRequestRepository.findByIdZOAndInISTAndDtCREBetween(id,ist,startOfDay,endOfDay);
//    }

}
