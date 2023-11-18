package ru.infoza.bot.service.infoza;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;

import static ru.infoza.bot.util.BotConstants.ERROR_TEXT;


@Slf4j
@Service
public class InfozaPhoneService {

    @Value("${api.key}")
    String apiKey;

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
        String apiUrl = "http://phones.local/?pho=7" + phoneNumber;

        // Создаем HttpClient
        // Создаем настройки для тайм-аута
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(20000) // Время ожидания данных от сервера (максимальное время выполнения запроса)
//                .setConnectTimeout(10000) // Время ожидания установки соединения с сервером
//                .setConnectionRequestTimeout(10000) // Время ожидания получения соединения из пула
                .build();

        // Создаем HttpClient с настройками тайм-аута
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // Создаем объект HttpGet с указанным URL
        HttpGet httpGet = new HttpGet(apiUrl);

        try {
            // Выполняем запрос и получаем ответ
            HttpResponse response = httpClient.execute(httpGet);

            // Проверяем статус код ответа
            if (response.getStatusLine().getStatusCode() == 200) {
                // Извлекаем содержимое ответа как строку
                return EntityUtils.toString(response.getEntity());
            } else {
                // В случае ошибки выводим сообщение
                log.error("HTTP Request Failed with error code " + response.getStatusLine().getStatusCode());
                return "";
            }
        } catch (IOException e) {
            // В случае исключения (тайм-аута или других ошибок), возвращаем пустую строку
            log.error("HTTP Request Failed with exception: " + e.getMessage());
            return "";
        } finally {
            // Закрываем ресурсы
            httpGet.releaseConnection();
            try {
                httpClient.close();
            } catch (IOException e) {
                // Обработка исключения при закрытии HttpClient
                log.error(ERROR_TEXT + e.getMessage());
            }
        }
    }

    public List<GrabContactDTO> getGrabcontactInfo(String phoneNumber) {
        // Формируем URL с параметром телефона
        String grabContactUrl = apiUrl + "/api/v1/phones/" + phoneNumber;

        // Создаем HttpClient
        // Создаем настройки для тайм-аута
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(10000) // Время ожидания данных от сервера (максимальное время выполнения запроса)
                .setConnectTimeout(10000) // Время ожидания установки соединения с сервером
                .setConnectionRequestTimeout(10000) // Время ожидания получения соединения из пула
                .build();

        // Создаем HttpClient с настройками тайм-аута
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // Создаем объект HttpGet с указанным URL
        HttpGet httpGet = new HttpGet(grabContactUrl);

        // Добавляем заголовок с токеном аутентификации
        httpGet.setHeader("Authorization", "Bearer "+apiKey);

        try {
            // Выполняем запрос и получаем ответ
            HttpResponse response = httpClient.execute(httpGet);

            // Проверяем статус код ответа
            if (response.getStatusLine().getStatusCode() == 200) {
                // Извлекаем содержимое ответа как строку
                String responseBody = EntityUtils.toString(response.getEntity()).replaceAll("<br />", "");
                return parseGrabContactDTOList(responseBody);
            } else {
                // В случае ошибки выводим сообщение
                log.error("HTTP Request Failed with error code " + response.getStatusLine().getStatusCode());
                return Collections.emptyList();
            }
        } catch (IOException e) {
            // В случае исключения (тайм-аута или других ошибок), возвращаем пустую строку
            log.error("HTTP Request Failed with exception: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            // Закрываем ресурсы
            httpGet.releaseConnection();
            try {
                httpClient.close();
            } catch (IOException e) {
                // Обработка исключения при закрытии HttpClient
                log.error(ERROR_TEXT + e.getMessage());
            }
        }
    }

    public List<NumbusterDTO> getNumbusterInfo(String phoneNumber) {
        // Формируем URL с параметром телефона
        String numbusterUrl = apiUrl + "/api/v1/numbuster/" + phoneNumber;

        // Создаем HttpClient
        // Создаем настройки для тайм-аута
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(10000) // Время ожидания данных от сервера (максимальное время выполнения запроса)
                .setConnectTimeout(10000) // Время ожидания установки соединения с сервером
                .setConnectionRequestTimeout(10000) // Время ожидания получения соединения из пула
                .build();

        // Создаем HttpClient с настройками тайм-аута
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // Создаем объект HttpGet с указанным URL
        HttpGet httpGet = new HttpGet(numbusterUrl);

        // Добавляем заголовок с токеном аутентификации
        httpGet.setHeader("Authorization", "Bearer "+apiKey);

        try {
            // Выполняем запрос и получаем ответ
            HttpResponse response = httpClient.execute(httpGet);

            // Проверяем статус код ответа
            if (response.getStatusLine().getStatusCode() == 200) {
                // Извлекаем содержимое ответа как строку
                String responseBody = EntityUtils.toString(response.getEntity());
                return parseNumbusterDTOList(responseBody);
            } else {
                // В случае ошибки выводим сообщение
                log.error("HTTP Request Failed with error code " + response.getStatusLine().getStatusCode());
                return Collections.emptyList();
            }
        } catch (IOException e) {
            // В случае исключения (тайм-аута или других ошибок), возвращаем пустую строку
            log.error("HTTP Request Failed with exception: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            // Закрываем ресурсы
            httpGet.releaseConnection();
            try {
                httpClient.close();
            } catch (IOException e) {
                // Обработка исключения при закрытии HttpClient
                log.error(ERROR_TEXT + e.getMessage());
            }
        }
    }

    public List<GetcontactDTO> getGetcontactInfo(String phoneNumber) {
        // Формируем URL с параметром телефона
        String getcontactUrl = apiUrl + "/api/v1/getcontact/" + phoneNumber;

        // Создаем HttpClient
        // Создаем настройки для тайм-аута
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(10000) // Время ожидания данных от сервера (максимальное время выполнения запроса)
                .setConnectTimeout(10000) // Время ожидания установки соединения с сервером
                .setConnectionRequestTimeout(10000) // Время ожидания получения соединения из пула
                .build();

        // Создаем HttpClient с настройками тайм-аута
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // Создаем объект HttpGet с указанным URL
        HttpGet httpGet = new HttpGet(getcontactUrl);

        // Добавляем заголовок с токеном аутентификации
        httpGet.setHeader("Authorization", "Bearer "+apiKey);

        try {
            // Выполняем запрос и получаем ответ
            HttpResponse response = httpClient.execute(httpGet);

            // Проверяем статус код ответа
            if (response.getStatusLine().getStatusCode() == 200) {
                // Извлекаем содержимое ответа как строку
                String responseBody = EntityUtils.toString(response.getEntity());
                return parseGetcontactDTOList(responseBody);
            } else {
                // В случае ошибки выводим сообщение
                log.error("HTTP Request Failed with error code " + response.getStatusLine().getStatusCode());
                return Collections.emptyList();
            }
        } catch (IOException e) {
            // В случае исключения (тайм-аута или других ошибок), возвращаем пустую строку
            log.error("HTTP Request Failed with exception: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            // Закрываем ресурсы
            httpGet.releaseConnection();
            try {
                httpClient.close();
            } catch (IOException e) {
                // Обработка исключения при закрытии HttpClient
                log.error(ERROR_TEXT + e.getMessage());
            }
        }
    }

    private List<GrabContactDTO> parseGrabContactDTOList(String responseBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            TypeReference<List<GrabContactDTO>> typeReference = new TypeReference<>() {};
            return objectMapper.readValue(responseBody, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Error parsing GrabContactDTO list: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<NumbusterDTO> parseNumbusterDTOList(String responseBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            TypeReference<List<NumbusterDTO>> typeReference = new TypeReference<>() {};
            return objectMapper.readValue(responseBody, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Error parsing NumbusterDTO list: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<GetcontactDTO> parseGetcontactDTOList(String responseBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            TypeReference<List<GetcontactDTO>> typeReference = new TypeReference<>() {};
            return objectMapper.readValue(responseBody, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Error parsing NumbusterDTO list: " + e.getMessage());
            return Collections.emptyList();
        }
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
        return infozaPhoneRequestRepository.findByIdZPAndInISTAndDtCREBetween(id,ist,startOfDay,endOfDay);
    }

    public List<InfozaPhoneRequestDTO> findRequestListByPhone(String phone){
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
            InfozaPhoneRequestDTO dto = new InfozaPhoneRequestDTO(inIST,vcFIO,vcORG,dtCRE);
            infozaPhoneRequestList.add(dto);
        }
        return infozaPhoneRequestList;
    }

}
