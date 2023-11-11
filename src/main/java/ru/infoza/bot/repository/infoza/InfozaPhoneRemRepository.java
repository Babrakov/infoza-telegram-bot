package ru.infoza.bot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.bot.model.infoza.InfozaPhoneRem;

import java.util.List;

public interface InfozaPhoneRemRepository extends JpaRepository<InfozaPhoneRem, Long> {
    List<InfozaPhoneRem> findByVcPHO(String query);
}