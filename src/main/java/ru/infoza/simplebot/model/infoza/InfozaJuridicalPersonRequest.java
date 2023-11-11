package ru.infoza.simplebot.model.infoza;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "lnzo")
public class InfozaJuridicalPersonRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idLZO", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Column(name = "idZZ", columnDefinition = "int UNSIGNED not null")
    private Long idZZ;

    @Column(name = "idZO", columnDefinition = "int UNSIGNED not null")
    private Long idZO;

    @Column(name = "inTIP", columnDefinition = "int UNSIGNED")
    private Long inTIP;

    @Column(name = "inIST", columnDefinition = "int UNSIGNED not null")
    private Long inIST;

    @NotNull
    @Column(name = "dtCRE", nullable = false)
    private Instant dtCRE;

}