package ru.infoza.bot.model.infoza;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "z_remo")
public class InfozaJuridicalPersonRem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idZRO", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Size(max = 12)
    @NotNull
    @Column(name = "vcINN", nullable = false, length = 12)
    private String vcINN;

    @Column(name = "inTIP", columnDefinition = "int UNSIGNED not null")
    private Long inTIP;

    @Column(name = "inFLS", columnDefinition = "int UNSIGNED")
    private Long inFLS;

    @Size(max = 256)
    @Column(name = "vcREM", length = 256)
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