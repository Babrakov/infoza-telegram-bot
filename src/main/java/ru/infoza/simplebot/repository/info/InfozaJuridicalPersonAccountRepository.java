package ru.infoza.simplebot.repository.info;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.info.InfozaJuridicalPersonAccount;

import java.util.List;

public interface InfozaJuridicalPersonAccountRepository extends JpaRepository<InfozaJuridicalPersonAccount, Integer> {
    List<InfozaJuridicalPersonAccount> findByVcINN(String vcINN);
}