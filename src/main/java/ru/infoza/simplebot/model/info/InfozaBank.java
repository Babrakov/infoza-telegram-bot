package ru.infoza.simplebot.model.info;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "spbnk")
public class InfozaBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBNK", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Size(max = 9)
    @NotNull
    @Column(name = "vcBIK", nullable = false, length = 9)
    private String vcBIK;

    @Size(max = 45)
    @Column(name = "vcNAZ", length = 45)
    private String vcNAZ;

    @Size(max = 4)
    @Column(name = "vcREL", length = 4)
    private String vcREL;

    @Size(max = 2)
    @Column(name = "vcRGN", length = 2)
    private String vcRGN;

    @Size(max = 25)
    @Column(name = "vcNNP", length = 25)
    private String vcNNP;

    @Size(max = 25)
    @Column(name = "vcPHO", length = 25)
    private String vcPHO;

    @Size(max = 6)
    @Column(name = "vcIND", length = 6)
    private String vcIND;

    @Size(max = 30)
    @Column(name = "vcADR", length = 30)
    private String vcADR;

    @Size(max = 2)
    @Column(name = "vcPZN", length = 2)
    private String vcPZN;

    @Column(name = "daIZM")
    private LocalDate daIZM;

    @Column(name = "daDEL")
    private LocalDate daDEL;

    @Column(name = "daIN")
    private LocalDate daIN;

}