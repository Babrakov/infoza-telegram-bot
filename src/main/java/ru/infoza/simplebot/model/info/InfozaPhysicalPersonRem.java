package ru.infoza.simplebot.model.info;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "z_remz")
public class InfozaPhysicalPersonRem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idZRZ", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Size(max = 32)
    @NotNull
    @Column(name = "vcHASH", nullable = false, length = 32)
    private String vcHASH;

    @Column(name = "inTIP", columnDefinition = "int UNSIGNED not null")
    private Long inTIP;

    @Column(name = "inFLS", columnDefinition = "int UNSIGNED")
    private Long inFLS;

    @Lob
    @Column(name = "vcREM")
    private String vcREM;

    @Column(name = "inTMP", columnDefinition = "int UNSIGNED")
    private Long inTMP;

    @Column(name = "inIST", columnDefinition = "int UNSIGNED not null")
    private Long inIST;

    @Column(name = "inGOOD", columnDefinition = "int UNSIGNED")
    private Long inGOOD;

    @NotNull
    @Column(name = "dtCRE", nullable = false)
    private Instant dtCRE;

    @Column(name = "dtCHE")
    private Instant dtCHE;

}