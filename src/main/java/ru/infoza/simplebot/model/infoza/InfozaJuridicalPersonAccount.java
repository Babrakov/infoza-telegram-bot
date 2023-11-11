package ru.infoza.simplebot.model.infoza;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "tbche")
public class InfozaJuridicalPersonAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCHE", nullable = false)
    private Integer id;

    @Size(max = 12)
    @NotNull
    @Column(name = "vcINN", nullable = false, length = 12)
    private String vcINN;

    @Size(max = 9)
    @Column(name = "vcBIK", length = 9)
    private String vcBIK;

    @Column(name = "dCHE")
    private LocalDate dCHE;

}