package com.soaengry.moment.domain.wedding.repository;

import com.soaengry.moment.config.TestSchemaConfig;
import com.soaengry.moment.domain.event.entity.Account;
import com.soaengry.moment.domain.event.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestSchemaConfig.class)
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Account 저장 및 조회 테스트")
    void saveAndFindAccount() {
        // given
        Account account = Account.create(
                1L,
                "신한은행",
                "088",
                "110-123-456789",
                "김철수",
                "https://qr.kakaopay.com/123456",
                1
        );

        // when
        Account saved = accountRepository.save(account);
        Account found = accountRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getId()).isNotNull();
        assertThat(found.getAccountGroupId()).isEqualTo(1L);
        assertThat(found.getBankName()).isEqualTo("신한은행");
        assertThat(found.getBankCode()).isEqualTo("088");
        assertThat(found.getAccountNumber()).isEqualTo("110-123-456789");
        assertThat(found.getAccountHolder()).isEqualTo("김철수");
        assertThat(found.getKakaoPayUrl()).isEqualTo("https://qr.kakaopay.com/123456");
    }

    @Test
    @DisplayName("AccountGroup ID로 계좌 목록 조회 - orderIndex 순서대로")
    void findByAccountGroupIdOrderByOrderIndex() {
        // given
        accountRepository.save(Account.create(1L, "신한은행", "088", "110-111-111111", "김철수", null, 2));
        accountRepository.save(Account.create(1L, "국민은행", "004", "220-222-222222", "박철수", null, 1));

        // when
        List<Account> accounts = accountRepository.findByAccountGroupIdOrderByOrderIndex(1L);

        // then
        assertThat(accounts).hasSize(2);
        assertThat(accounts.get(0).getBankName()).isEqualTo("국민은행");
        assertThat(accounts.get(1).getBankName()).isEqualTo("신한은행");
    }

    @Test
    @DisplayName("여러 그룹 ID로 계좌 일괄 조회 - N+1 방지용 배치 쿼리")
    void findByAccountGroupIdInOrderByOrderIndex_batchLoad() {
        // given: 그룹 1에 계좌 2개, 그룹 2에 계좌 1개
        accountRepository.save(Account.create(1L, "신한은행", "088", "110-111-111111", "김A", null, 2));
        accountRepository.save(Account.create(1L, "국민은행", "004", "220-222-222222", "김B", null, 1));
        accountRepository.save(Account.create(2L, "카카오뱅크", "090", "333-333-333333", "박C", null, 0));

        // when: 두 그룹을 한 번의 쿼리로 조회
        List<Account> all = accountRepository.findByAccountGroupIdInOrderByOrderIndex(List.of(1L, 2L));

        // then: 전체 3개, orderIndex 전역 오름차순 정렬 (0→1→2)
        assertThat(all).hasSize(3);
        assertThat(all.get(0).getOrderIndex()).isEqualTo(0); // 그룹2 순서0 (카카오뱅크)
        assertThat(all.get(1).getOrderIndex()).isEqualTo(1); // 그룹1 순서1 (국민은행)
        assertThat(all.get(2).getOrderIndex()).isEqualTo(2); // 그룹1 순서2 (신한은행)

        // 그룹별 분류 확인
        Map<Long, List<Account>> byGroup = all.stream()
                .collect(Collectors.groupingBy(Account::getAccountGroupId));
        assertThat(byGroup.get(1L)).hasSize(2);
        assertThat(byGroup.get(2L)).hasSize(1);
        assertThat(byGroup.get(2L).get(0).getBankName()).isEqualTo("카카오뱅크");
    }

    @Test
    @DisplayName("빈 그룹 ID 목록으로 조회하면 빈 리스트 반환")
    void findByAccountGroupIdInOrderByOrderIndex_emptyIds_returnsEmpty() {
        // given: 저장된 계좌가 있어도
        accountRepository.save(Account.create(1L, "신한은행", "088", "110-111-111111", "김A", null, 0));

        // when: 빈 목록으로 조회
        List<Account> result = accountRepository.findByAccountGroupIdInOrderByOrderIndex(List.of());

        // then: 빈 리스트
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Account 업데이트 테스트")
    void updateAccount() {
        // given
        Account account = Account.create(1L, "신한은행", "088", "110-111-111111", "김철수", null, 1);
        Account saved = accountRepository.save(account);

        // when
        saved.update("국민은행", "004", "220-222-222222", "이철수",
                "https://qr.kakaopay.com/new", 5);
        Account updated = accountRepository.save(saved);

        // then
        assertThat(updated.getBankName()).isEqualTo("국민은행");
        assertThat(updated.getBankCode()).isEqualTo("004");
        assertThat(updated.getAccountNumber()).isEqualTo("220-222-222222");
        assertThat(updated.getAccountHolder()).isEqualTo("이철수");
        assertThat(updated.getKakaoPayUrl()).isEqualTo("https://qr.kakaopay.com/new");
        assertThat(updated.getOrderIndex()).isEqualTo(5);
    }
}
