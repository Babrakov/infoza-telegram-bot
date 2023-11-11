package ru.infoza.simplebot.service.infoza;

import org.springframework.stereotype.Service;
import ru.infoza.simplebot.model.infoza.InfozaBank;
import ru.infoza.simplebot.repository.infoza.InfozaBankRepository;

@Service
public class InfozaBankService {
    private final InfozaBankRepository infozaBankRepository;

    public InfozaBankService(InfozaBankRepository infozaBankRepository) {
        this.infozaBankRepository = infozaBankRepository;
    }

    public InfozaBank findBankByBIK(String vcBIK) {
        return infozaBankRepository.findTopByVcBIKOrderByDaIZM(vcBIK);
    }
}
