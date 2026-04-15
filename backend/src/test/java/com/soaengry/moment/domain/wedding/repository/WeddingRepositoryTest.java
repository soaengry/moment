package com.soaengry.moment.domain.wedding.repository;

import com.soaengry.moment.config.TestSchemaConfig;
import com.soaengry.moment.domain.event.entity.Event;
import com.soaengry.moment.domain.event.entity.EventType;
import com.soaengry.moment.domain.event.repository.EventRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestSchemaConfig.class)
class WeddingRepositoryTest {

    @Autowired
    private WeddingRepository weddingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private Event createTestEvent(String slug) {
        User user = userRepository.save(User.builder()
                .email("wedding_repo_" + System.nanoTime() + "@test.com")
                .nickname("user_" + System.nanoTime())
                .isEmailVerified(true)
                .build());
        return eventRepository.save(Event.builder()
                .user(user)
                .title("테스트 이벤트")
                .type(EventType.WEDDING)
                .date(LocalDateTime.of(2026, 12, 25, 14, 0))
                .slug(slug + "_" + System.nanoTime())
                .build());
    }

    @Test
    @DisplayName("Wedding 저장 및 조회 테스트")
    void saveAndFindWedding() {
        // given
        Event event = createTestEvent("wedding-repo-test");
        Wedding wedding = Wedding.builder()
                .event(event)
                .notice("주차는 건물 지하 2층에서 가능합니다.")
                .parkingInfo("건물 지하 2-3층 무료 주차 가능 (3시간)")
                .mealInfo("뷔페 식사 제공")
                .greeting("환영합니다")
                .build();

        // when
        Wedding saved = weddingRepository.save(wedding);
        Wedding found = weddingRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getNotice()).isEqualTo("주차는 건물 지하 2층에서 가능합니다.");
        assertThat(found.getParkingInfo()).isEqualTo("건물 지하 2-3층 무료 주차 가능 (3시간)");
        assertThat(found.getMealInfo()).isEqualTo("뷔페 식사 제공");
        assertThat(found.getGreeting()).isEqualTo("환영합니다");
        assertThat(found.getEvent().getId()).isEqualTo(event.getId());
    }

    @Test
    @DisplayName("Wedding 업데이트 테스트")
    void updateWedding() {
        // given
        Event event = createTestEvent("wedding-repo-update");
        Wedding wedding = Wedding.builder()
                .event(event)
                .notice("원래 유의사항")
                .build();
        Wedding saved = weddingRepository.save(wedding);

        // when
        saved.update("변경된 유의사항", "변경된 주차정보", "변경된 식사정보", "변경된 인사말");
        Wedding updated = weddingRepository.save(saved);

        // then
        assertThat(updated.getNotice()).isEqualTo("변경된 유의사항");
        assertThat(updated.getParkingInfo()).isEqualTo("변경된 주차정보");
        assertThat(updated.getMealInfo()).isEqualTo("변경된 식사정보");
        assertThat(updated.getGreeting()).isEqualTo("변경된 인사말");
    }
}
