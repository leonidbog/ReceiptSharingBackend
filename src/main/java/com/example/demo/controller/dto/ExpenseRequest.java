package com.example.demo.controller.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
public class ExpenseRequest {

    // Геттеры и сеттеры
    private BigDecimal totalAmount;
    private Map<Long, BigDecimal> userAmounts;

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setUserAmounts(Map<Long, BigDecimal> userAmounts) {
        this.userAmounts = userAmounts;
    }
}
