package ru.infoza.simplebot.repository.info;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.info.InfozaPhysicalPersonRem;

import java.util.List;

public interface InfozaPhysicalPersonRemRepository extends JpaRepository<InfozaPhysicalPersonRem, Long> {

    List<InfozaPhysicalPersonRem> findByVcHASH(String hash);

}