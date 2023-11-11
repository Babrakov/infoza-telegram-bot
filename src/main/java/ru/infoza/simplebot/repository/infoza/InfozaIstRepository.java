package ru.infoza.simplebot.repository.infoza;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.infoza.simplebot.model.infoza.InfozaIst;

import java.util.List;

public interface InfozaIstRepository extends JpaRepository<InfozaIst, Long> {

    @Query("SELECT z FROM z_ist z \n" +
            "JOIN s_usr s ON z.vcUSR=s.vcUSR \n" +
            "WHERE  \n" +
            "    REPLACE(REPLACE(REPLACE(REPLACE(z.vcSOT, '-', ''), '(', ''), ')', ''), ' ', '') LIKE %:phoneNumber% \n" +
            "    OR \n" +
            "    REPLACE(REPLACE(REPLACE(REPLACE(z.vcWRK, '-', ''), '(', ''), ')', ''), ' ', '') LIKE %:phoneNumber% \n" )
    List<InfozaIst> findInfoByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT z FROM z_ist z \n" +
            "JOIN s_usr s ON z.vcUSR=s.vcUSR \n" +
            "WHERE  \n" +
            " s.inTIP>1 AND(" +
            "    REPLACE(REPLACE(REPLACE(REPLACE(z.vcSOT, '-', ''), '(', ''), ')', ''), ' ', '') LIKE %:phoneNumber% \n" +
            "    OR \n" +
            "    REPLACE(REPLACE(REPLACE(REPLACE(z.vcWRK, '-', ''), '(', ''), ')', ''), ' ', '') LIKE %:phoneNumber% \n)")
    List<InfozaIst> findInfoIstByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    InfozaIst findInfozaIstByVcUSR(String usr);
}
