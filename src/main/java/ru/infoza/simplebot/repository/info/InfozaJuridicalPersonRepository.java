package ru.infoza.simplebot.repository.info;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.info.InfozaJuridicalPerson;

import java.util.List;

public interface InfozaJuridicalPersonRepository extends JpaRepository<InfozaJuridicalPerson, Long> {
    List<InfozaJuridicalPerson> findByVcINN(String vcINN);
}