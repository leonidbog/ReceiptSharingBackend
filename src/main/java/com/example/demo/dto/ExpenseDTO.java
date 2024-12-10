package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class ExpenseDTO {
    private Long id;
    private String description;
    private Double amount;
    private UserDTO payer;
    private Set<UserDTO> participants;
}
