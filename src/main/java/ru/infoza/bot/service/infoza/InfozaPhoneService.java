package ru.infoza.bot.service.infoza;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public InfozaPhoneService(InfozaPhoneRemRepository infozaPhoneRemRepository, InfozaPhoneRepository infozaPhoneRepository, InfozaPhoneRequestRepository infozaPhoneRequestRepository) {
        this.infozaPhoneRemRepository = infozaPhoneRemRepository;
        this.infozaPhoneRepository = infozaPhoneRepository;
        this.infozaPhoneRequestRepository = infozaPhoneRequestRepository;
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
        TypeReference<List<GrabContactDTO>> typeReference = new TypeReference<>() {
        };
        return parseDTOList(responseBody, typeReference, "GrabContactDTO");
    }

    private List<NumbusterDTO> parseNumbusterDTOList(String responseBody) {
        TypeReference<List<NumbusterDTO>> typeReference = new TypeReference<>() {
        };
        return parseDTOList(responseBody, typeReference, "NumbusterDTO");
    }

    private List<GetcontactDTO> parseGetcontactDTOList(String responseBody) {
        TypeReference<List<GetcontactDTO>> typeReference = new TypeReference<>() {
        };
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

}
