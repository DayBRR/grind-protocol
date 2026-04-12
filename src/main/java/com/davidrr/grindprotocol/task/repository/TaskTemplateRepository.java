package com.davidrr.grindprotocol.task.repository;

import com.davidrr.grindprotocol.task.model.TaskTemplate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {

    List<TaskTemplate> findByActiveTrue();

    List<TaskTemplate> findByIsPublicTrueAndActiveTrue();

    List<TaskTemplate> findByCreatorUserIdAndActiveTrue(Long creatorUserId);

    Optional<TaskTemplate> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = {"traits"})
    Optional<TaskTemplate> findWithTraitsByIdAndActiveTrue(Long id);
}