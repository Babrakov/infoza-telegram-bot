package ru.infoza.bot.dto.cldb;

import lombok.Value;
import ru.infoza.bot.model.cldb.Source;

import java.io.Serializable;

/**
 * DTO for {@link Source}
 */
@Value
public class SourcesDto implements Serializable {
    String name;
    String table;
}