package ru.infoza.bot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.bot.model.infoza.InfozaPhoneRequest;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public interface InfozaPhoneRequestRepository extends JpaRepository<InfozaPhoneRequest, Long> {
    List<InfozaPhoneRequest> findByIdZP(Long idZP);

    InfozaPhoneRequest findByIdZPAndInISTAndDtCREBetween(Long idZP, Long inIST, Instant startOfDay, Instant endOfDay);


}