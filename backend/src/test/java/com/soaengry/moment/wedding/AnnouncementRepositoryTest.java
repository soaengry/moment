package com.soaengry.moment.wedding;

import com.soaengry.moment.wedding.entity.Announcement;
import com.soaengry.moment.wedding.repository.AnnouncementRepository;
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
class AnnouncementRepositoryTest {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Test
    @DisplayName("Announcement 저장 및 조회 테스트")
    void saveAndFindAnnouncement() {
        // given
        Announcement announcement = Announcement.create(
                1L,
                "주차 안내",
                "주차는 건물 지하 2층에서 가능합니다.",
                false
        );

        // when
        Announcement saved = announcementRepository.save(announcement);
        Announcement found = announcementRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getWeddingId()).isEqualTo(1L);
        assertThat(found.getTitle()).isEqualTo("주차 안내");
        assertThat(found.getContent()).isEqualTo("주차는 건물 지하 2층에서 가능합니다.");
        assertThat(found.getIsPinned()).isFalse();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Wedding ID로 공지사항 목록 조회 - 고정된 것 먼저, 그 다음 최신순")
    void findByWeddingIdOrderByIsPinnedDescCreatedAtDesc() throws InterruptedException {
        // given
        Announcement normal1 = Announcement.create(1L, "일반1", "내용1", false);
        announcementRepository.save(normal1);

        Thread.sleep(10); // createdAt 차이를 위해

        Announcement pinned = Announcement.create(1L, "고정", "내용", true);
        announcementRepository.save(pinned);

        Thread.sleep(10);

        Announcement normal2 = Announcement.create(1L, "일반2", "내용2", false);
        announcementRepository.save(normal2);

        // when
        List<Announcement> announcements = announcementRepository.findByWeddingIdOrderByIsPinnedDescCreatedAtDesc(1L);

        // then
        assertThat(announcements).hasSize(3);
        assertThat(announcements.get(0).getTitle()).isEqualTo("고정");
        assertThat(announcements.get(1).getTitle()).isEqualTo("일반2");
        assertThat(announcements.get(2).getTitle()).isEqualTo("일반1");
    }

    @Test
    @DisplayName("Announcement 업데이트 테스트")
    void updateAnnouncement() {
        // given
        Announcement announcement = Announcement.create(1L, "원래 제목", "원래 내용", false);
        Announcement saved = announcementRepository.save(announcement);

        // when
        saved.update("변경된 제목", "변경된 내용", true);
        Announcement updated = announcementRepository.save(saved);

        // then
        assertThat(updated.getTitle()).isEqualTo("변경된 제목");
        assertThat(updated.getContent()).isEqualTo("변경된 내용");
        assertThat(updated.getIsPinned()).isTrue();
        assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
    }

    @Test
    @DisplayName("Announcement 고정/해제 테스트")
    void pinAndUnpinAnnouncement() {
        // given
        Announcement announcement = Announcement.create(1L, "제목", "내용", false);
        Announcement saved = announcementRepository.save(announcement);

        // when - pin
        saved.pin();
        Announcement pinned = announcementRepository.save(saved);

        // then
        assertThat(pinned.getIsPinned()).isTrue();

        // when - unpin
        pinned.unpin();
        Announcement unpinned = announcementRepository.save(pinned);

        // then
        assertThat(unpinned.getIsPinned()).isFalse();
    }
}