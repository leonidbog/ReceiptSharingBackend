package com.example.demo.service;

import com.example.demo.controller.dto.CreateExpenseDTO;
import com.example.demo.dto.ExpenseDTO;
import com.example.demo.entity.Expense;
import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.repository.ExpenseRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;

    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        expenseService = new ExpenseService(expenseRepository, groupRepository, userRepository);
    }

    @Test
    void createExpenseSuccess() {
        Long currentUserId = 1L;
        User currentUser = new User();
        currentUser.setId(currentUserId);

        Long groupId = 10L;
        Group group = new Group();
        group.setId(groupId);
        // Представим, что текущий пользователь - участник этой группы
        group.setMembers(Set.of(currentUser));

        // DTO с данными о расходе
        CreateExpenseDTO dto = new CreateExpenseDTO();
        dto.setGroupId(groupId);
        dto.setDescription("Lunch");
        dto.setAmount(100.0);
        dto.setParticipantsIds(List.of(currentUserId));
        // Примерно так - адаптируйте под свой реальный DTO

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        // Допустим save при создании возвращает созданный Expense
        when(expenseRepository.save(any())).thenAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            saved.setId(999L);
            return saved;
        });

        String resultMessage = expenseService.createExpense(currentUser, dto);
        assertNotNull(resultMessage);
        assertTrue(resultMessage.contains("created"), "Message should indicate successful creation");

        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void createExpenseUserNotInGroup() {
        Long currentUserId = 1L;
        User currentUser = new User();
        currentUser.setId(currentUserId);

        Long groupId = 10L;
        Group group = new Group();
        group.setId(groupId);
        // текущего пользователя в группе нет
        group.setMembers(Collections.emptySet());

        CreateExpenseDTO dto = new CreateExpenseDTO();
        dto.setGroupId(groupId);
        dto.setDescription("Dinner");
        dto.setAmount(200.0);
        dto.setParticipantsIds(List.of(currentUserId));

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        SecurityException ex = assertThrows(SecurityException.class, () ->
                expenseService.createExpense(currentUser, dto));
        assertTrue(ex.getMessage().contains("not in the group"), "Should throw if user not in group");
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void createExpenseInvalidAmount() {
        Long currentUserId = 1L;
        User currentUser = new User();
        currentUser.setId(currentUserId);

        Group group = new Group();
        group.setId(10L);
        group.setMembers(Set.of(currentUser));

        CreateExpenseDTO dto = new CreateExpenseDTO();
        dto.setGroupId(10L);
        dto.setDescription("Snack");
        dto.setAmount(-50.0); // Невалидная сумма
        dto.setParticipantsIds(List.of(currentUserId));

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                expenseService.createExpense(currentUser, dto));
        assertTrue(ex.getMessage().contains("Amount must be positive"), "Amount must be positive");
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void getExpensesByGroupSuccess() {
        Long currentUserId = 1L;
        User currentUser = new User();
        currentUser.setId(currentUserId);

        Long groupId = 20L;
        Group group = new Group();
        group.setId(groupId);
        // currentUser - участник группы
        group.setMembers(Set.of(currentUser));

        User payer1 = new User();
        payer1.setId(2L);
        payer1.setUsername("Payer1");

        User payer2 = new User();
        payer2.setId(3L);
        payer2.setUsername("Payer2");

        Expense expense1 = new Expense();
        expense1.setId(100L);
        expense1.setDescription("Taxi");
        expense1.setAmount(150.0);
        expense1.setPayer(payer1); // Устанавливаем плательщика

        Expense expense2 = new Expense();
        expense2.setId(101L);
        expense2.setDescription("Coffee");
        expense2.setAmount(20.0);
        expense2.setPayer(payer2); // Устанавливаем плательщика

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(expenseRepository.findByGroup(group)).thenReturn(List.of(expense1, expense2));

        List<ExpenseDTO> result = expenseService.getExpensesDTOByGroup(currentUser, groupId);
        assertEquals(2, result.size());
        assertEquals("Taxi", result.get(0).getDescription());
        assertEquals("Coffee", result.get(1).getDescription());
        assertEquals(150.0, result.get(0).getAmount());
        assertEquals(20.0, result.get(1).getAmount());

        // Проверяем, что payer тоже корректно мапится
        assertEquals(payer1.getId(), result.get(0).getPayer().getId());
        assertEquals(payer2.getId(), result.get(1).getPayer().getId());
    }

    @Test
    void getExpensesByGroupUserNotMember() {
        Long currentUserId = 1L;
        User currentUser = new User();
        currentUser.setId(currentUserId);

        Long groupId = 30L;
        Group group = new Group();
        group.setId(groupId);
        // user не в группе
        group.setMembers(Collections.emptySet());

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        SecurityException ex = assertThrows(SecurityException.class, () ->
                expenseService.getExpensesDTOByGroup(currentUser, groupId));
        assertTrue(ex.getMessage().contains("not a member of this group"));
        verify(expenseRepository, never()).findByGroupId(anyLong());
    }

    @Test
    void getExpensesByGroupNoExpenses() {
        Long currentUserId = 1L;
        User currentUser = new User();
        currentUser.setId(currentUserId);

        Long groupId = 40L;
        Group group = new Group();
        group.setId(groupId);
        group.setMembers(Set.of(currentUser));

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(expenseRepository.findByGroupId(groupId)).thenReturn(Collections.emptyList());

        List<ExpenseDTO> result = expenseService.getExpensesDTOByGroup(currentUser, groupId);
        assertTrue(result.isEmpty(), "No expenses should return empty list");
    }
}