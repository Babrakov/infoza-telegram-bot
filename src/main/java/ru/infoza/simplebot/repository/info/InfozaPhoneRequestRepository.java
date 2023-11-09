package ru.infoza.simplebot.repository.info;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.info.InfozaPhoneRequest;

import java.util.List;

public interface InfozaPhoneRequestRepository extends JpaRepository<InfozaPhoneRequest, Long> {
    List<InfozaPhoneRequest> findByIdZP(Long idZP);
}