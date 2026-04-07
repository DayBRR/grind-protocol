package com.davidrr.grindprotocol.task.repository;

import com.davidrr.grindprotocol.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserIdAndActiveTrue(Long userId);

    Optional<Task> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}