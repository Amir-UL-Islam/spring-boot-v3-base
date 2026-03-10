package com.hmtmcse.module.core;

import lombok.Data;
import java.util.Set;
import java.util.HashSet;

/**
 * Module metadata class representing a module's configuration
 * Similar to OpenMRS Module concept
 */
@Data
public class ModuleMetadata {
    private String moduleId;
    private String name;
    private String version;
    private String author;
    private String description;
    private ModuleType type;
    private boolean required;
    private Set<String> dependencies = new HashSet<>();
    private String packageName;
    private Set<String> privileges = new HashSet<>();
    private String autoConfigurationClass;

    public enum ModuleType {
        LIBRARY,    // Core libraries (common, request-response)
        FEATURE,    // Feature modules (security, export-import)
        EXTENSION   // Extension modules (custom business modules)
    }

    public boolean isLibrary() {
        return ModuleType.LIBRARY.equals(type);
    }

    public boolean isFeature() {
        return ModuleType.FEATURE.equals(type);
    }

    public boolean isExtension() {
        return ModuleType.EXTENSION.equals(type);
    }
}

