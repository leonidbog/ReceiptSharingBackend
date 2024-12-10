package com.example.demo.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ExpenseShareId implements Serializable {

    @Column(name = "expense_id")
    private Long expenseId;

    @Column(name = "user_id")
    private Long userId;

    public ExpenseShareId() {
    }

    public ExpenseShareId(Long expenseId, Long userId) {
        this.expenseId = expenseId;
        this.userId = userId;
    }

    // Геттеры и сеттеры
    public Long getExpenseId() {
        return expenseId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setExpenseId(Long expenseId) {
        this.expenseId = expenseId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // equals и hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpenseShareId that = (ExpenseShareId) o;
        return Objects.equals(expenseId, that.expenseId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expenseId, userId);
    }
}
