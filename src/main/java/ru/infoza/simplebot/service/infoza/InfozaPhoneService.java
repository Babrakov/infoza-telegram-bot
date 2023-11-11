package ru.infoza.simplebot.service.infoza;

import org.springframework.stereotype.Service;
import ru.infoza.simplebot.model.infoza.InfozaPhone;
import ru.infoza.simplebot.model.infoza.InfozaPhoneRem;
import ru.infoza.simplebot.model.infoza.InfozaPhoneRequest;
import ru.infoza.simplebot.repository.infoza.InfozaPhoneRemRepository;
import ru.infoza.simplebot.repository.infoza.InfozaPhoneRepository;
import ru.infoza.simplebot.repository.infoza.InfozaPhoneRequestRepository;

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
