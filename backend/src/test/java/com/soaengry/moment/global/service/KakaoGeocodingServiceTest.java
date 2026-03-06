package com.soaengry.moment.global.service;

import com.soaengry.moment.global.service.KakaoGeocodingService.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class KakaoGeocodingServiceTest {

    @MockitoBean(name = "kakaoRestClient")
    private RestClient restClient;

    @Autowired
    private KakaoGeocodingService kakaoGeocodingService;

    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // RestClient mocking 체인 설정
        requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), ArgumentMatchers.<Object>any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("유효한 주소 → 좌표 변환 성공")
    @SuppressWarnings("unchecked")
    void geocode_ValidAddress_Success() {
        // given
        String address = "서울특별시 강남구 테헤란로 152";

        Map<String, Object> mockResponse = Map.of(
                "documents", List.of(
                        Map.of(
                                "x", "127.0361234",
                                "y", "37.5012345"
                        )
                )
        );

        when(responseSpec.body(Map.class)).thenReturn(mockResponse);

        // when
        Coordinate result = kakaoGeocodingService.geocode(address);

        // then
        assertThat(result).isNotNull();
        assertThat(result.lat()).isEqualTo(37.5012345);
        assertThat(result.lng()).isEqualTo(127.0361234);

        System.out.println("✅ 유효한 주소 → 좌표 변환 성공");
        System.out.println("   - 주소: " + address);
        System.out.println("   - 좌표: (" + result.lat() + ", " + result.lng() + ")");
    }

    @Test
    @DisplayName("빈 documents 배열 → null 반환")
    @SuppressWarnings("unchecked")
    void geocode_EmptyDocuments_ReturnsNull() {
        // given
        String address = "존재하지 않는 주소";

        Map<String, Object> mockResponse = Map.of(
                "documents", List.of()
        );

        when(responseSpec.body(Map.class)).thenReturn(mockResponse);

        // when
        Coordinate result = kakaoGeocodingService.geocode(address);

        // then
        assertThat(result).isNull();

        System.out.println("✅ 빈 documents 배열 → null 반환");
        System.out.println("   - 주소: " + address);
    }

    @Test
    @DisplayName("API 예외 발생 → null 반환")
    @SuppressWarnings("unchecked")
    void geocode_ApiException_ReturnsNull() {
        // given
        String address = "서울특별시";

        when(responseSpec.body(Map.class)).thenThrow(new RuntimeException("API 호출 실패"));

        // when
        Coordinate result = kakaoGeocodingService.geocode(address);

        // then
        assertThat(result).isNull();

        System.out.println("✅ API 예외 발생 → null 반환 (예외 throw 안 함)");
        System.out.println("   - 주소: " + address);
    }

    @Test
    @DisplayName("null 응답 → null 반환")
    @SuppressWarnings("unchecked")
    void geocode_NullResponse_ReturnsNull() {
        // given
        String address = "테스트 주소";

        when(responseSpec.body(Map.class)).thenReturn(null);

        // when
        Coordinate result = kakaoGeocodingService.geocode(address);

        // then
        assertThat(result).isNull();

        System.out.println("✅ null 응답 → null 반환");
        System.out.println("   - 주소: " + address);
    }
}
