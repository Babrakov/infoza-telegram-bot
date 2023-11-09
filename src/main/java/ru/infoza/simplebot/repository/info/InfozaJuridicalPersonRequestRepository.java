package ru.infoza.simplebot.repository.info;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.info.InfozaJuridicalPersonRequest;

import java.util.List;

public interface InfozaJuridicalPersonRequestRepository extends JpaRepository<InfozaJuridicalPersonRequest, Long> {
    List<InfozaJuridicalPersonRequest> findByIdZO(Long idZO);
}