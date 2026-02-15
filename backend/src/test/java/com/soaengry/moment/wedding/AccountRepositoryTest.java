package com.soaengry.moment.wedding;

import com.soaengry.moment.wedding.entity.Account;
import com.soaengry.moment.wedding.repository.AccountRepository;
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
    @DisplayName("AccountGroup ID로 계좌 개수 조회")
    void countByAccountGroupId() {
        // given
        accountRepository.save(Account.create(1L, "신한은행", "088", "110-111-111111", "김철수", null, 1));
        accountRepository.save(Account.create(1L, "국민은행", "004", "220-222-222222", "박철수", null, 2));
        accountRepository.save(Account.create(2L, "우리은행", "020", "330-333-333333", "이철수", null, 1));

        // when
        long count = accountRepository.countByAccountGroupId(1L);

        // then
        assertThat(count).isEqualTo(2);
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