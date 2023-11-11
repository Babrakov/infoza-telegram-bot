package ru.infoza.bot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.bot.model.infoza.InfozaPhysicalPersonRem;

import java.util.List;

public interface InfozaPhysicalPersonRemRepository extends JpaRepository<InfozaPhysicalPersonRem, Long> {

    List<InfozaPhysicalPersonRem> findByVcHASH(String hash);

}