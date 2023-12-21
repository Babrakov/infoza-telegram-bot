package ru.infoza.bot.service.infoza;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.infoza.bot.config.PostgresConfig;
import ru.infoza.bot.dto.GetcontactDTO;
import ru.infoza.bot.dto.GrabContactDTO;
import ru.infoza.bot.dto.InfozaPhoneRequestDTO;
import ru.infoza.bot.dto.NumbusterDTO;
import ru.infoza.bot.model.infoza.InfozaPhone;
import ru.infoza.bot.model.infoza.InfozaPhoneRem;
import ru.infoza.bot.model.infoza.InfozaPhoneRequest;
import ru.infoza.bot.repository.infoza.InfozaPhoneRemRepository;
import ru.infoza.bot.repository.infoza.InfozaPhoneRepository;
import ru.infoza.bot.repository.infoza.InfozaPhoneRequestRepository;
import ru.infoza.bot.util.Pair;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
public class InfozaPhoneService {

    public static final String SRD_URL = "http://phones.local/?pho=7";
    public static final String REQUEST_FAILED_WITH_ERROR = "HTTP Request Failed with error code: ";
    public static final String REQUEST_FAILED_WITH_EXCEPTION = "HTTP Request Failed with exception: ";
    public static final String REQUEST_FAILED_WITH_TIMEOUT = "HTTP Request Failed with timeout: ";
    public static final String EMPTY_STRING = "";
    @Value("${api.key}")
    private String apiKey;

    @Value("${api.url}")
    private String apiUrl;

    private final InfozaPhoneRemRepository infozaPhoneRemRepository;
    private final InfozaPhoneRepository infozaPhoneRepository;
    private final InfozaPhoneRequestRepository infozaPhoneRequestRepository;
    private final PostgresConfig postgresConfig;

    public InfozaPhoneService(InfozaPhoneRemRepository infozaPhoneRemRepository, InfozaPhoneRepository infozaPhoneRepository, InfozaPhoneRequestRepository infozaPhoneRequestRepository, PostgresConfig postgresConfig) {
        this.infozaPhoneRemRepository = infozaPhoneRemRepository;
        this.infozaPhoneRepository = infozaPhoneRepository;
        this.infozaPhoneRequestRepository = infozaPhoneRequestRepository;
        this.postgresConfig = postgresConfig;
    }

    public List<InfozaPhoneRem> findRemarksByPhoneNumber(String phone) {
        return infozaPhoneRemRepository.findByVcPHO(phone);
    }

    public InfozaPhone findPhoneByPhoneNumber(String phone) {
        return infozaPhoneRepository.findByVcPHO(phone);
    }

    public List<InfozaPhoneRequest> findRequestsByPhoneId(Long id) {
        return infozaPhoneRequestRepository.findByIdZP(id);
    }

