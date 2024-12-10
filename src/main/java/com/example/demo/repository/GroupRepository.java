package com.example.demo.repository;

import com.example.demo.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Set;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Set<Group> findAllByCreatorId(Long creatorId);
}