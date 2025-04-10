package ru.infoza.bot.service.cldb;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CldbCarService extends AbstractCldbService {

    public CldbCarService(WebClient.Builder webClientBuilder) {
        super(webClientBuilder);
    }

    public Mono<String> getCloudCarInfo(String car) {
        return fetchInfo("/cars/" + car);
    }
}
