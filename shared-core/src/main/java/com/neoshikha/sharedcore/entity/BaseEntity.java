package com.neoshikha.sharedcore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -4939242513804832311L;

    @Builder.Default
    @Version
    private Long version = 0L;

    @Builder.Default
    private boolean deleted = Boolean.FALSE;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(updatable = false)
    private UUID createdBy;

    private UUID updatedBy;

    private String ipAddress;

}