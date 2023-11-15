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
import ru.infoza.bot.dto.NumbusterDTO;
import ru.infoza.bot.model.infoza.InfozaPhone;
import ru.infoza.bot.model.infoza.InfozaPhoneRem;
import ru.infoza.bot.model.infoza.InfozaPhoneRequest;
import ru.infoza.bot.repository.infoza.InfozaPhoneRemRepository;
import ru.infoza.bot.repository.infoza.InfozaPhoneRepository;
import ru.infoza.bot.repository.infoza.InfozaPhoneRequestRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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

}