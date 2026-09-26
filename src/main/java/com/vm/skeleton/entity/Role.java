package com.vm.skeleton.entity;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Role reference data (seeded by Flyway). Identified by its unique {@code code}, which is also the authority name.
 */
@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    public Role(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return this == o || (o instanceof Role other && Objects.equals(code, other.code));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
