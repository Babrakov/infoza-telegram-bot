package ru.infoza.simplebot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.infoza.InfozaBank;

public interface InfozaBankRepository extends JpaRepository<InfozaBank, Long> {
    InfozaBank findTopByVcBIKOrderByDaIZM(String vcBIK);
}