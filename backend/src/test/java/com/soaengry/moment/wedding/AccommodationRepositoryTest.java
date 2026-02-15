package com.soaengry.moment.wedding;

import com.soaengry.moment.wedding.entity.Accommodation;
import com.soaengry.moment.wedding.repository.AccommodationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccommodationRepositoryTest {

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Test
    @DisplayName("Accommodation 저장 및 조회 테스트")
    void saveAndFindAccommodation() {
        // given
        Accommodation accommodation = Accommodation.create(
                1L,
                "그랜드 호텔",
                "서울시 강남구 테헤란로 456",
                "02-9999-8888",
                "도보 5분",
                "20만원대",
                1
        );

        // when
        Accommodation saved = accommodationRepository.save(accommodation);
        Accommodation found = accommodationRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getWeddingId()).isEqualTo(1L);
        assertThat(found.getName()).isEqualTo("그랜드 호텔");
        assertThat(found.getAddress()).isEqualTo("서울시 강남구 테헤란로 456");
        assertThat(found.getPhone()).isEqualTo("02-9999-8888");
        assertThat(found.getDistance()).isEqualTo("도보 5분");
        assertThat(found.getPriceRange()).isEqualTo("20만원대");
    }

    @Test
    @DisplayName("Wedding ID로 숙박 목록 조회 - orderIndex 순서대로")
    void findByWeddingIdOrderByOrderIndex() {
        // given
        accommodationRepository.save(Accommodation.create(1L, "세번째 호텔", "주소3", null, null, null, 3));
        accommodationRepository.save(Accommodation.create(1L, "첫번째 호텔", "주소1", null, null, null, 1));
        accommodationRepository.save(Accommodation.create(1L, "두번째 호텔", "주소2", null, null, null, 2));

        // when
        List<Accommodation> accommodations = accommodationRepository.findByWeddingIdOrderByOrderIndex(1L);

        // then
        assertThat(accommodations).hasSize(3);
        assertThat(accommodations.get(0).getName()).isEqualTo("첫번째 호텔");
        assertThat(accommodations.get(1).getName()).isEqualTo("두번째 호텔");
        assertThat(accommodations.get(2).getName()).isEqualTo("세번째 호텔");
    }

    @Test
    @DisplayName("Accommodation 업데이트 테스트")
    void updateAccommodation() {
        // given
        Accommodation accommodation = Accommodation.create(1L, "원래 호텔", "원래 주소", "02-1111-1111",
                "도보 10분", "10만원대", 1);
        Accommodation saved = accommodationRepository.save(accommodation);

        // when
        saved.update("변경된 호텔", "변경된 주소", "02-2222-2222",
                "차량 5분", "30만원대", 5);
        Accommodation updated = accommodationRepository.save(saved);

        // then
        assertThat(updated.getName()).isEqualTo("변경된 호텔");
        assertThat(updated.getAddress()).isEqualTo("변경된 주소");
        assertThat(updated.getPhone()).isEqualTo("02-2222-2222");
        assertThat(updated.getDistance()).isEqualTo("차량 5분");
        assertThat(updated.getPriceRange()).isEqualTo("30만원대");
        assertThat(updated.getOrderIndex()).isEqualTo(5);
    }
}