package com.soaengry.moment.wedding.repository;

import com.soaengry.moment.wedding.entity.Schedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ScheduleRepositoryTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Test
    @DisplayName("Schedule 저장 및 조회 테스트")
    void saveAndFindSchedule() {
        // given
        Schedule schedule = Schedule.create(
                1L,
                LocalTime.of(14, 0),
                "신랑 신부 입장",
                "양가 부모님과 함께 입장합니다",
                1
        );

        // when
        Schedule saved = scheduleRepository.save(schedule);
        Schedule found = scheduleRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getWeddingId()).isEqualTo(1L);
        assertThat(found.getTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(found.getTitle()).isEqualTo("신랑 신부 입장");
        assertThat(found.getOrderIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("Wedding ID로 식순 목록 조회 - orderIndex 순서대로")
    void findByWeddingIdOrderByOrderIndex() {
        // given
        scheduleRepository.save(Schedule.create(1L, LocalTime.of(14, 30), "축가", null, 3));
        scheduleRepository.save(Schedule.create(1L, LocalTime.of(14, 0), "입장", null, 1));
        scheduleRepository.save(Schedule.create(1L, LocalTime.of(14, 15), "성혼선언", null, 2));

        // when
        List<Schedule> schedules = scheduleRepository.findByWeddingIdOrderByOrderIndex(1L);

        // then
        assertThat(schedules).hasSize(3);
        assertThat(schedules.get(0).getTitle()).isEqualTo("입장");
        assertThat(schedules.get(1).getTitle()).isEqualTo("성혼선언");
        assertThat(schedules.get(2).getTitle()).isEqualTo("축가");
    }

    @Test
    @DisplayName("Schedule 업데이트 테스트")
    void updateSchedule() {
        // given
        Schedule schedule = Schedule.create(1L, LocalTime.of(14, 0), "입장", "설명", 1);
        Schedule saved = scheduleRepository.save(schedule);

        // when
        saved.update(LocalTime.of(15, 0), "변경된 제목", "변경된 설명", 5);
        Schedule updated = scheduleRepository.save(saved);

        // then
        assertThat(updated.getTime()).isEqualTo(LocalTime.of(15, 0));
        assertThat(updated.getTitle()).isEqualTo("변경된 제목");
        assertThat(updated.getDescription()).isEqualTo("변경된 설명");
        assertThat(updated.getOrderIndex()).isEqualTo(5);
    }
}