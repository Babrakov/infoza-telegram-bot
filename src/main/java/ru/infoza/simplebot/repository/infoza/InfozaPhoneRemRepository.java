package ru.infoza.simplebot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.infoza.InfozaPhoneRem;

import java.util.List;

public interface InfozaPhoneRemRepository extends JpaRepository<InfozaPhoneRem, Long> {
    List<InfozaPhoneRem> findByVcPHO(String query);
}