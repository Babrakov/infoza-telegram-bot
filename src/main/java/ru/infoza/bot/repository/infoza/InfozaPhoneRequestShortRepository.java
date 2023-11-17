package ru.infoza.bot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.infoza.bot.model.infoza.InfozaPhoneRequest;
import ru.infoza.bot.model.infoza.InfozaPhoneRequestShort;

import java.util.List;

public interface InfozaPhoneRequestShortRepository extends JpaRepository<InfozaPhoneRequestShort, Long> {
//    @Query(value = "SELECT zp.inIST, zi.vcFIO, zp.dtCRE FROM z_pho zp " +
//            "JOIN z_ist zi ON zp.inIST = zi.idIST " +
//            "WHERE zp.vcPHO = :vcPHO " +
//            "UNION " +
//            "SELECT l.inIST, zi.vcFIO, l.dtCRE FROM lnzp l " +
//            "JOIN z_pho zp2 ON l.idZP = zp2.idZP " +
//            "JOIN z_ist zi ON l.inIST = zi.idIST " +
//            "WHERE zp2.vcPHO = :vcPHO", nativeQuery = true)
    @Query(value = "SELECT ROW_NUMBER() OVER (ORDER BY inIST) as id,inIST,vcFIO,vcORG,dtCRE\n" +
            "FROM (\n" +
            "    SELECT zp.inIST,zi.vcFIO,zi.vcORG,zp.dtCRE \n" +
            "    FROM z_pho zp \n" +
            "    JOIN z_ist zi ON zp.inIST = zi.idIST \n" +
            "    WHERE zp.vcPHO = :vcPHO\n" +
            "    UNION \n" +
            "    SELECT l.inIST,zi.vcFIO,zi.vcORG,l.dtCRE \n" +
            "    FROM lnzp l \n" +
            "    JOIN z_pho zp2 ON l.idZP = zp2.idZP \n" +
            "    JOIN z_ist zi ON l.inIST = zi.idIST \n" +
            "    WHERE zp2.vcPHO = :vcPHO\n" +
            ") AS Subquery ORDER BY dtCRE", nativeQuery = true)
    List<InfozaPhoneRequestShort> findRequestListByVcPHO(String vcPHO);
}