package com.soaengry.moment.wedding;

import com.soaengry.moment.wedding.entity.Couple;
import com.soaengry.moment.wedding.repository.CoupleRepository;
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
class CoupleRepositoryTest {

    @Autowired
    private CoupleRepository coupleRepository;

    @Test
    @DisplayName("Couple 저장 및 조회 테스트")
    void saveAndFindCouple() {
        // given
        Couple groom = Couple.create(
                1L,
                Couple.CoupleRole.GROOM,
                "김철수",
                "김아버지",
                "박어머니",
                true,
                true,
                "010-1234-5678",
                "https://example.com/groom.jpg",
                "안녕하세요. 신랑 김철수입니다."
        );

        // when
        Couple saved = coupleRepository.save(groom);
        Couple found = coupleRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getWeddingId()).isEqualTo(1L);
        assertThat(found.getRole()).isEqualTo(Couple.CoupleRole.GROOM);
        assertThat(found.getName()).isEqualTo("김철수");
        assertThat(found.getIsFatherAlive()).isTrue();
        assertThat(found.getIsMotherAlive()).isTrue();
    }

    @Test
    @DisplayName("Wedding ID로 신랑신부 조회 테스트")
    void findByWeddingId() {
        // given
        Couple groom = Couple.create(1L, Couple.CoupleRole.GROOM, "김철수", "김아버지", "박어머니",
                true, true, "010-1111-1111", null, "신랑입니다");
        Couple bride = Couple.create(1L, Couple.CoupleRole.BRIDE, "이영희", "이아버지", "최어머니",
                true, false, "010-2222-2222", null, "신부입니다");

        coupleRepository.save(groom);
        coupleRepository.save(bride);

        // when
        List<Couple> couples = coupleRepository.findByWeddingIdOrderByRole(1L);

        // then
        assertThat(couples).hasSize(2);
        assertThat(couples.get(0).getRole()).isEqualTo(Couple.CoupleRole.BRIDE);
        assertThat(couples.get(1).getRole()).isEqualTo(Couple.CoupleRole.GROOM);
    }

    @Test
    @DisplayName("Couple 업데이트 테스트 - 부모님 생존 여부 변경")
    void updateCouple() {
        // given
        Couple couple = Couple.create(1L, Couple.CoupleRole.GROOM, "김철수", "김아버지", "박어머니",
                true, true, "010-1111-1111", null, "안녕하세요");
        Couple saved = coupleRepository.save(couple);

        // when
        saved.update("김철수", "김아버지", "박어머니", false, true,
                "010-9999-9999", "https://new.jpg", "변경된 소개");
        Couple updated = coupleRepository.save(saved);

        // then
        assertThat(updated.getIsFatherAlive()).isFalse();
        assertThat(updated.getIsMotherAlive()).isTrue();
        assertThat(updated.getContact()).isEqualTo("010-9999-9999");
        assertThat(updated.getIntroduction()).isEqualTo("변경된 소개");
    }
}
