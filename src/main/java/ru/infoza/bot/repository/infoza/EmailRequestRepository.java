package ru.infoza.bot.repository.infoza;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.infoza.bot.model.infoza.EmailRequest;

public interface EmailRequestRepository extends JpaRepository<EmailRequest, Long> {

    EmailRequest findByIdEmailAndInIstAndCreatedAtBetween(Long id, Long inIst, Instant startOfDay, Instant endOfDay);
    EmailRequest findByIdEmailAndInIstAndCreatedAtAfter(Long id, Long inIst, Instant startOfDay);

    @Query(value = "SELECT zi.vcORG, l.created_at\n"
            + "FROM lnze l\n"
            + "JOIN z_email e ON l.id_email = e.id\n"
            + "JOIN z_ist zi ON l.in_ist = zi.idIST\n"
            + "WHERE e.email = :email\n"
            + "ORDER BY l.created_at", nativeQuery = true)
    List<Object[]> findRequestListByEmail(@Param("email") String email);

}
