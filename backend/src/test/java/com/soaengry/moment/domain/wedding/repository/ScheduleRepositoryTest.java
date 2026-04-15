package com.soaengry.moment.wedding.repository;

import com.soaengry.moment.config.TestSchemaConfig;
import com.soaengry.moment.domain.event.entity.Schedule;
import com.soaengry.moment.domain.event.repository.ScheduleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestSchemaConfig.class)
class ScheduleRepositoryTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Test
    @DisplayName("Schedule 저장 및 조회 테스트")
    void saveAndFindSchedule() {
        // given
        Schedule schedule = Schedule.create(
                1L,
                "신랑 신부 입장",
                "양가 부모님과 함께 입장합니다",
                1
        );

        // when
        Schedule saved = scheduleRepository.save(schedule);
        Schedule found = scheduleRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getEventId()).isEqualTo(1L);
        assertThat(found.getTitle()).isEqualTo("신랑 신부 입장");
        assertThat(found.getOrderIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("Event ID로 식순 목록 조회 - orderIndex 순서대로")
    void findByEventIdOrderByOrderIndex() {
        // given
        scheduleRepository.save(Schedule.create(1L, "축가", null, 3));
        scheduleRepository.save(Schedule.create(1L, "입장", null, 1));
        scheduleRepository.save(Schedule.create(1L, "성혼선언", null, 2));

        // when
        List<Schedule> schedules = scheduleRepository.findByEventIdOrderByOrderIndex(1L);

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
        Schedule schedule = Schedule.create(1L, "입장", "설명", 1);
        Schedule saved = scheduleRepository.save(schedule);

        // when
        saved.update("변경된 제목", "변경된 설명", 5);
        Schedule updated = scheduleRepository.save(saved);

        // then
        assertThat(updated.getTitle()).isEqualTo("변경된 제목");
        assertThat(updated.getDescription()).isEqualTo("변경된 설명");
        assertThat(updated.getOrderIndex()).isEqualTo(5);
    }
}
