package ru.infoza.bot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.bot.model.infoza.InfozaJuridicalPerson;
import ru.infoza.bot.model.infoza.InfozaJuridicalPersonRequest;

import java.time.Instant;
import java.util.List;

public interface InfozaJuridicalPersonRepository extends JpaRepository<InfozaJuridicalPerson, Long> {
    List<InfozaJuridicalPerson> findByVcINN(String vcINN);

}