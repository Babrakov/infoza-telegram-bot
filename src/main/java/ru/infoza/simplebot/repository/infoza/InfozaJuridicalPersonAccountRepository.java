package ru.infoza.simplebot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.infoza.simplebot.model.infoza.InfozaJuridicalPersonAccount;

import java.util.List;

public interface InfozaJuridicalPersonAccountRepository extends JpaRepository<InfozaJuridicalPersonAccount, Integer> {
    List<InfozaJuridicalPersonAccount> findByVcINN(String vcINN);
}