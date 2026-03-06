package com.soaengry.moment.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KakaoProperties.class)
public class KakaoConfig {

    @Bean
    public RestClient kakaoRestClient(KakaoProperties kakaoProperties) {
        return RestClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoProperties.restApiKey())
                .build();
    }
}
