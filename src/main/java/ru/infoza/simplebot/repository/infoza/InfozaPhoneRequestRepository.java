package ru.infoza.simplebot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.infoza.InfozaPhoneRequest;

import java.util.List;

public interface InfozaPhoneRequestRepository extends JpaRepository<InfozaPhoneRequest, Long> {
    List<InfozaPhoneRequest> findByIdZP(Long idZP);
}