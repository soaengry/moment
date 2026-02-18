package com.soaengry.moment.domain.bank.repository;

import com.soaengry.moment.domain.bank.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankRepository extends JpaRepository<Bank, Long> {
    List<Bank> findAllByOrderByBankNameAsc();
}
