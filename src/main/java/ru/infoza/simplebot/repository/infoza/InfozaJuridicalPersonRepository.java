package ru.infoza.simplebot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.infoza.InfozaJuridicalPerson;

import java.util.List;

public interface InfozaJuridicalPersonRepository extends JpaRepository<InfozaJuridicalPerson, Long> {
    List<InfozaJuridicalPerson> findByVcINN(String vcINN);
}