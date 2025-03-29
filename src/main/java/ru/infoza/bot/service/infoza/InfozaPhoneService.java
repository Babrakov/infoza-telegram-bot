package ru.infoza.bot.service.infoza;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.infoza.bot.dto.InfozaPhoneRequestDTO;
import ru.infoza.bot.model.infoza.InfozaPhone;
import ru.infoza.bot.model.infoza.InfozaPhoneRem;
import ru.infoza.bot.model.infoza.InfozaPhoneRequest;
import ru.infoza.bot.repository.infoza.InfozaPhoneRemRepository;
import ru.infoza.bot.repository.infoza.InfozaPhoneRepository;
import ru.infoza.bot.repository.infoza.InfozaPhoneRequestRepository;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfozaPhoneService {

    private final InfozaPhoneRemRepository infozaPhoneRemRepository;
    private final InfozaPhoneRepository infozaPhoneRepository;
    private final InfozaPhoneRequestRepository infozaPhoneRequestRepository;

    public List<InfozaPhoneRem> findRemarksByPhoneNumber(String phone) {
        return infozaPhoneRemRepository.findByVcPHO(phone);
    }

    public InfozaPhone findPhoneByPhoneNumber(String phone) {
        return infozaPhoneRepository.findByVcPHO(phone);
    }

    @Transactional
    public void saveInfozaPhone(InfozaPhone infozaPhone) {
        // Сохраняем объект в базе данных
        infozaPhoneRepository.save(infozaPhone);
    }

    @Transactional
    public void saveInfozaPhoneRequest(InfozaPhoneRequest infozaPhoneRequest) {
        // Сохраняем объект в базе данных
        infozaPhoneRequestRepository.save(infozaPhoneRequest);
    }

    public InfozaPhoneRequest getTodayRequestByIst(Long id, Long ist) {
        // Получение текущей даты
        LocalDate today = LocalDate.now();
        // Преобразование в начало дня (00:00:00)
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        // Преобразование в конец дня (23:59:59)
        Instant endOfDay = today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
        return infozaPhoneRequestRepository.findByIdZPAndInISTAndDtCREBetween(id, ist, startOfDay, endOfDay);
    }

    public List<InfozaPhoneRequestDTO> findRequestListByPhone(String phone) {
        List<Object[]> result = infozaPhoneRequestRepository.findRequestListByVcPHO(phone);
        List<InfozaPhoneRequestDTO> infozaPhoneRequestList = new ArrayList<>();

        for (Object[] row : result) {
            BigInteger inISTBigInteger = (BigInteger) row[0];
            Long inIST = inISTBigInteger.longValue(); // Convert BigInteger to Long

            String vcFIO = (String) row[1];
            String vcORG = (String) row[2];

            Timestamp timestamp = (Timestamp) row[3];
            Instant instant = timestamp.toInstant();
            LocalDate dtCRE = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            InfozaPhoneRequestDTO dto = new InfozaPhoneRequestDTO(inIST, vcFIO, vcORG, dtCRE);
            infozaPhoneRequestList.add(dto);
        }
        return infozaPhoneRequestList;
    }

}
