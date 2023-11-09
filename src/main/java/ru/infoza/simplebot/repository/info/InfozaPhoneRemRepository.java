package ru.infoza.simplebot.repository.info;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.info.InfozaPhoneRem;

import java.util.List;

public interface InfozaPhoneRemRepository extends JpaRepository<InfozaPhoneRem, Long> {
    List<InfozaPhoneRem> findByVcPHO(String query);
}