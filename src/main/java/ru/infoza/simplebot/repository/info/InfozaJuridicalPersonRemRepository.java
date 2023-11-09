package ru.infoza.simplebot.repository.info;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.info.InfozaJuridicalPersonRem;

import java.util.List;

public interface InfozaJuridicalPersonRemRepository extends JpaRepository<InfozaJuridicalPersonRem, Long> {

    List<InfozaJuridicalPersonRem> findByVcINN(String inn);

}