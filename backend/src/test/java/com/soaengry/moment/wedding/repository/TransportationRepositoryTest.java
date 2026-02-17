package com.soaengry.moment.wedding.repository;

import com.soaengry.moment.wedding.entity.Transportation;
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
class TransportationRepositoryTest {

    @Autowired
    private TransportationRepository transportationRepository;

    @Test
    @DisplayName("Transportation 저장 및 조회 테스트")
    void saveAndFindTransportation() {
        // given
        Transportation transportation = Transportation.create(
                1L,
                Transportation.TransportType.SUBWAY,
                "2호선 강남역",
                "3번 출구에서 도보 5분",
                1
        );

        // when
        Transportation saved = transportationRepository.save(transportation);
        Transportation found = transportationRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getWeddingId()).isEqualTo(1L);
        assertThat(found.getType()).isEqualTo(Transportation.TransportType.SUBWAY);
        assertThat(found.getTitle()).isEqualTo("2호선 강남역");
        assertThat(found.getDescription()).isEqualTo("3번 출구에서 도보 5분");
    }

    @Test
    @DisplayName("Wedding ID로 교통편 목록 조회 - orderIndex 순서대로")
    void findByWeddingIdOrderByOrderIndex() {
        // given
        transportationRepository.save(Transportation.create(1L, Transportation.TransportType.BUS, "버스", "설명", 3));
        transportationRepository.save(Transportation.create(1L, Transportation.TransportType.SUBWAY, "지하철", "설명", 1));
        transportationRepository.save(Transportation.create(1L, Transportation.TransportType.SHUTTLE, "셔틀", "설명", 2));

        // when
        List<Transportation> transportations = transportationRepository.findByWeddingIdOrderByOrderIndex(1L);

        // then
        assertThat(transportations).hasSize(3);
        assertThat(transportations.get(0).getTitle()).isEqualTo("지하철");
        assertThat(transportations.get(1).getTitle()).isEqualTo("셔틀");
        assertThat(transportations.get(2).getTitle()).isEqualTo("버스");
    }

    @Test
    @DisplayName("Transportation 업데이트 테스트")
    void updateTransportation() {
        // given
        Transportation transportation = Transportation.create(1L, Transportation.TransportType.SUBWAY, "지하철", "설명", 1);
        Transportation saved = transportationRepository.save(transportation);

        // when
        saved.update(Transportation.TransportType.BUS, "버스", "변경된 설명", 5);
        Transportation updated = transportationRepository.save(saved);

        // then
        assertThat(updated.getType()).isEqualTo(Transportation.TransportType.BUS);
        assertThat(updated.getTitle()).isEqualTo("버스");
        assertThat(updated.getDescription()).isEqualTo("변경된 설명");
        assertThat(updated.getOrderIndex()).isEqualTo(5);
    }
}