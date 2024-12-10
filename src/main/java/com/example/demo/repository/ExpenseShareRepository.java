package com.example.demo.repository;

import com.example.demo.entity.ExpenseShare;
import com.example.demo.entity.ExpenseShareId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, ExpenseShareId> {
}
