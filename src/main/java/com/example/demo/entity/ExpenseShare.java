package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "expense_shares")
public class ExpenseShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    // Связь с расходом
    @ManyToOne
    @JoinColumn(name = "expense_id")
    @JsonManagedReference
    private Expense expense;

    // Связь с пользователем
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonManagedReference
    private User user;

    // Конструкторы

    public ExpenseShare() {
    }

    public ExpenseShare(BigDecimal amount, Expense expense, User user) {
        this.amount = amount;
        this.expense = expense;
        this.user = user;
    }

    // Геттеры и сеттеры

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Expense getExpense() {
        return expense;
    }

    public User getUser() {
        return user;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public void setUser(User user) {
        this.user = user;
    }
}