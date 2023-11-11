package ru.infoza.bot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.bot.model.infoza.InfozaJuridicalPersonRem;

import java.util.List;

public interface InfozaJuridicalPersonRemRepository extends JpaRepository<InfozaJuridicalPersonRem, Long> {

    List<InfozaJuridicalPersonRem> findByVcINN(String inn);

}