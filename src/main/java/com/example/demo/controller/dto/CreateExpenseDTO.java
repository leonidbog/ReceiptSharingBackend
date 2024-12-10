package com.example.demo.controller.dto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateExpenseDTO {
    private String description;
    private Double amount;
    private Long groupId;
    private Set<Long> participantIds = new HashSet<>();

    // Геттеры и сеттеры

    public String getDescription() {
        return description;
    }

    public Double getAmount() {
        return amount;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Set<Long> getParticipantIds() {
        return participantIds;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void setParticipantIds(Set<Long> participantIds) {
        this.participantIds = participantIds;
    }

    public void setParticipantsIds(List<Long> currentUserId) {
        participantIds.addAll(currentUserId);
    }
}