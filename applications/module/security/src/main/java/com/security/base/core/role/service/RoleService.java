package com.security.base.core.role.service;

import com.security.base.core.role.repository.RoleRepository;
import com.security.base.core.role.model.dto.RoleDTO;
import com.security.base.core.role.model.entity.Role;
import com.security.base.events.BeforeDeletePrivilege;
import com.security.base.events.BeforeDeleteRole;
import com.security.base.util.CustomCollectors;
import com.security.base.util.NotFoundException;
import com.problemfighter.pfspring.restapi.rr.RequestResponse;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class RoleService implements RequestResponse {

    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher publisher;


    public Page<RoleDTO> findAll(final String filter, final Pageable pageable) {
        Page<Role> page;
        if (filter != null) {
            Long longFilter = null;
            try {
                longFilter = Long.parseLong(filter);
            } catch (final NumberFormatException numberFormatException) {
                // keep null - no parseable input
            }
            page = roleRepository.findAllById(longFilter, pageable);
        } else {
            page = roleRepository.findAll(pageable);
        }
        return new PageImpl<>(
                responseProcessor().entityToDTO(page.getContent(), RoleDTO.class),
                pageable,
                page.getTotalElements()
        );
    }

    public RoleDTO get(final Long id) {
        return roleRepository.findById(id)
                .map(role -> responseProcessor().entityToDTO(role, RoleDTO.class))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final RoleDTO roleDTO) {
        final Role role = new Role();
        requestProcessor().process(roleDTO, role);
        return roleRepository.save(role).getId();
    }

    public void update(final Long id, final RoleDTO roleDTO) {
        final Role role = roleRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        requestProcessor().process(roleDTO, role);
        roleRepository.save(role);
    }

    public void delete(final Long id) {
        final Role role = roleRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteRole(id));
        roleRepository.delete(role);
    }

    public boolean nameExists(final String name) {
        return roleRepository.existsByNameIgnoreCase(name);
    }

    public boolean descriptionExists(final String description) {
        return roleRepository.existsByDescriptionIgnoreCase(description);
    }

    public Map<Long, String> getRoleValues() {
        return roleRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Role::getId, Role::getName));
    }

    @EventListener(BeforeDeletePrivilege.class)
    public void on(final BeforeDeletePrivilege event) {
        // remove many-to-many relations at owning side
        roleRepository.findAllByPrivilegeId(event.getId()).forEach(role ->
                role.getPrivilege().removeIf(privilege -> privilege.getId().equals(event.getId())));
    }

}
