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
@Table(name = "z_pho")
public class InfozaPhone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idZP", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Size(max = 10)
    @NotNull
    @Column(name = "vcPHO", nullable = false, length = 10)
    private String vcPHO;

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