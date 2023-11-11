package ru.infoza.bot.service.infoza;

import org.springframework.stereotype.Service;
import ru.infoza.bot.model.infoza.InfozaPhone;
import ru.infoza.bot.model.infoza.InfozaPhoneRem;
import ru.infoza.bot.model.infoza.InfozaPhoneRequest;
import ru.infoza.bot.repository.infoza.InfozaPhoneRemRepository;
import ru.infoza.bot.repository.infoza.InfozaPhoneRepository;
import ru.infoza.bot.repository.infoza.InfozaPhoneRequestRepository;

import java.util.List;

@Service
public class InfozaPhoneService {

    private final InfozaPhoneRemRepository infozaPhoneRemRepository;
    private final InfozaPhoneRepository infozaPhoneRepository;
    private final InfozaPhoneRequestRepository infozaPhoneRequestRepository;

    public InfozaPhoneService(InfozaPhoneRemRepository infozaPhoneRemRepository, InfozaPhoneRepository infozaPhoneRepository, InfozaPhoneRequestRepository infozaPhoneRequestRepository) {
        this.infozaPhoneRemRepository = infozaPhoneRemRepository;
        this.infozaPhoneRepository = infozaPhoneRepository;
        this.infozaPhoneRequestRepository = infozaPhoneRequestRepository;
    }

    public List<InfozaPhoneRem> findRemarksByPhoneNumber(String phone){
        return infozaPhoneRemRepository.findByVcPHO(phone);
    }

    public InfozaPhone findPhoneByPhoneNumber(String phone){
        return infozaPhoneRepository.findByVcPHO(phone);
    }

    public List<InfozaPhoneRequest> findRequestsByPhoneId(Long id) {
        return infozaPhoneRequestRepository.findByIdZP(id);
    }
}
