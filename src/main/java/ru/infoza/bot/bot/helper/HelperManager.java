package ru.infoza.bot.bot.helper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Getter
@RequiredArgsConstructor
public class HelperManager {

    private final FlsHelper flsHelper;
    private final CarHelper carHelper;
    private final EmailHelper emailHelper;
    private final EmployeeHelper employeeHelper;
    private final PhoneHelper phoneHelper;
    private final UlsHelper ulsHelper;

}
