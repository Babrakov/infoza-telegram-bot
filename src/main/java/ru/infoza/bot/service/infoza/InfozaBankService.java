package ru.infoza.bot.service.infoza;

import org.springframework.stereotype.Service;
import ru.infoza.bot.model.infoza.InfozaBank;
import ru.infoza.bot.repository.infoza.InfozaBankRepository;

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
