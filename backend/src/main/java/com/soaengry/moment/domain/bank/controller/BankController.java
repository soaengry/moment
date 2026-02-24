package com.soaengry.moment.domain.bank.controller;

import com.soaengry.moment.domain.bank.service.BankService;
import com.soaengry.moment.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @GetMapping("/detect")
    public ResponseEntity<ApiResponse<Map<String, String>>> detectBank(@RequestParam String accountNumber) {
        return bankService.findBankByAccountNumber(accountNumber)
                .map(info -> ResponseEntity.ok(ApiResponse.success(Map.of(
                        "bankCode", info.bankCode(),
                        "bankName", info.bankName()
                ))))
                .orElse(ResponseEntity.ok(ApiResponse.success(Map.of(
                        "bankCode", "",
                        "bankName", ""
                ))));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BankService.BankInfo>>> getAllBanks() {
        return ResponseEntity.ok(ApiResponse.success(bankService.getAllBanks()));
    }
}
