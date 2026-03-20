package com.hmtmcse.module.service;

import com.hmtmcse.module.core.Module;
import com.hmtmcse.module.core.ModuleFactory;
import com.hmtmcse.module.core.ModuleMetadata;
import com.hmtmcse.module.entity.ModuleEntity;
import com.hmtmcse.module.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing module lifecycle
 * Similar to OpenMRS ModuleService
 */
@Service
public class ModuleService {

    @Autowired(required = false)
    private ModuleRepository moduleRepository;

    private final Map<String, Module> loadedModules = new LinkedHashMap<>();

    /**
     * Discover and register all modules from classpath
     */
    @Transactional
    public void discoverAndRegisterModules() {
        List<ModuleMetadata> discoveredModules = ModuleFactory.discoverModules();

        System.out.println("=== Module Discovery ===");
        System.out.println("Discovered " + discoveredModules.size() + " modules");

        // Sort by dependencies
        List<ModuleMetadata> sortedModules = ModuleFactory.sortByDependencies(discoveredModules);

        for (ModuleMetadata metadata : sortedModules) {
            registerModule(metadata);
        }

        System.out.println("=== Module Registration Complete ===");
    }

    /**
     * Register a single module
     */
    @Transactional
    public void registerModule(ModuleMetadata metadata) {
        System.out.println("Registering module: " + metadata.getModuleId() + " v" + metadata.getVersion());

        Module module = new Module();
        module.setModuleId(metadata.getModuleId());
        module.setName(metadata.getName());
        module.setVersion(metadata.getVersion());
        module.setDescription(metadata.getDescription());
        module.setRequired(metadata.isRequired());
        module.setMetadata(metadata);

        // Check if module exists in database
        if (moduleRepository != null) {
            Optional<ModuleEntity> existing = moduleRepository.findByModuleId(metadata.getModuleId());

            if (existing.isPresent()) {
                ModuleEntity entity = existing.get();
                module.setId(entity.getId());
                module.setEnabled(entity.getEnabled());
                module.setState(convertState(entity.getState()));
                module.setInstalledDate(entity.getInstalledDate());

                // Update version if changed
                if (!entity.getVersion().equals(metadata.getVersion())) {
                    entity.setVersion(metadata.getVersion());
                    entity.setDescription(metadata.getDescription());
                    moduleRepository.save(entity);
                    System.out.println("  Updated module version to " + metadata.getVersion());
                }
            } else {
                // New module - create entity
                ModuleEntity entity = new ModuleEntity();
                entity.setModuleId(metadata.getModuleId());
                entity.setName(metadata.getName());
                entity.setVersion(metadata.getVersion());
                entity.setDescription(metadata.getDescription());
                entity.setRequired(metadata.isRequired());
                entity.setEnabled(true);
                entity.setState(ModuleEntity.ModuleState.INSTALLED);
                entity.setModuleType(metadata.getType().name());
                entity.setPackageName(metadata.getPackageName());
                entity.setDependencies(String.join(",", metadata.getDependencies()));

                entity = moduleRepository.save(entity);
                module.setId(entity.getId());
                module.setEnabled(true);
                module.setState(Module.ModuleState.INSTALLED);
                module.setInstalledDate(entity.getInstalledDate());

                System.out.println("  Registered new module");
            }
        } else {
            // No repository available (probably during testing)
            module.setEnabled(true);
            module.setState(Module.ModuleState.INSTALLED);
            module.setInstalledDate(LocalDateTime.now());
        }

        loadedModules.put(metadata.getModuleId(), module);

        // Auto-start if enabled
        if (module.isEnabled()) {
            startModule(module);
        }

    }

    /**
     * Start a module
     */
    @Transactional
    public void startModule(Module module) {
        if (module.getState() == Module.ModuleState.STARTED) {
            return;
        }

        System.out.println("Starting module: " + module.getModuleId());

        // Validate dependencies are started
        for (String depId : module.getMetadata().getDependencies()) {
            Module dep = loadedModules.get(depId);
            if (dep == null || !dep.isStarted()) {
                System.err.println("  Failed: Dependency " + depId + " is not started");
                module.setState(Module.ModuleState.FAILED);
                return;
            }
        }

        module.setState(Module.ModuleState.STARTED);
        module.setLastStartedDate(LocalDateTime.now());

        if (moduleRepository != null && module.getId() != null) {
            ModuleEntity entity = moduleRepository.findById(module.getId()).orElse(null);
            if (entity != null) {
                entity.setState(ModuleEntity.ModuleState.STARTED);
                entity.setLastStartedDate(LocalDateTime.now());
                moduleRepository.save(entity);
            }
        }

        System.out.println("  Module started successfully");
    }

    /**
     * Stop a module
     */
    @Transactional
    public void stopModule(String moduleId) {
        Module module = loadedModules.get(moduleId);
        if (module == null) {
            throw new IllegalArgumentException("Module not found: " + moduleId);
        }

        if (!module.canBeDisabled()) {
            throw new IllegalStateException("Cannot stop required module: " + moduleId);
        }

        System.out.println("Stopping module: " + moduleId);

        module.setState(Module.ModuleState.STOPPED);
        module.setLastStoppedDate(LocalDateTime.now());

        if (moduleRepository != null && module.getId() != null) {
            ModuleEntity entity = moduleRepository.findById(module.getId()).orElse(null);
            if (entity != null) {
                entity.setState(ModuleEntity.ModuleState.STOPPED);
                entity.setLastStoppedDate(LocalDateTime.now());
                moduleRepository.save(entity);
            }
        }
    }

    /**
     * Enable a module
     */
    @Transactional
    public void enableModule(String moduleId) {
        Module module = loadedModules.get(moduleId);
        if (module == null) {
            throw new IllegalArgumentException("Module not found: " + moduleId);
        }

        module.setEnabled(true);

        if (moduleRepository != null && module.getId() != null) {
            ModuleEntity entity = moduleRepository.findById(module.getId()).orElse(null);
            if (entity != null) {
                entity.setEnabled(true);
                moduleRepository.save(entity);
            }
        }

        startModule(module);
    }

    /**
     * Disable a module
     */
    @Transactional
    public void disableModule(String moduleId) {
        Module module = loadedModules.get(moduleId);
        if (module == null) {
            throw new IllegalArgumentException("Module not found: " + moduleId);
        }

        if (!module.canBeDisabled()) {
            throw new IllegalStateException("Cannot disable required module: " + moduleId);
        }

        module.setEnabled(false);
        stopModule(moduleId);

        if (moduleRepository != null && module.getId() != null) {
            ModuleEntity entity = moduleRepository.findById(module.getId()).orElse(null);
            if (entity != null) {
                entity.setEnabled(false);
                moduleRepository.save(entity);
            }
        }
    }

    /**
     * Get all loaded modules
     */
    public List<Module> getAllModules() {
        return new ArrayList<>(loadedModules.values());
    }

    /**
     * Get a specific module
     */
    public Module getModule(String moduleId) {
        return loadedModules.get(moduleId);
    }

    /**
     * Get started modules
     */
    public List<Module> getStartedModules() {
        return loadedModules.values().stream()
                .filter(Module::isStarted)
                .collect(Collectors.toList());
    }

    private Module.ModuleState convertState(ModuleEntity.ModuleState entityState) {
        return Module.ModuleState.valueOf(entityState.name());
    }
}

