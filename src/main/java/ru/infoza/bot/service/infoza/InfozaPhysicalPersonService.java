package ru.infoza.bot.service.infoza;

import org.springframework.stereotype.Service;
import ru.infoza.bot.model.infoza.InfozaPhoneRequest;
import ru.infoza.bot.model.infoza.InfozaPhysicalPersonRem;
import ru.infoza.bot.model.infoza.InfozaPhysicalPersonRequest;
import ru.infoza.bot.repository.infoza.InfozaPhysicalPersonRemRepository;
import ru.infoza.bot.repository.infoza.InfozaPhysicalPersonRequestRepository;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class InfozaPhysicalPersonService {

    private final InfozaPhysicalPersonRemRepository infozaPhysicalPersonRemRepository;
    private final InfozaPhysicalPersonRequestRepository infozaPhysicalPersonRequestRepository;

    public InfozaPhysicalPersonService(InfozaPhysicalPersonRemRepository infozaPhysicalPersonRemRepository, InfozaPhysicalPersonRequestRepository infozaPhysicalPersonRequestRepository) {
        this.infozaPhysicalPersonRemRepository = infozaPhysicalPersonRemRepository;
        this.infozaPhysicalPersonRequestRepository = infozaPhysicalPersonRequestRepository;
    }

    public List<InfozaPhysicalPersonRem> findRemarkListByHash(String hash) {
        return infozaPhysicalPersonRemRepository.findByVcHASH(hash);
    }

    public List<InfozaPhysicalPersonRequest> findRequestListByHash(String hash) {
        return infozaPhysicalPersonRequestRepository.findByVcHASH(hash);
    }

    @Transactional
    public void saveInfozaPhysicalPersonRequest(InfozaPhysicalPersonRequest infozaPhysicalPersonRequest) {
        infozaPhysicalPersonRequestRepository.save(infozaPhysicalPersonRequest);
    }

    public InfozaPhysicalPersonRequest getTodayRequestByIst(String hash, Long ist) {
        // Получение текущей даты
        LocalDate today = LocalDate.now();
        // Преобразование в начало дня (00:00:00)
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        // Преобразование в конец дня (23:59:59)
        Instant endOfDay = today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
        return infozaPhysicalPersonRequestRepository.findByVcHASHAndInISTAndDtCREBetween(hash,ist,startOfDay,endOfDay);
    }
}
