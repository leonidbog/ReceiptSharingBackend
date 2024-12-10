package com.example.demo.service;

import com.example.demo.controller.dto.CreateExpenseDTO;
import com.example.demo.dto.ExpenseDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.Expense;
import com.example.demo.entity.ExpenseShare;
import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.repository.ExpenseRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, GroupRepository groupRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    // Создание расхода
    public String createExpense(User currentUser, CreateExpenseDTO createExpenseDTO) {
        String description = createExpenseDTO.getDescription();
        BigDecimal amount = BigDecimal.valueOf(createExpenseDTO.getAmount());
        Long groupId = createExpenseDTO.getGroupId();
        Set<Long> participantIds = createExpenseDTO.getParticipantIds();

        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            throw new IllegalArgumentException("Группа не найдена");
        }

        Group group = groupOpt.get();

        if (!group.getMembers().contains(currentUser)) {
            throw new SecurityException("You are not in the group");
        }

        // Проверяем что сумма положительна
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Set<User> participants = new HashSet<>();
        for (Long userId : participantIds) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent() && group.getMembers().contains(userOpt.get())) {
                participants.add(userOpt.get());
            } else {
                throw new IllegalArgumentException("Участник с ID " + userId + " не найден в группе");
            }
        }

        Expense expense = new Expense(description, amount.doubleValue(), currentUser, group, participants);

        BigDecimal numberOfParticipants = new BigDecimal(participants.size());
        BigDecimal shareAmount = amount.divide(numberOfParticipants, MathContext.DECIMAL128);
        Set<ExpenseShare> shares = new HashSet<>();

        for (User participant : participants) {
            ExpenseShare share = new ExpenseShare(shareAmount, expense, participant);
            shares.add(share);
        }

        expense.setShares(shares);

        expenseRepository.save(expense);

        return "Expense created successfully";
    }

    private List<Expense> getExpensesByGroup(User currentUser, Long groupId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            throw new IllegalArgumentException("Группа не найдена");
        }

        Group group = groupOpt.get();

        if (!group.getMembers().contains(currentUser)) {
            throw new SecurityException("not a member of this group");
        }

        // Предполагается, что у вас есть метод в репозитории, например:
        // List<Expense> findByGroup(Group group);
        return expenseRepository.findByGroup(group);
    }

    // Получение расходов по группе
    public List<ExpenseDTO> getExpensesDTOByGroup(User currentUser, Long groupId) {
        List<Expense> expenses = getExpensesByGroup(currentUser, groupId);

        return expenses.stream().map(expense -> {
            UserDTO payerDTO = new UserDTO(expense.getPayer().getId(), expense.getPayer().getUsername());
            Set<UserDTO> participantDTOs = expense.getParticipants().stream()
                    .map(participant -> new UserDTO(participant.getId(), participant.getUsername()))
                    .collect(Collectors.toSet());

            return new ExpenseDTO(
                    expense.getId(),
                    expense.getDescription(),
                    expense.getAmount(),
                    payerDTO,
                    participantDTOs
            );
        }).collect(Collectors.toList());
    }
}