package com.soaengry.moment.global.service;

import com.soaengry.moment.global.config.KakaoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KakaoGeocodingService {

    private static final String GEOCODING_URL = "https://dapi.kakao.com/v2/local/search/address.json";

    private final RestClient restClient;

    public KakaoGeocodingService(RestClient kakaoRestClient) {
        this.restClient = kakaoRestClient;
    }

    public record Coordinate(double lat, double lng) {}

    /**
     * 주소를 위도/경도로 변환합니다.
     * @param address 변환할 주소
     * @return 좌표 (실패 시 null)
     */
    @SuppressWarnings("unchecked")
    public Coordinate geocode(String address) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(GEOCODING_URL + "?query={query}", address)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                log.warn("Kakao Geocoding API 응답이 null입니다. address={}", address);
                return null;
            }

            List<Map<String, Object>> documents = (List<Map<String, Object>>) response.get("documents");

            if (documents == null || documents.isEmpty()) {
                log.warn("Kakao Geocoding 결과 없음. address={}", address);
                return null;
            }

            Map<String, Object> first = documents.get(0);
            double lng = Double.parseDouble((String) first.get("x"));
            double lat = Double.parseDouble((String) first.get("y"));

            return new Coordinate(lat, lng);
        } catch (Exception e) {
            log.error("Kakao Geocoding API 호출 실패. address={}", address, e);
            return null;
        }
    }
}
