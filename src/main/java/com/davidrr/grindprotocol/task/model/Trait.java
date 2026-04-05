package com.davidrr.grindprotocol.task.model;

import com.davidrr.grindprotocol.common.model.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "traits",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_traits_code", columnNames = "code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trait extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código técnico único del trait.
     * Ejemplos: DISCIPLINE, STRENGTH, INTELLIGENCE...
     */
    @Column(nullable = false, length = 50)
    private String code;

    /**
     * Nombre legible para mostrar en UI.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Descripción opcional del significado del trait.
     */
    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean active;
}