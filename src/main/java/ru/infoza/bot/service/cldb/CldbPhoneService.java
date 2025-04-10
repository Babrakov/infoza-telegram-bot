package ru.infoza.bot.service.cldb;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CldbPhoneService extends AbstractCldbService {

    public CldbPhoneService(WebClient.Builder webClientBuilder) {
        super(webClientBuilder);
    }

    public Mono<String> getCloudPhoneInfo(String phone) {
        return fetchInfo("/phones/" + phone);
    }
}
