package com.davidrr.grindprotocol.task.repository;

import com.davidrr.grindprotocol.task.model.TaskTemplate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {

    @EntityGraph(attributePaths = {"traits"})
    List<TaskTemplate> findByCreatorUserIdAndActiveTrue(Long creatorUserId);

    @EntityGraph(attributePaths = {"traits"})
    List<TaskTemplate> findByPublicTemplateTrueAndActiveTrue();

    @EntityGraph(attributePaths = {"traits"})
    Optional<TaskTemplate> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = {"traits"})
    Optional<TaskTemplate> findByIdAndCreatorUserIdAndActiveTrue(Long id, Long creatorUserId);

    @EntityGraph(attributePaths = {"traits"})
    Optional<TaskTemplate> findByIdAndPublicTemplateTrueAndActiveTrue(Long id);
}