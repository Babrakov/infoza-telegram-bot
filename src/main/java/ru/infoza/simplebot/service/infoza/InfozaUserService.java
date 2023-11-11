package ru.infoza.simplebot.service.infoza;

import org.springframework.stereotype.Service;
import ru.infoza.simplebot.model.infoza.InfozaIst;
import ru.infoza.simplebot.model.infoza.InfozaUser;
import ru.infoza.simplebot.repository.infoza.InfozaIstRepository;
import ru.infoza.simplebot.repository.infoza.InfozaUserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class InfozaUserService {

    private final InfozaIstRepository infozaIstRepository;
    private final InfozaUserRepository infozaUserRepository;


    public InfozaUserService(InfozaIstRepository infozaIstRepository, InfozaUserRepository infozaUserRepository) {
        this.infozaIstRepository = infozaIstRepository;
        this.infozaUserRepository = infozaUserRepository;
    }

    public List<InfozaIst> findIstListByPhone(String phone) {
        return infozaIstRepository.findInfoIstByPhoneNumber(phone);
    }


    public Optional<InfozaIst> findIstById(Long inIST) {
        return infozaIstRepository.findById(inIST);
    }


    public InfozaIst findIstByUserName(String user) {
        return infozaIstRepository.findInfozaIstByVcUSR(user);
    }

    public InfozaUser findUserByUserName(String vcUSR) {
        return infozaUserRepository.findByVcUSR(vcUSR);
    }

    public List<InfozaUser> findEnabledUserListByFullName(String query) {
        return infozaUserRepository.findByVcLNAAndInTIPGreaterThan(query, 1);
    }

    public List<InfozaUser> findEnabledUserListByOrganisation(String query) {
        return infozaUserRepository.findByVcORGContainingAndInTIPGreaterThanAndInLSTOrderByVcLNA(query, 1, 1);
    }

}
