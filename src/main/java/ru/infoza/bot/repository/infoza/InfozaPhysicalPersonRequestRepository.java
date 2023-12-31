package ru.infoza.bot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.bot.model.infoza.InfozaPhoneRequest;
import ru.infoza.bot.model.infoza.InfozaPhysicalPersonRequest;

import java.time.Instant;
import java.util.List;

public interface InfozaPhysicalPersonRequestRepository extends JpaRepository<InfozaPhysicalPersonRequest, Long> {
    List<InfozaPhysicalPersonRequest> findByVcHASH(String vcHASH);

    InfozaPhysicalPersonRequest findByVcHASHAndInISTAndDtCREBetween(String hash, Long ist, Instant startOfDay, Instant endOfDay);
}