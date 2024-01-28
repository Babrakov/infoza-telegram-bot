package ru.infoza.bot.service.cldb;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.infoza.bot.model.cldb.Source;
import ru.infoza.bot.repository.cldb.SourcesRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CldbCarService {

    public static final String SQL_FAILED_WITH_EXCEPTION = "SQL Request Failed with exception: ";

    private final DataSource postgresDataSource;
    private final SourcesRepository sourcesRepository;

    public CldbCarService(@Qualifier("postgresDataSource") DataSource postgresDataSource,
                          SourcesRepository sourcesRepository) {

        this.postgresDataSource = postgresDataSource;
        this.sourcesRepository = sourcesRepository;
    }

    public String getCloudCarInfo(String car) {
        StringBuilder result = new StringBuilder();
        try (Connection connection = postgresDataSource.getConnection()) {
            // Шаг 1: Найти запись в cars
            long carId = findCloudCarId(connection, car);

            if (carId != -1) {
                // Шаг 2: Найти записи в cars_sources
                Map<Long, Source> sourceInfo = findSourceInfo(connection, carId);

                if (sourceInfo != null) {
                    for (Map.Entry<Long, Source> entry : sourceInfo.entrySet()) {
                        long recordId = entry.getKey();
                        Source tableInfo = entry.getValue();
                        String tableName = tableInfo.getTableName();
                        String sourceUrl = tableInfo.getUrl();
                        String sourceName = tableInfo.getName();
                        // Шаг 3: Сделать запрос к таблице sourceTableName
                        JsonObject jsonResult = executeQuery(connection, tableName, recordId);
                        result.append("<a href='").append(sourceUrl).append("'>")
                                .append(sourceName).append("</a>")
                                .append(": ")
                                .append(processCloudResult(jsonResult))
                                .append("\n");
                    }
                } else {
                    log.error("Не удалось найти имя таблицы источника.");
                }
            }

        } catch (SQLException e) {
            log.error(SQL_FAILED_WITH_EXCEPTION + e.getMessage());
        }
        return result.toString();
    }


    private String processCloudResult(JsonObject result) {
        StringBuilder processedString = new StringBuilder();
        for (String key : result.keySet()) {
            JsonElement value = result.get(key);
            if (value != null && !value.isJsonNull()) {
                processedString.append(key).append(" ");
                processedString.append(value.getAsString()).append(", ");
            }
        }
        // Удаляем лишнюю запятую и пробел в конце строки
        if (processedString.length() > 0) {
            processedString.setLength(processedString.length() - 2);
        }
        return processedString.toString();
    }

    private long findCloudCarId(Connection connection, String car) throws SQLException {
        String sql = "SELECT id FROM public.cars WHERE car_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, car);
            try (ResultSet resultSet = statement.executeQuery()) {
                // Вывод SQL-запроса с подставленным значением
                if (resultSet.next()) {
                    return resultSet.getLong("id");
                }
            }
        }
        return -1;
    }

    private String getSqlWithValues(String sql, Object... values) {
        // Заменяем ? на значения
        for (Object value : values) {
            sql = sql.replaceFirst("\\?", value.toString());
        }
        return sql;
    }

    private Map<Long, Source> findSourceInfo(Connection connection, long carId) throws SQLException {
        Map<Long, Source> result = new HashMap<>();
        String sql = "SELECT source_id, record_id FROM public.cars_sources WHERE car_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, carId);
            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Source source = sourcesRepository.findById(resultSet.getInt("source_id")).orElse(null);
                    long recordId = resultSet.getLong("record_id");
                    result.put(recordId, source);
                }
                return result;
            }
        }
    }

    private JsonObject executeQuery(Connection connection, String tableName, long recordId) throws SQLException {

        DatabaseMetaData dbMetaData = connection.getMetaData();
        ResultSet columns = dbMetaData.getColumns(null, null, tableName, null);
        Map<String, String> details = new HashMap<>();
        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            String columnComment = columns.getString("REMARKS");
            details.put(columnName, columnComment);
        }

        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, recordId);
            try (ResultSet resultSet = statement.executeQuery()) {
                JsonObject result = new JsonObject();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                // Форматтер для преобразования дат
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

                while (resultSet.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        // Исключаем столбец "id"
                        if (!columnName.equalsIgnoreCase("id") && details.get(columnName) != null) {
                            Object value = resultSet.getObject(i);
                            // Проверка на null перед вызовом toString()
                            if (value != null) {
                                // Если это дата, форматируем ее
                                if (value instanceof Date) {
                                    java.util.Date dateValue = new java.util.Date(((Date) value).getTime());
                                    result.addProperty(details.get(columnName), dateFormat.format(dateValue));
                                } else {
                                    result.addProperty(details.get(columnName), value.toString());
                                }
                            } else {
                                result.addProperty(details.get(columnName), (String) null);
                            }
                        }
                    }
                }
                return result;
            }
        }
    }


}
