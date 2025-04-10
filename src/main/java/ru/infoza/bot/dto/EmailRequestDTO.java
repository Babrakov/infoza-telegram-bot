package ru.infoza.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class EmailRequestDTO {
    private String org;
    private LocalDate date;
}
