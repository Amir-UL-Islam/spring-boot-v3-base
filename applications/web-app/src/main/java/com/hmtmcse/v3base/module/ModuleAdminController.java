package com.hmtmcse.v3base.module;

import com.hmtmcse.module.core.Module;
import com.hmtmcse.module.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API for module management (Odoo-style)
 * Allows admin users to view, enable, disable modules
 */
@RestController
@RequestMapping("/api/v1/modules")
@Tag(name = "Module Management", description = "Manage system modules (Odoo-style)")
@RequiredArgsConstructor
public class ModuleAdminController {

    private final ModuleService moduleService;

    @GetMapping
    @Operation(summary = "List all modules", description = "Get all installed modules with their status")
    public ResponseEntity<List<ModuleDTO>> listModules() {
        List<Module> modules = moduleService.getAllModules();
        List<ModuleDTO> dtos = modules.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{moduleId}")
    @Operation(summary = "Get module details", description = "Get detailed information about a specific module")
    public ResponseEntity<ModuleDTO> getModule(@PathVariable String moduleId) {
        Module module = moduleService.getModule(moduleId);
        if (module == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDTO(module));
    }

    @GetMapping("/active")
    @Operation(summary = "List active modules", description = "Get all currently active (started) modules")
    public ResponseEntity<List<ModuleDTO>> listActiveModules() {
        List<Module> modules = moduleService.getStartedModules();
        List<ModuleDTO> dtos = modules.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{moduleId}/enable")
    @Operation(
            summary = "Enable module",
            description = "Enable and start a module (requires MODULE_ADMIN privilege)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<MessageResponse> enableModule(@PathVariable String moduleId) {
        try {
            moduleService.enableModule(moduleId);
            return ResponseEntity.ok(new MessageResponse("Module enabled successfully. Restart may be required for full activation.", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to enable module: " + e.getMessage(), false));
        }
    }

    @PostMapping("/{moduleId}/disable")
    @Operation(
            summary = "Disable module",
            description = "Disable and stop a module (requires MODULE_ADMIN privilege)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<MessageResponse> disableModule(@PathVariable String moduleId) {
        try {
            moduleService.disableModule(moduleId);
            return ResponseEntity.ok(new MessageResponse("Module disabled successfully. Restart may be required for full deactivation.", true));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to disable module: " + e.getMessage(), false));
        }
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh module list",
            description = "Re-discover modules from classpath (requires MODULE_ADMIN privilege)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<MessageResponse> refreshModules() {
        try {
            moduleService.discoverAndRegisterModules();
            return ResponseEntity.ok(new MessageResponse("Modules refreshed successfully", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to refresh modules: " + e.getMessage(), false));
        }
    }

    private ModuleDTO toDTO(Module module) {
        ModuleDTO dto = new ModuleDTO();
        dto.setModuleId(module.getModuleId());
        dto.setName(module.getName());
        dto.setVersion(module.getVersion());
        dto.setDescription(module.getDescription());
        dto.setState(module.getState().name());
        dto.setEnabled(module.isEnabled());
        dto.setRequired(module.isRequired());
        dto.setCanBeDisabled(module.canBeDisabled());
        dto.setInstalledDate(module.getInstalledDate());
        dto.setLastStartedDate(module.getLastStartedDate());

        if (module.getMetadata() != null) {
            dto.setType(module.getMetadata().getType().name());
            dto.setPackageName(module.getMetadata().getPackageName());
            dto.setDependencies(module.getMetadata().getDependencies());
            dto.setPrivileges(module.getMetadata().getPrivileges());
        }

        return dto;
    }
}

