package com.davidrr.grindprotocol.task.repository;

import com.davidrr.grindprotocol.task.model.Trait;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TraitRepository extends JpaRepository<Trait, Long> {

    Optional<Trait> findByCode(String code);

    List<Trait> findByCodeIn(Collection<String> codes);

    List<Trait> findByActiveTrue();

    boolean existsByCode(String code);

    long countByCodeIn(Collection<String> codes);

    List<Trait> findByCodeInAndActiveTrue(Set<String> codes);
}