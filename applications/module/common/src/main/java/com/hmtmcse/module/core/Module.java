package com.hmtmcse.module.core;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents the runtime state of a module in the system
 * Similar to OpenMRS's Module class
 */
@Data
public class Module {
    private Long id;
    private String moduleId;
    private String name;
    private String version;
    private String description;
    private ModuleState state;
    private boolean enabled;
    private boolean required;
    private LocalDateTime installedDate;
    private LocalDateTime lastStartedDate;
    private LocalDateTime lastStoppedDate;
    private String installedBy;
    private ModuleMetadata metadata;

    public enum ModuleState {
        INSTALLED,      // Module is installed but not started
        STARTED,        // Module is running
        STOPPED,        // Module was started but is now stopped
        DISABLED,       // Module is disabled by admin
        FAILED          // Module failed to start
    }

    public boolean isStarted() {
        return ModuleState.STARTED.equals(state) && enabled;
    }

    public boolean canBeDisabled() {
        return !required;
    }
}

