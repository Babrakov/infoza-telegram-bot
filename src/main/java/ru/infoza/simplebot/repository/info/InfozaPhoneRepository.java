package ru.infoza.simplebot.repository.info;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.info.InfozaPhone;

public interface InfozaPhoneRepository extends JpaRepository<InfozaPhone, Long> {
    InfozaPhone findByVcPHO(String vcPHO);
}