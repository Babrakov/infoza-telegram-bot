package ru.infoza.bot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.bot.model.infoza.InfozaPhone;

public interface InfozaPhoneRepository extends JpaRepository<InfozaPhone, Long> {
    InfozaPhone findByVcPHO(String vcPHO);
}