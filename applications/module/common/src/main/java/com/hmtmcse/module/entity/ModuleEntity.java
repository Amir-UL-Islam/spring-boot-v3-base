package com.hmtmcse.module.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * JPA Entity for persisting module state in database
 * Tracks which modules are installed and their current state
 */
@Entity
@Table(name = "system_modules")
@Data
public class ModuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String moduleId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String version;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ModuleState state = ModuleState.INSTALLED;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean required = false;

    @Column
    private LocalDateTime installedDate;

    @Column
    private LocalDateTime lastStartedDate;

    @Column
    private LocalDateTime lastStoppedDate;

    @Column(length = 100)
    private String installedBy;

    @Column(length = 50)
    private String moduleType;

    @Column(length = 500)
    private String packageName;

    @Column(length = 1000)
    private String dependencies;

    public enum ModuleState {
        INSTALLED,
        STARTED,
        STOPPED,
        DISABLED,
        FAILED
    }

    @PrePersist
    protected void onCreate() {
        if (installedDate == null) {
            installedDate = LocalDateTime.now();
        }
    }
}

