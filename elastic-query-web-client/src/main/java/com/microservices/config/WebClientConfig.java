package com.microservices.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@LoadBalancerClient(
        name = "elastic-query-service",
        configuration = ElasticQueryServiceInstanceListSupplierConfig.class
)
public class WebClientConfig {
    private final ElasticQueryWebClientConfigData.WebClient webClientConfig;

    @Value("${security.default-client-registration-id}")
    private String defaultClientRegistrationId;

    @LoadBalanced
    @Bean("webClientBuilder")
    WebClient.Builder webClientBuilder(ClientRegistrationRepository clientRegistrationRepository,
                                       OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                        clientRegistrationRepository, oAuth2AuthorizedClientRepository
                );
        oauth2.setDefaultOAuth2AuthorizedClient(true);
        oauth2.setDefaultClientRegistrationId(defaultClientRegistrationId);

        return WebClient.builder()
                .baseUrl(webClientConfig.getBaseUrl())
                .defaultHeaders(headers -> {
                    headers.setContentType(MediaType.valueOf(webClientConfig.getContentType()));
                    headers.setAccept(List.of(MediaType.valueOf(webClientConfig.getAcceptType())));
                })
                .clientConnector(new ReactorClientHttpConnector(getHttpClient()))
                .apply(oauth2.oauth2Configuration())
                .codecs(clientCodecConfigurer -> clientCodecConfigurer
                        .defaultCodecs()
                        .maxInMemorySize(webClientConfig.getMaxInMemorySize()));
    }

    private HttpClient getHttpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, webClientConfig.getConnectTimeoutMs())
                .doOnConnected(connection -> {
                    connection.addHandlerLast(
                            new ReadTimeoutHandler(webClientConfig.getReadTimeoutMs(), TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(
                            new WriteTimeoutHandler(webClientConfig.getWriteTimeoutMs(), TimeUnit.MILLISECONDS)
                    );
                });
    }
}