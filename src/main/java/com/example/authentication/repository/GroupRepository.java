package com.example.authentication.repository;

import com.example.authentication.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository
        extends JpaRepository<Group, Long> {

    Optional<Group> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}