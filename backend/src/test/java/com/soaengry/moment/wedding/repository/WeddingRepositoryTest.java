package com.soaengry.moment.wedding.repository;

import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.repository.WeddingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WeddingRepositoryTest {

    @Autowired
    private WeddingRepository weddingRepository;

    @Test
    @DisplayName("Wedding 저장 및 조회 테스트")
    void saveAndFindWedding() {
        // given
        Wedding wedding = Wedding.builder()
                .title("김철수 ❤️ 이영희 결혼식")
                .invitationId("test-invitation-id")
                .weddingDate(LocalDateTime.of(2024, 6, 15, 14, 0))
                .venueName("그랜드컨벤션센터")
                .venueAddress("서울시 강남구 테헤란로 123")
                .venueDetail("3층 그랜드홀")
                .venueLat(37.5012345)
                .venueLng(127.0398765)
                .venuePhone("02-1234-5678")
                .dressCode("편안한 캐주얼 복장")
                .notice("주차는 건물 지하 2층에서 가능합니다.")
                .parkingInfo("건물 지하 2-3층 무료 주차 가능 (3시간)")
                .mealInfo("뷔페 식사 제공")
                .build();

        // when
        Wedding saved = weddingRepository.save(wedding);
        Wedding found = weddingRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getTitle()).isEqualTo("김철수 ❤️ 이영희 결혼식");
        assertThat(found.getVenueName()).isEqualTo("그랜드컨벤션센터");
        assertThat(found.getVenueLat()).isEqualTo(37.5012345);
        assertThat(found.getVenueLng()).isEqualTo(127.0398765);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Wedding 업데이트 테스트")
    void updateWedding() throws InterruptedException {
        // given
        Wedding wedding = Wedding.builder()
                .title("원제목")
                .invitationId("test-invitation-id-2")
                .weddingDate(LocalDateTime.of(2024, 6, 15, 14, 0))
                .venueName("원장소")
                .venueAddress("원주소")
                .venueLat(37.5)
                .venueLng(127.0)
                .build();
        Wedding saved = weddingRepository.save(wedding);

        // when
        saved.updateTitle("변경된 제목");
        saved.updateWeddingDate(LocalDateTime.of(2024, 7, 20, 15, 0));
        saved.updateVenue("변경된 장소", "변경된 주소", "변경된 상세주소", 37.6, 127.1, "02-9999-8888");
        saved.updateDressCode("정장 착용");
        saved.updateNotice("변경된 안내사항");
        saved.updateParkingInfo("변경된 주차정보");
        saved.updateMealInfo("변경된 식사정보");
        Wedding updated = weddingRepository.save(saved);

        // then
        assertThat(updated.getTitle()).isEqualTo("변경된 제목");
        assertThat(updated.getVenueName()).isEqualTo("변경된 장소");
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updated.getCreatedAt());
    }
}