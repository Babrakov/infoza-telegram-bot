package ru.infoza.bot.service.cldb;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RequiredArgsConstructor
public abstract class AbstractCldbService {

    private final WebClient.Builder webClientBuilder;
    @Value("${api.url}")
    private String apiUrl;
    @Value("${api.key}")
    private String apiKey;

    protected Mono<String> fetchInfo(String path) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
        return webClientBuilder.baseUrl(apiUrl)
                .build()
                .mutate()
                .exchangeStrategies(strategies)
                .build()
                .get()
                .uri(path)
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(120));
    }

}
