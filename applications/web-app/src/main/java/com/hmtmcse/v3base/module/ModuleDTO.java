package com.hmtmcse.v3base.module;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ModuleDTO {
    private String moduleId;
    private String name;
    private String version;
    private String description;
    private String state;
    private boolean enabled;
    private boolean required;
    private boolean canBeDisabled;
    private LocalDateTime installedDate;
    private LocalDateTime lastStartedDate;
    private String type;
    private String packageName;
    private Set<String> dependencies;
    private Set<String> privileges;
}

