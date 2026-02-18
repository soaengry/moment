package com.soaengry.moment.domain.bank.service;

import com.soaengry.moment.domain.bank.entity.Bank;
import com.soaengry.moment.domain.bank.entity.BankPrefix;
import com.soaengry.moment.domain.bank.repository.BankPrefixRepository;
import com.soaengry.moment.domain.bank.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final BankRepository bankRepository;
    private final BankPrefixRepository bankPrefixRepository;

    public record BankInfo(String bankCode, String bankName) {}

    /**
     * 계좌번호에서 prefix를 추출하여 은행 정보를 조회합니다.
     * 4자리 → 3자리 순으로 매칭을 시도합니다.
     */
    public Optional<BankInfo> findBankByAccountNumber(String accountNumber) {
        String cleaned = accountNumber.replaceAll("[^0-9]", "");

        if (cleaned.length() < 3) {
            return Optional.empty();
        }

        // 4자리 prefix 먼저 시도
        if (cleaned.length() >= 4) {
            Optional<BankPrefix> result = bankPrefixRepository.findByPrefixWithBank(cleaned.substring(0, 4));
            if (result.isPresent()) {
                Bank bank = result.get().getBank();
                return Optional.of(new BankInfo(bank.getBankCode(), bank.getBankName()));
            }
        }

        // 3자리 prefix 시도
        Optional<BankPrefix> result = bankPrefixRepository.findByPrefixWithBank(cleaned.substring(0, 3));
        if (result.isPresent()) {
            Bank bank = result.get().getBank();
            return Optional.of(new BankInfo(bank.getBankCode(), bank.getBankName()));
        }

        return Optional.empty();
    }

    public List<BankInfo> getAllBanks() {
        return bankRepository.findAllByOrderByBankNameAsc()
                .stream()
                .map(bank -> new BankInfo(bank.getBankCode(), bank.getBankName()))
                .toList();
    }
}
