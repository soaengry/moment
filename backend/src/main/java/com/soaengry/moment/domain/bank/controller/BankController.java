package com.soaengry.moment.domain.bank.controller;

import com.soaengry.moment.domain.bank.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @GetMapping("/detect")
    public ResponseEntity<?> detectBank(@RequestParam String accountNumber) {
        return bankService.findBankByAccountNumber(accountNumber)
                .map(info -> ResponseEntity.ok(Map.of(
                        "bankCode", info.bankCode(),
                        "bankName", info.bankName()
                )))
                .orElse(ResponseEntity.ok(Map.of(
                        "bankCode", "",
                        "bankName", ""
                )));
    }
}
