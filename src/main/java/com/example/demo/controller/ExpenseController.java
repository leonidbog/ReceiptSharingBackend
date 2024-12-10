package com.example.demo.controller;

import com.example.demo.controller.dto.CreateExpenseDTO;
import com.example.demo.dto.ExpenseDTO;
import com.example.demo.entity.Expense;
import com.example.demo.entity.User;
import com.example.demo.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // Создание расхода
    @PostMapping("/create")
    public ResponseEntity<?> createExpense(@AuthenticationPrincipal User currentUser,
                                           @RequestBody CreateExpenseDTO createExpenseDTO) {
        try {
            String message = expenseService.createExpense(currentUser, createExpenseDTO);
            return ResponseEntity.ok(message);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        }
    }

    // Получение расходов по группе
    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getExpensesByGroup(@AuthenticationPrincipal User currentUser,
                                                @PathVariable Long groupId) {
        try {
            List<ExpenseDTO> expenses = expenseService.getExpensesDTOByGroup(currentUser, groupId);
            return ResponseEntity.ok(expenses);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        }
    }
}