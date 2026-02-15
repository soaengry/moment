package com.soaengry.moment.wedding.service;

import com.soaengry.moment.wedding.dto.request.*;
import com.soaengry.moment.wedding.dto.response.*;
import com.soaengry.moment.wedding.entity.Account;
import com.soaengry.moment.wedding.entity.AccountGroup;
import com.soaengry.moment.wedding.entity.Couple;
import com.soaengry.moment.wedding.entity.Wedding;
import com.soaengry.moment.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.wedding.exception.WeddingException;
import com.soaengry.moment.wedding.repository.AccountGroupRepository;
import com.soaengry.moment.wedding.repository.AccountRepository;
import com.soaengry.moment.wedding.repository.WeddingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class WeddingServiceTest {

    @Autowired
    private WeddingService weddingService;

    @Autowired
    private WeddingRepository weddingRepository;

    @Autowired
    private AccountGroupRepository accountGroupRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Wedding 생성 테스트")
    void createWedding() {
        // given
        WeddingRequest request = new WeddingRequest(
                "김철수 ❤️ 이영희 결혼식",
                LocalDateTime.of(2024, 6, 15, 14, 0),
                "그랜드컨벤션센터",
                "서울시 강남구 테헤란로 123",
                "3층 그랜드홀",
                37.5012345,
                127.0398765,
                "02-1234-5678",
                "https://example.com/map.jpg",
                "편안한 캐주얼 복장",
                "주차는 건물 지하 2층에서 가능합니다.",
                "건물 지하 2-3층 무료 주차 가능 (3시간)",
                "뷔페 식사 제공"
        );

        // when
        WeddingResponse response = weddingService.createWedding(request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("김철수 ❤️ 이영희 결혼식");
        assertThat(response.venueName()).isEqualTo("그랜드컨벤션센터");
    }

    @Test
    @DisplayName("Wedding 조회 테스트")
    void getWedding() {
        // given
        Wedding wedding = weddingRepository.save(Wedding.create(
                "제목", LocalDateTime.now(), "장소", "주소", null,
                37.5, 127.0, null, null, null, null, null, null
        ));

        // when
        WeddingResponse response = weddingService.getWedding(wedding.getId());

        // then
        assertThat(response.id()).isEqualTo(wedding.getId());
        assertThat(response.title()).isEqualTo("제목");
    }

    @Test
    @DisplayName("Wedding 업데이트 테스트")
    void updateWedding() {
        // given
        Wedding wedding = weddingRepository.save(Wedding.create(
                "원제목", LocalDateTime.now(), "원장소", "주소", null,
                37.5, 127.0, null, null, null, null, null, null
        ));

        WeddingRequest updateRequest = new WeddingRequest(
                "변경된 제목",
                LocalDateTime.of(2024, 7, 20, 15, 0),
                "변경된 장소",
                "변경된 주소",
                null, 37.6, 127.1, null, null, null, null, null, null
        );

        // when
        WeddingResponse response = weddingService.updateWedding(wedding.getId(), updateRequest);

        // then
        assertThat(response.title()).isEqualTo("변경된 제목");
        assertThat(response.venueName()).isEqualTo("변경된 장소");
    }

    @Test
    @DisplayName("Wedding 삭제 테스트")
    void deleteWedding() {
        // given
        Wedding wedding = weddingRepository.save(Wedding.create(
                "제목", LocalDateTime.now(), "장소", "주소", null,
                37.5, 127.0, null, null, null, null, null, null
        ));

        // when
        weddingService.deleteWedding(wedding.getId());

        // then
        assertThat(weddingRepository.findById(wedding.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 Wedding 조회 시 예외 발생")
    void getWeddingNotFound() {
        // when & then
        assertThatThrownBy(() -> weddingService.getWedding(999L))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.WEDDING_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Couple 생성 테스트")
    void createCouple() {
        // given
        Wedding wedding = weddingRepository.save(Wedding.create(
                "제목", LocalDateTime.now(), "장소", "주소", null,
                37.5, 127.0, null, null, null, null, null, null
        ));

        CoupleRequest request = new CoupleRequest(
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
        CoupleResponse response = weddingService.createCouple(wedding.getId(), request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.weddingId()).isEqualTo(wedding.getId());
        assertThat(response.name()).isEqualTo("김철수");
        assertThat(response.role()).isEqualTo(Couple.CoupleRole.GROOM);
    }

    @Test
    @DisplayName("Wedding의 Couple 목록 조회 테스트")
    void getCouplesByWedding() {
        // given
        Wedding wedding = weddingRepository.save(Wedding.create(
                "제목", LocalDateTime.now(), "장소", "주소", null,
                37.5, 127.0, null, null, null, null, null, null
        ));

        weddingService.createCouple(wedding.getId(), new CoupleRequest(
                Couple.CoupleRole.GROOM, "김철수", "김아버지", "박어머니",
                true, true, "010-1111-1111", null, "신랑입니다"
        ));

        weddingService.createCouple(wedding.getId(), new CoupleRequest(
                Couple.CoupleRole.BRIDE, "이영희", "이아버지", "최어머니",
                true, false, "010-2222-2222", null, "신부입니다"
        ));

        // when
        List<CoupleResponse> couples = weddingService.getCouplesByWedding(wedding.getId());

        // then
        assertThat(couples).hasSize(2);
        assertThat(couples.get(0).role()).isEqualTo(Couple.CoupleRole.BRIDE);
        assertThat(couples.get(1).role()).isEqualTo(Couple.CoupleRole.GROOM);
    }

    @Test
    @DisplayName("Schedule 생성 테스트")
    void createSchedule() {
        // given
        Wedding wedding = weddingRepository.save(Wedding.create(
                "제목", LocalDateTime.now(), "장소", "주소", null,
                37.5, 127.0, null, null, null, null, null, null
        ));

        ScheduleRequest request = new ScheduleRequest(
                LocalTime.of(14, 0),
                "신랑 신부 입장",
                "양가 부모님과 함께 입장합니다",
                1
        );

        // when
        ScheduleResponse response = weddingService.createSchedule(wedding.getId(), request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("신랑 신부 입장");
        assertThat(response.time()).isEqualTo(LocalTime.of(14, 0));
    }

    @Test
    @DisplayName("AccountGroup 생성 시 최대 3개 제한 테스트")
    void createAccountGroupLimitExceeded() {
        // given
        Wedding wedding = weddingRepository.save(Wedding.create(
                "제목", LocalDateTime.now(), "장소", "주소", null,
                37.5, 127.0, null, null, null, null, null, null
        ));

        accountGroupRepository.save(AccountGroup.create(wedding.getId(), AccountGroup.Side.GROOM, "신랑측", 1));
        accountGroupRepository.save(AccountGroup.create(wedding.getId(), AccountGroup.Side.BRIDE, "신부측", 2));
        accountGroupRepository.save(AccountGroup.create(wedding.getId(), AccountGroup.Side.BOTH, "양가", 3));

        AccountGroupRequest request = new AccountGroupRequest(
                AccountGroup.Side.GROOM, "네번째 그룹", 4
        );

        // when & then
        assertThatThrownBy(() -> weddingService.createAccountGroup(wedding.getId(), request))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.ACCOUNT_GROUP_LIMIT_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("Account 생성 시 최대 2개 제한 테스트")
    void createAccountLimitExceeded() {
        // given
        Wedding wedding = weddingRepository.save(Wedding.create(
                "제목", LocalDateTime.now(), "장소", "주소", null,
                37.5, 127.0, null, null, null, null, null, null
        ));

        AccountGroup group = accountGroupRepository.save(
                AccountGroup.create(wedding.getId(), AccountGroup.Side.GROOM, "신랑측", 1)
        );

        accountRepository.save(Account.create(group.getId(), "신한은행", "088", "111-111-111111", "김철수", null, 1));
        accountRepository.save(Account.create(group.getId(), "국민은행", "004", "222-222-222222", "박철수", null, 2));

        AccountRequest request = new AccountRequest(
                "우리은행", "020", "333-333-333333", "이철수", null, 3
        );

        // when & then
        assertThatThrownBy(() -> weddingService.createAccount(group.getId(), request))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.ACCOUNT_LIMIT_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("전체 Wedding 정보 조회 테스트")
    void getWeddingInfo() {
        // given
        Wedding wedding = weddingRepository.save(Wedding.create(
                "제목", LocalDateTime.now(), "장소", "주소", null,
                37.5, 127.0, null, null, null, null, null, null
        ));

        weddingService.createCouple(wedding.getId(), new CoupleRequest(
                Couple.CoupleRole.GROOM, "김철수", "김아버지", "박어머니",
                true, true, "010-1111-1111", null, "신랑입니다"
        ));

        weddingService.createSchedule(wedding.getId(), new ScheduleRequest(
                LocalTime.of(14, 0), "입장", "설명", 1
        ));

        AccountGroupResponse group = weddingService.createAccountGroup(wedding.getId(),
                new AccountGroupRequest(AccountGroup.Side.GROOM, "신랑측", 1)
        );

        weddingService.createAccount(group.id(),
                new AccountRequest("신한은행", "088", "111-111-111111", "김철수", null, 1)
        );

        // when
        WeddingInfoResponse response = weddingService.getWeddingInfo(wedding.getId());

        // then
        assertThat(response.wedding().id()).isEqualTo(wedding.getId());
        assertThat(response.couples()).hasSize(1);
        assertThat(response.schedules()).hasSize(1);
        assertThat(response.accountGroups()).hasSize(1);
        assertThat(response.accountGroups().get(0).accounts()).hasSize(1);
    }
}