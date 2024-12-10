package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "expenses")
@Data
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private Double amount;

    // Плательщик
    @ManyToOne
    @JoinColumn(name = "payer_id")
    @JsonManagedReference
    private User payer;

    // Группа
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    // Участники расхода
    @ManyToMany
    @JoinTable(
            name = "expense_participants",
            joinColumns = @JoinColumn(name = "expense_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonManagedReference
    private Set<User> participants = new HashSet<>();

    // **Новое поле для долей расходов**
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<ExpenseShare> shares = new HashSet<>();

    // Конструкторы

    public Expense() {
    }

    public Expense(String description, Double amount, User payer, Group group, Set<User> participants) {
        this.description = description;
        this.amount = amount;
        this.payer = payer;
        this.group = group;
        this.participants = participants;
    }


}