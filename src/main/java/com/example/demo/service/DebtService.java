package com.example.demo.service;

import com.example.demo.entity.Expense;
import com.example.demo.entity.ExpenseShare;
import com.example.demo.entity.User;
import com.example.demo.repository.ExpenseRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DebtService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public DebtService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public Map<Long, BigDecimal> calculateDebtsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Expense> expenses = expenseRepository.findAll();

        Map<Long, BigDecimal> debts = new HashMap<>();

        for (Expense expense : expenses) {
            User payer = expense.getPayer();
            for (ExpenseShare share : expense.getShares()) {
                if (share.getUser().getId().equals(userId)) {
                    if (!payer.getId().equals(userId)) {
                        debts.merge(payer.getId(), share.getAmount(), BigDecimal::add);
                    }
                } else if (payer.getId().equals(userId)) {
                    debts.merge(share.getUser().getId(), share.getAmount().negate(), BigDecimal::add);
                }
            }
        }

        // Удаляем нулевые долги
        debts.entrySet().removeIf(entry -> entry.getValue().compareTo(BigDecimal.ZERO) == 0);

        return debts;
    }
}
