package com.hmtmcse.module.core;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Factory for discovering and loading module metadata
 * Similar to OpenMRS ModuleFactory
 */
public class ModuleFactory {

    private static final String MODULE_PROPERTIES_PATTERN = "classpath*:module.properties";
    private static final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    /**
     * Discover all modules by scanning for module.properties files
     */
    public static List<ModuleMetadata> discoverModules() {
        List<ModuleMetadata> modules = new ArrayList<>();

        try {
            Resource[] resources = resolver.getResources(MODULE_PROPERTIES_PATTERN);

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    Properties props = new Properties();
                    props.load(is);

                    ModuleMetadata metadata = parseModuleProperties(props);
                    modules.add(metadata);
                } catch (Exception e) {
                    System.err.println("Failed to load module from " + resource.getDescription() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to discover modules: " + e.getMessage());
        }

        return modules;
    }

    /**
     * Parse module properties into ModuleMetadata
     */
    private static ModuleMetadata parseModuleProperties(Properties props) {
        ModuleMetadata metadata = new ModuleMetadata();

        metadata.setModuleId(props.getProperty("module.id"));
        metadata.setName(props.getProperty("module.name"));
        metadata.setVersion(props.getProperty("module.version"));
        metadata.setAuthor(props.getProperty("module.author"));
        metadata.setDescription(props.getProperty("module.description"));

        String typeStr = props.getProperty("module.type", "FEATURE");
        metadata.setType(ModuleMetadata.ModuleType.valueOf(typeStr.toUpperCase()));

        metadata.setRequired(Boolean.parseBoolean(props.getProperty("module.required", "false")));

        String dependenciesStr = props.getProperty("module.dependencies", "");
        if (!dependenciesStr.trim().isEmpty()) {
            Set<String> deps = Arrays.stream(dependenciesStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
            metadata.setDependencies(deps);
        }

        metadata.setPackageName(props.getProperty("module.package"));

        String privilegesStr = props.getProperty("module.privileges", "");
        if (!privilegesStr.trim().isEmpty()) {
            Set<String> privs = Arrays.stream(privilegesStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
            metadata.setPrivileges(privs);
        }

        metadata.setAutoConfigurationClass(props.getProperty("module.auto-configuration"));

        return metadata;
    }

    /**
     * Validate module dependencies
     */
    public static boolean validateDependencies(ModuleMetadata module, Map<String, ModuleMetadata> availableModules) {
        for (String dependency : module.getDependencies()) {
            if (!availableModules.containsKey(dependency)) {
                System.err.println("Module " + module.getModuleId() + " requires missing dependency: " + dependency);
                return false;
            }
        }
        return true;
    }

    /**
     * Sort modules by dependency order (topological sort)
     */
    public static List<ModuleMetadata> sortByDependencies(List<ModuleMetadata> modules) {
        Map<String, ModuleMetadata> moduleMap = modules.stream()
                .collect(Collectors.toMap(ModuleMetadata::getModuleId, m -> m));

        List<ModuleMetadata> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (ModuleMetadata module : modules) {
            if (!visited.contains(module.getModuleId())) {
                visitModule(module, moduleMap, visited, visiting, sorted);
            }
        }

        return sorted;
    }

    private static void visitModule(ModuleMetadata module, Map<String, ModuleMetadata> moduleMap,
                                   Set<String> visited, Set<String> visiting, List<ModuleMetadata> sorted) {
        if (visiting.contains(module.getModuleId())) {
            throw new IllegalStateException("Circular dependency detected: " + module.getModuleId());
        }

        if (visited.contains(module.getModuleId())) {
            return;
        }

        visiting.add(module.getModuleId());

        for (String dependency : module.getDependencies()) {
            ModuleMetadata depModule = moduleMap.get(dependency);
            if (depModule != null) {
                visitModule(depModule, moduleMap, visited, visiting, sorted);
            }
        }

        visiting.remove(module.getModuleId());
        visited.add(module.getModuleId());
        sorted.add(module);
    }
}

