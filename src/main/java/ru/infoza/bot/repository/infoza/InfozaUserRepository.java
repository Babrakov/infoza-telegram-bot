package ru.infoza.bot.repository.infoza;

import org.springframework.data.repository.CrudRepository;
import ru.infoza.bot.model.infoza.InfozaUser;

import java.util.List;

public interface InfozaUserRepository extends CrudRepository<InfozaUser, Long> {
    List<InfozaUser> findByVcLNAContainingAndInTIPGreaterThan(String query, int tip);
    List<InfozaUser> findByVcLNAAndInTIPGreaterThan(String query, int tip);
    List<InfozaUser> findByVcORGContainingAndInTIPGreaterThanAndInLSTOrderByVcLNA(String query, int tip, int inLST);

    InfozaUser findByVcUSR(String query);
}
