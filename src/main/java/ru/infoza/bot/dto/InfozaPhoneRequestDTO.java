package ru.infoza.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.glassfish.grizzly.http.util.TimeStamp;

import java.math.BigInteger;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class InfozaPhoneRequestDTO {
    private Long inIST;
    private String vcFIO;
    private String vcORG;
    private LocalDate dtCRE;
}
