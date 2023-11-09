package ru.infoza.simplebot.repository.info;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.info.InfozaBank;

public interface InfozaBankRepository extends JpaRepository<InfozaBank, Long> {
    InfozaBank findTopByVcBIKOrderByDaIZM(String vcBIK);
}