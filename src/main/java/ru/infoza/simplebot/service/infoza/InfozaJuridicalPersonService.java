package ru.infoza.simplebot.service.infoza;

import org.springframework.stereotype.Service;
import ru.infoza.simplebot.model.infoza.InfozaJuridicalPerson;
import ru.infoza.simplebot.model.infoza.InfozaJuridicalPersonAccount;
import ru.infoza.simplebot.model.infoza.InfozaJuridicalPersonRem;
import ru.infoza.simplebot.model.infoza.InfozaJuridicalPersonRequest;
import ru.infoza.simplebot.repository.infoza.InfozaJuridicalPersonAccountRepository;
import ru.infoza.simplebot.repository.infoza.InfozaJuridicalPersonRemRepository;
import ru.infoza.simplebot.repository.infoza.InfozaJuridicalPersonRepository;
import ru.infoza.simplebot.repository.infoza.InfozaJuridicalPersonRequestRepository;

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
}
