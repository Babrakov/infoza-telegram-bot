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
@Table(name = "z_zap")
public class InfozaPhysicalPersonRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idZZ", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Size(max = 32)
    @Column(name = "vcHASH", length = 32)
    private String vcHASH;

    @Column(name = "inTIP")
    private Integer inTIP;

    @NotNull
    @Column(name = "inIST", nullable = false)
    private Long inIST;

    @NotNull
    @Column(name = "dtCRE", nullable = false)
    private Instant dtCRE;

    @Size(max = 15)
    @Column(name = "vcIP", length = 15)
    private String vcIP;

}