    public String getPhoneInfo(String phoneNumber) {
        // Формируем URL с параметром телефона
        String url = SRD_URL + phoneNumber;

        // Создаем HttpClient с настройками тайм-аута
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)) // Время ожидания установки соединения с сервером
                .build();

        // Создаем объект HttpRequest с указанным URL
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(20))  // Ответ может долго формироваться
                .build();

        try {
            // Выполняем запрос и получаем ответ
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // Проверяем статус код ответа
            if (response.statusCode() == 200) {
                // Извлекаем содержимое ответа как строку
                return response.body();
            } else {
                // В случае ошибки пишем в лог и возвращаем пустую строку
                log.error(REQUEST_FAILED_WITH_ERROR + response.statusCode());
                return EMPTY_STRING;
            }
        } catch (HttpTimeoutException e) {
            // В случае тайм-аута, возвращаем пустую строку
            log.error(REQUEST_FAILED_WITH_TIMEOUT + e.getMessage());
            return EMPTY_STRING;
        } catch (Exception e) {
            // В случае других исключений, возвращаем пустую строку
            log.error(REQUEST_FAILED_WITH_EXCEPTION + e.getMessage());
            return EMPTY_STRING;
        }
    }

    public List<GrabContactDTO> getGrabContactInfo(String phoneNumber) {
        return executeRequest("/api/v1/phones/" + phoneNumber, this::parseGrabContactDTOList);
    }

    public List<NumbusterDTO> getNumbusterInfo(String phoneNumber) {
        return executeRequest("/api/v1/numbuster/" + phoneNumber, this::parseNumbusterDTOList);
    }

    public List<GetcontactDTO> getGetcontactInfo(String phoneNumber) {
        return executeRequest("/api/v1/getcontact/" + phoneNumber, this::parseGetcontactDTOList);
    }

    private <T> List<T> parseDTOList(String responseBody, TypeReference<List<T>> typeReference, String dtoName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(responseBody, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Error parsing " + dtoName + " list: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<GrabContactDTO> parseGrabContactDTOList(String responseBody) {
        TypeReference<List<GrabContactDTO>> typeReference = new TypeReference<>() {};
        return parseDTOList(responseBody, typeReference, "GrabContactDTO");
    }

    private List<NumbusterDTO> parseNumbusterDTOList(String responseBody) {
        TypeReference<List<NumbusterDTO>> typeReference = new TypeReference<>() {};
        return parseDTOList(responseBody, typeReference, "NumbusterDTO");
    }

    private List<GetcontactDTO> parseGetcontactDTOList(String responseBody) {
        TypeReference<List<GetcontactDTO>> typeReference = new TypeReference<>() {};
        return parseDTOList(responseBody, typeReference, "GetcontactDTO");
    }

    @Transactional
    public void saveInfozaPhone(InfozaPhone infozaPhone) {
        // Сохраняем объект в базе данных
        infozaPhoneRepository.save(infozaPhone);
    }

    @Transactional
    public void saveInfozaPhoneRequest(InfozaPhoneRequest infozaPhoneRequest) {
        // Сохраняем объект в базе данных
        infozaPhoneRequestRepository.save(infozaPhoneRequest);
    }

    public InfozaPhoneRequest getTodayRequestByIst(Long id, Long ist) {
        // Получение текущей даты
        LocalDate today = LocalDate.now();
        // Преобразование в начало дня (00:00:00)
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        // Преобразование в конец дня (23:59:59)
        Instant endOfDay = today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
        return infozaPhoneRequestRepository.findByIdZPAndInISTAndDtCREBetween(id, ist, startOfDay, endOfDay);
    }

    public List<InfozaPhoneRequestDTO> findRequestListByPhone(String phone) {
        List<Object[]> result = infozaPhoneRequestRepository.findRequestListByVcPHO(phone);
        List<InfozaPhoneRequestDTO> infozaPhoneRequestList = new ArrayList<>();

        for (Object[] row : result) {
            BigInteger inISTBigInteger = (BigInteger) row[0];
            Long inIST = inISTBigInteger.longValue(); // Convert BigInteger to Long

            String vcFIO = (String) row[1];
            String vcORG = (String) row[2];

            Timestamp timestamp = (Timestamp) row[3];
            Instant instant = timestamp.toInstant();
            LocalDate dtCRE = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            InfozaPhoneRequestDTO dto = new InfozaPhoneRequestDTO(inIST, vcFIO, vcORG, dtCRE);
            infozaPhoneRequestList.add(dto);
        }
        return infozaPhoneRequestList;
    }

    private <T> List<T> executeRequest(String endpoint, Function<String, List<T>> parser) {
        String requestUrl = apiUrl + endpoint;

        // Create an HttpClient with timeout settings
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(requestUrl)).header("Authorization", "Bearer " + apiKey).build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                return parser.apply(responseBody);
            } else {
                log.error(REQUEST_FAILED_WITH_ERROR + response.statusCode());
                return Collections.emptyList();
            }
        } catch (IOException | InterruptedException e) {
            log.error(REQUEST_FAILED_WITH_EXCEPTION + e.getMessage());
            return Collections.emptyList();
        }
    }


    public String getCloudPhoneInfo(String phone) {

        String jdbcUrl = postgresConfig.getPostgresUrl();
        String user = postgresConfig.getPostgresUser();
        String password = postgresConfig.getPostgresPassword();

        StringBuilder result = new StringBuilder();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, user, password)) {
            // Шаг 1: Найти запись в phones
            long phoneId = findCloudPhoneId(connection, phone);

            if (phoneId != -1) {
                // Шаг 2: Найти записи в phones_sources
                Map<Long, Pair<String,String>> sourceInfo = findSourceInfo(connection, phoneId);

                if (sourceInfo != null) {
                    for (Map.Entry<Long, Pair<String,String>> entry : sourceInfo.entrySet()) {
                        long recordId = entry.getKey();
                        Pair<String,String> tableInfo = entry.getValue();
                        String tableName = tableInfo.getLeft();
                        String sourceName = tableInfo.getRight();
                        // Шаг 3: Сделать запрос к таблице sourceTableName
                        JsonObject jsonResult = executeQuery(connection, tableName, recordId);
                        result.append("<u>").append(sourceName).append("</u>")
                                .append(": ")
                                .append(processCloudResult(jsonResult))
                                .append("\n");
                    }


                }
//                else {
//                    result = new StringBuilder("Не удалось найти имя таблицы источника.");
//                }
            }
//            else {
//                result = new StringBuilder("Не удалось найти номер телефона в таблице phones.");
//            }

        } catch (SQLException e) {
            e.printStackTrace();
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

    private long findCloudPhoneId(Connection connection, String phone) throws SQLException {
        String sql = "SELECT id FROM public.phones WHERE phone = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phone);
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

    private Map<Long, Pair<String,String>> findSourceInfo(Connection connection, long phoneId) throws SQLException {
        Map<Long, Pair<String,String>> result = new HashMap<>();
        String sql = "SELECT source_id, record_id FROM public.phones_sources WHERE phone_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, phoneId);
            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Pair<String,String> sourceTableInfo = getSourceTableInfo(connection, resultSet.getLong("source_id"));
                    long recordId = resultSet.getLong("record_id");
                    result.put(recordId, sourceTableInfo);
                }
                return result;
            }
        }
    }

    private Pair<String,String> getSourceTableInfo(Connection connection, long sourceId) throws SQLException {
        String sql = "SELECT table_name, name FROM public.sources WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sourceId);
            //System.out.println(getSqlWithValues(sql, sourceId));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Pair.of(resultSet.getString("table_name"),resultSet.getString("name"));
                }
            }
        }
        return null;
    }

    private JsonObject executeQuery(Connection connection, String tableName, long recordId) throws SQLException {

        DatabaseMetaData dbMetaData = connection.getMetaData();
        ResultSet columns = dbMetaData.getColumns(null, null, tableName, null);
        Map<String, String> details = new HashMap<>();
        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            String columnComment = columns.getString("REMARKS");
            details.put(columnName, columnComment);
//            System.out.println("Column Name: " + columnName + ", Comment: " + columnComment);
        }

        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, recordId);
            try (ResultSet resultSet = statement.executeQuery()) {
                JsonObject result = new JsonObject();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (resultSet.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        // Исключаем столбец "id"
                        if (!columnName.equalsIgnoreCase("id") && details.get(columnName)!=null) {
                            Object value = resultSet.getObject(i);
                            // Проверка на null перед вызовом toString()
                            result.addProperty(details.get(columnName), (value != null) ? value.toString() : null);
                        }
                    }
                }
                return result;
            }
        }
    }


}
