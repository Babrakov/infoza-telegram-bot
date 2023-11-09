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
@Table(name = "lnzp")
public class InfozaPhoneRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idLZP", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Column(name = "idZZ", columnDefinition = "int UNSIGNED not null")
    private Long idZZ;

    @Column(name = "idZP", columnDefinition = "int UNSIGNED not null")
    private Long idZP;

    @Column(name = "inTIP", columnDefinition = "int UNSIGNED")
    private Long inTIP;

    @Column(name = "inIST", columnDefinition = "int UNSIGNED not null")
    private Long inIST;

    @NotNull
    @Column(name = "dtCRE", nullable = false)
    private Instant dtCRE;

    @Size(max = 15)
    @Column(name = "vcIP", length = 15)
    private String vcIP;

}