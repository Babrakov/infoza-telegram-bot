package ru.infoza.bot.service.infoza;

import org.springframework.stereotype.Service;
import ru.infoza.bot.model.infoza.InfozaPhysicalPersonRem;
import ru.infoza.bot.model.infoza.InfozaPhysicalPersonRequest;
import ru.infoza.bot.repository.infoza.InfozaPhysicalPersonRemRepository;
import ru.infoza.bot.repository.infoza.InfozaPhysicalPersonRequestRepository;

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
}
