package com.example.demo.service;
import com.example.demo.entity.Expense;
import com.example.demo.entity.ExpenseShare;
import com.example.demo.entity.User;
import com.example.demo.repository.ExpenseRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DebtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DebtServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    private DebtService debtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        debtService = new DebtService(expenseRepository, userRepository);
    }

    @Test
    void userNotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> debtService.calculateDebtsForUser(userId));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void noExpenses() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(expenseRepository.findAll()).thenReturn(Collections.emptyList());

        Map<Long, BigDecimal> debts = debtService.calculateDebtsForUser(userId);
        assertTrue(debts.isEmpty(), "Debts should be empty when no expenses present");
    }

    @Test
    void userNoDebts() {
        // Пользователь есть, есть расходы, но он не участвует или он платил сам за себя
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        User otherUser = new User();
        otherUser.setId(2L);

        Expense expense = new Expense();
        expense.setPayer(otherUser); // Плательщик другой

        // Но долей у этого пользователя нет
        expense.setShares(Collections.emptySet());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(expenseRepository.findAll()).thenReturn(List.of(expense));

        Map<Long, BigDecimal> debts = debtService.calculateDebtsForUser(userId);
        assertTrue(debts.isEmpty(), "No debts if user not involved in any shares");
    }

    @Test
    void userOwesToOthers() {
        Long userId = 1L;
        User user = new User(); // наш текущий пользователь
        user.setId(userId);

        User payer = new User();
        payer.setId(2L);

        Expense expense = new Expense();
        expense.setPayer(payer); // Плательщик payer

        ExpenseShare share = new ExpenseShare();
        share.setUser(user); // пользователь user должен payer'у
        share.setAmount(BigDecimal.valueOf(50));
        expense.setShares(Set.of(share));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(expenseRepository.findAll()).thenReturn(List.of(expense));

        Map<Long, BigDecimal> debts = debtService.calculateDebtsForUser(userId);
        // Ожидаем что userId должен payer'у 50
        assertEquals(1, debts.size());
        assertEquals(BigDecimal.valueOf(50), debts.get(payer.getId()));
    }

    @Test
    void othersOweToUser() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        User debtor = new User();
        debtor.setId(2L);

        Expense expense = new Expense();
        expense.setPayer(user); // наш пользователь платил

        ExpenseShare share = new ExpenseShare();
        share.setUser(debtor); // debtor должен нашему пользователю
        share.setAmount(BigDecimal.valueOf(100));
        expense.setShares(Set.of(share));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(expenseRepository.findAll()).thenReturn(List.of(expense));

        Map<Long, BigDecimal> debts = debtService.calculateDebtsForUser(userId);
        // Поскольку user платил за debtor, debtor должен user'у, значит в карте должен быть запись: debtorId -> -100
        assertEquals(1, debts.size());
        // Отрицательное число показывает, что debtor должен нашему пользователю
        assertEquals(BigDecimal.valueOf(-100), debts.get(debtor.getId()));
    }

    @Test
    void mixedDebts() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        User payerA = new User();
        payerA.setId(2L);

        User payerB = new User();
        payerB.setId(3L);

        // Первый расход: payerA платит за user и за payerB
        Expense expense1 = new Expense();
        expense1.setPayer(payerA);

        ExpenseShare share1 = new ExpenseShare();
        share1.setUser(user);
        share1.setAmount(BigDecimal.valueOf(50));

        ExpenseShare share2 = new ExpenseShare();
        share2.setUser(payerB);
        share2.setAmount(BigDecimal.valueOf(20));

        expense1.setShares(Set.of(share1, share2));

        // Второй расход: user платит за payerB
        Expense expense2 = new Expense();
        expense2.setPayer(user);

        ExpenseShare share3 = new ExpenseShare();
        share3.setUser(payerB);
        share3.setAmount(BigDecimal.valueOf(30));

        expense2.setShares(Set.of(share3));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(expenseRepository.findAll()).thenReturn(List.of(expense1, expense2));

        Map<Long, BigDecimal> debts = debtService.calculateDebtsForUser(userId);

        // Анализ:
        // expense1: user должен payerA 50 (запись в debts: 2L -> 50)
        //           payerB тоже должен payerA 20, но нас это не касается напрямую, это учитывается только если userId == payerB
        // expense2: user платит за payerB 30, значит payerB должен user'у 30 -> запись (3L -> -30)

        // В итоге:
        // user должен payerA: 50
        // payerB должен user: -30
        // Ждем две записи: {2 -> 50, 3 -> -30}

        assertEquals(2, debts.size());
        assertEquals(BigDecimal.valueOf(50), debts.get(payerA.getId()));
        assertEquals(BigDecimal.valueOf(-30), debts.get(payerB.getId()));
    }

    @Test
    void zeroDebtsAreRemoved() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        User payer = new User();
        payer.setId(2L);

        Expense expense = new Expense();
        expense.setPayer(payer);

        // Предположим что user должен payer'у 0, а payer должен user'у тоже 0
        // Для этого сделаем shares так, чтобы сумма нивелировалась:
        ExpenseShare share1 = new ExpenseShare();
        share1.setUser(user);
        share1.setAmount(BigDecimal.valueOf(50));

        ExpenseShare share2 = new ExpenseShare();
        share2.setUser(payer);
        share2.setAmount(BigDecimal.valueOf(50));

        // В итоге user owes payer 50, но так как payer - это платящий, это +50 для user,
        // но у нас есть обратная доля: payer как участник у user (который payer в другом expense, или в этом?)
        // Чтобы получить ноль: нужно чтобы user и payer поменялись ролями в двух расходах.

        // Упростим: пусть будет два расхода.

        Expense expense1 = new Expense();
        expense1.setPayer(payer);
        expense1.setShares(Set.of(share1)); // user должен payer'у 50

        Expense expense2 = new Expense();
        expense2.setPayer(user);
        expense2.setShares(Set.of(share2)); // payer должен user'у 50

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(expenseRepository.findAll()).thenReturn(List.of(expense1, expense2));

        Map<Long, BigDecimal> debts = debtService.calculateDebtsForUser(userId);
        // Долги должны взаимно компенсироваться. user должен payer'у 50, и payer должен user'у -50,
        // в сумме ноль, таких записей не должно остаться.

        assertTrue(debts.isEmpty(), "If debts cancel out to zero, should be removed");
    }
}