package ru.infoza.bot.model.infoza;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "s_usr")
public class InfozaUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUSR", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Size(max = 16)
    @NotNull
    @Column(name = "vcUSR", nullable = false, length = 16)
    private String vcUSR;

    @Size(max = 32)
    @NotNull
    @Column(name = "vcLNA", nullable = false, length = 32)
    private String vcLNA;

    @Size(max = 32)
    @NotNull
    @Column(name = "vcFNA", nullable = false, length = 32)
    private String vcFNA;

    @Size(max = 32)
    @Column(name = "vcMNA", length = 32)
    private String vcMNA;

    @Column(name = "inTIP", columnDefinition = "int UNSIGNED")
    private int inTIP;

    @Column(name = "inGRP", columnDefinition = "int UNSIGNED")
    private int inGRP;

    @Size(max = 32)
    @Column(name = "vcSITY", length = 32)
    private String vcSITY;

    @Size(max = 128)
    @Column(name = "vcORG", length = 128)
    private String vcORG;

    @Size(max = 32)
    @Column(name = "vcIP", length = 32)
    private String vcIP;

    @Column(name = "inLOG")
    private Integer inLOG;

    @Size(max = 255)
    @Column(name = "vcHOST")
    private String vcHOST;

    @Column(name = "inLST")
    private Integer inLST;

}