package ru.infoza.bot.service.infoza;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import ru.infoza.bot.model.infoza.InfozaPhone;
import ru.infoza.bot.model.infoza.InfozaPhoneRem;
import ru.infoza.bot.model.infoza.InfozaPhoneRequest;
import ru.infoza.bot.repository.infoza.InfozaPhoneRemRepository;
import ru.infoza.bot.repository.infoza.InfozaPhoneRepository;
import ru.infoza.bot.repository.infoza.InfozaPhoneRequestRepository;

import java.io.IOException;
import java.util.List;

@Service
public class InfozaPhoneService {

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
        HttpClient httpClient = HttpClients.createDefault();

        // Создаем объект HttpGet с указанным URL
        HttpGet httpGet = new HttpGet(apiUrl);

        try {
            // Выполняем запрос и получаем ответ
            HttpResponse response = httpClient.execute(httpGet);

            // Проверяем статус код ответа
            if (response.getStatusLine().getStatusCode() == 200) {
                // Извлекаем содержимое ответа как строку
                return EntityUtils.toString(response.getEntity()).replaceAll("<br />", "");
            } else {
                // В случае ошибки выводим сообщение
                System.out.println("HTTP Request Failed with error code " + response.getStatusLine().getStatusCode());
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Закрываем ресурсы
            httpGet.releaseConnection();
        }
    }
}
