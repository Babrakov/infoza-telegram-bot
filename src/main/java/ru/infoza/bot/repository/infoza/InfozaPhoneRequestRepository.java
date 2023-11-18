package ru.infoza.bot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.infoza.bot.model.infoza.InfozaPhoneRequest;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public interface InfozaPhoneRequestRepository extends JpaRepository<InfozaPhoneRequest, Long> {
    List<InfozaPhoneRequest> findByIdZP(Long idZP);

    InfozaPhoneRequest findByIdZPAndInISTAndDtCREBetween(Long idZP, Long inIST, Instant startOfDay, Instant endOfDay);

    @Query(value = "SELECT zp.inIST, zi.vcFIO, zi.vcORG, zp.dtCRE " +
            "FROM z_pho zp " +
            "JOIN z_ist zi ON zp.inIST = zi.idIST " +
            "WHERE zp.vcPHO = :phoneNumber " +
            "UNION " +
            "SELECT l.inIST, zi.vcFIO, zi.vcORG, l.dtCRE " +
            "FROM lnzp l " +
            "JOIN z_pho zp2 ON l.idZP = zp2.idZP " +
            "JOIN z_ist zi ON l.inIST = zi.idIST " +
            "WHERE zp2.vcPHO = :phoneNumber " +
            "ORDER BY dtCRE", nativeQuery = true)
    List<Object[]> findRequestListByVcPHO(@Param("phoneNumber") String phoneNumber);

}