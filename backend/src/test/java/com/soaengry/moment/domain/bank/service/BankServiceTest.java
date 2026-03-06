package com.soaengry.moment.domain.bank.service;

import com.soaengry.moment.domain.bank.entity.Bank;
import com.soaengry.moment.domain.bank.entity.BankPrefix;
import com.soaengry.moment.domain.bank.repository.BankPrefixRepository;
import com.soaengry.moment.domain.bank.repository.BankRepository;
import com.soaengry.moment.domain.bank.service.BankService.BankInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BankServiceTest {

    @Autowired
    private BankService bankService;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private BankPrefixRepository bankPrefixRepository;

    private Bank shinhanBank;
    private Bank kbBank;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트 전 데이터 정리
        bankPrefixRepository.deleteAll();
        bankRepository.deleteAll();

        // 은행 생성 (Reflection 사용)
        shinhanBank = createBank("088", "신한은행");
        kbBank = createBank("004", "KB국민은행");

        bankRepository.save(shinhanBank);
        bankRepository.save(kbBank);

        // Prefix 생성 (Reflection 사용)
        // 신한은행: 4자리 (1101), 3자리 (140)
        bankPrefixRepository.save(createBankPrefix(shinhanBank, "1101"));
        bankPrefixRepository.save(createBankPrefix(shinhanBank, "140"));

        // KB국민은행: 3자리 (123)
        bankPrefixRepository.save(createBankPrefix(kbBank, "123"));
    }

    @Test
    @DisplayName("4자리 prefix로 은행 조회 성공")
    void findBankByAccountNumber_4DigitPrefix_Success() {
        // given
        String accountNumber = "1101-1234-5678";

        // when
        Optional<BankInfo> result = bankService.findBankByAccountNumber(accountNumber);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().bankCode()).isEqualTo("088");
        assertThat(result.get().bankName()).isEqualTo("신한은행");

        System.out.println("✅ 4자리 prefix 조회 성공");
        System.out.println("   - 계좌번호: " + accountNumber);
        System.out.println("   - 은행: " + result.get().bankName());
    }

    @Test
    @DisplayName("3자리 prefix로 은행 조회 성공")
    void findBankByAccountNumber_3DigitPrefix_Success() {
        // given
        String accountNumber = "123-456-789012";

        // when
        Optional<BankInfo> result = bankService.findBankByAccountNumber(accountNumber);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().bankCode()).isEqualTo("004");
        assertThat(result.get().bankName()).isEqualTo("KB국민은행");

        System.out.println("✅ 3자리 prefix 조회 성공");
        System.out.println("   - 계좌번호: " + accountNumber);
        System.out.println("   - 은행: " + result.get().bankName());
    }

    @Test
    @DisplayName("4자리 실패 시 3자리 fallback 성공")
    void findBankByAccountNumber_FallbackTo3Digit_Success() {
        // given
        String accountNumber = "1401234567890"; // 1401로 시작하지만 4자리 매칭 실패, 140으로 3자리 매칭 성공

        // when
        Optional<BankInfo> result = bankService.findBankByAccountNumber(accountNumber);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().bankCode()).isEqualTo("088");
        assertThat(result.get().bankName()).isEqualTo("신한은행");

        System.out.println("✅ 4자리 실패 시 3자리 fallback 성공");
        System.out.println("   - 계좌번호: " + accountNumber);
        System.out.println("   - 4자리 '1401' 매칭 실패 → 3자리 '140' 매칭 성공");
    }

    @Test
    @DisplayName("계좌번호 너무 짧음 (< 3자리)")
    void findBankByAccountNumber_TooShort_ReturnsEmpty() {
        // given
        String accountNumber = "12";

        // when
        Optional<BankInfo> result = bankService.findBankByAccountNumber(accountNumber);

        // then
        assertThat(result).isEmpty();

        System.out.println("✅ 계좌번호 너무 짧음 테스트 통과");
        System.out.println("   - 계좌번호: " + accountNumber + " (길이 < 3)");
    }

    @Test
    @DisplayName("매칭되는 prefix 없음")
    void findBankByAccountNumber_NoMatch_ReturnsEmpty() {
        // given
        String accountNumber = "999-8888-7777"; // 존재하지 않는 prefix

        // when
        Optional<BankInfo> result = bankService.findBankByAccountNumber(accountNumber);

        // then
        assertThat(result).isEmpty();

        System.out.println("✅ 매칭되는 prefix 없음 테스트 통과");
        System.out.println("   - 계좌번호: " + accountNumber);
    }

    @Test
    @DisplayName("전체 은행 목록 조회")
    void getAllBanks_Success() {
        // when
        List<BankInfo> result = bankService.getAllBanks();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(BankInfo::bankName)
                .containsExactly("KB국민은행", "신한은행"); // 알파벳 순 정렬

        System.out.println("✅ 전체 은행 목록 조회 성공");
        System.out.println("   - 은행 수: " + result.size());
        result.forEach(bank -> System.out.println("   - " + bank.bankName() + " (" + bank.bankCode() + ")"));
    }

    // === Helper Methods (Reflection) ===

    private Bank createBank(String bankCode, String bankName) throws Exception {
        Constructor<Bank> constructor = Bank.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Bank bank = constructor.newInstance();

        setField(bank, "bankCode", bankCode);
        setField(bank, "bankName", bankName);

        return bank;
    }

    private BankPrefix createBankPrefix(Bank bank, String prefix) throws Exception {
        Constructor<BankPrefix> constructor = BankPrefix.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        BankPrefix bankPrefix = constructor.newInstance();

        setField(bankPrefix, "bank", bank);
        setField(bankPrefix, "prefix", prefix);

        return bankPrefix;
    }

    private void setField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
