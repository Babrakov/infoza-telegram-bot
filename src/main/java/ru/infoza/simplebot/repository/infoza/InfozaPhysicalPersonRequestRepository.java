package ru.infoza.simplebot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.infoza.InfozaPhysicalPersonRequest;

import java.util.List;

public interface InfozaPhysicalPersonRequestRepository extends JpaRepository<InfozaPhysicalPersonRequest, Long> {
    List<InfozaPhysicalPersonRequest> findByVcHASH(String vcHASH);
}