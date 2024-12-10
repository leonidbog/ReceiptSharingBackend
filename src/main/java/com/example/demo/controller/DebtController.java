package com.example.demo.controller;

import com.example.demo.service.DebtService;
import com.example.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/debts")
public class DebtController {

    private final DebtService debtService;
    private final UserService userService;

    public DebtController(DebtService debtService, UserService userService) {
        this.debtService = debtService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyDebts(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());

        Map<Long, BigDecimal> debts = debtService.calculateDebtsForUser(userId);

        return ResponseEntity.ok(debts);
    }
}
