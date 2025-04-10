package ru.infoza.bot.service.cldb;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CldbEmailService extends AbstractCldbService {

    public CldbEmailService(WebClient.Builder webClientBuilder) {
        super(webClientBuilder);
    }

    public Mono<String> getCloudEmailInfo(String email) {
        return fetchInfo("/emails/" + email);
    }
}
