package ru.infoza.bot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.bot.model.infoza.InfozaBank;

public interface InfozaBankRepository extends JpaRepository<InfozaBank, Long> {
    InfozaBank findTopByVcBIKOrderByDaIZM(String vcBIK);
}