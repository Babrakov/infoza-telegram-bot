package ru.infoza.bot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.bot.model.infoza.InfozaJuridicalPersonRequest;

import java.time.Instant;
import java.util.List;

public interface InfozaJuridicalPersonRequestRepository extends JpaRepository<InfozaJuridicalPersonRequest, Long> {
    List<InfozaJuridicalPersonRequest> findByIdZO(Long idZO);

    InfozaJuridicalPersonRequest findByIdZOAndInISTAndDtCREBetween(Long id, long ist, Instant startOfDay, Instant endOfDay);
}