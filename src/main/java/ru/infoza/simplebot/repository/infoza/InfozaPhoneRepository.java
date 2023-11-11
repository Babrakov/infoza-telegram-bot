package ru.infoza.simplebot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.infoza.InfozaPhone;

public interface InfozaPhoneRepository extends JpaRepository<InfozaPhone, Long> {
    InfozaPhone findByVcPHO(String vcPHO);
}