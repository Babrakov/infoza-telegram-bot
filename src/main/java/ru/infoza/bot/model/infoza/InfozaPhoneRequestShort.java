package ru.infoza.bot.model.infoza;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(force = true)
public class InfozaPhoneRequestShort {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private final Long id;
    private final Long inIST;
    private final String vcFIO;
    private final String vcORG;
    private final LocalDate dtCRE;

}
