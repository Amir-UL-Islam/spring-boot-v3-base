package com.security.base.core.users.service;

import com.security.base.core.users.model.dto.UsersDTO;
import com.security.base.core.users.model.entity.Users;
import com.security.base.core.users.repository.UsersRepository;
import com.security.base.events.BeforeDeleteRole;
import com.security.base.util.NotFoundException;
import com.problemfighter.pfspring.restapi.rr.RequestResponse;

import java.util.Random;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UsersService implements RequestResponse {

    private final UsersRepository usersRepository;


    public Page<UsersDTO> findAll(final String filter, final Pageable pageable) {
        Page<Users> page;
        if (filter != null) {
            Long longFilter = null;
            try {
                longFilter = Long.parseLong(filter);
            } catch (final NumberFormatException numberFormatException) {
                // keep null - no parseable input
            }
            page = usersRepository.findAllById(longFilter, pageable);
        } else {
            page = usersRepository.findAll(pageable);
        }
        return new PageImpl<>(
                responseProcessor().entityToDTO(page.getContent(), UsersDTO.class),
                pageable,
                page.getTotalElements()
        );
    }

    public UsersDTO get(final Long id) {
        return usersRepository.findById(id)
                .map(users -> responseProcessor().entityToDTO(users, UsersDTO.class))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final UsersDTO usersDTO) {
        final Users users = new Users();
        requestProcessor().process(usersDTO, users);
        return usersRepository.save(users).getId();
    }

    public void update(final Long id, final UsersDTO usersDTO) {
        final Users users = usersRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        requestProcessor().process(usersDTO, users);
        usersRepository.save(users);
    }

    public void delete(final Long id) {
        final Users users = usersRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        usersRepository.delete(users);
    }

    public boolean usernameExists(final String username) {
        return usersRepository.existsByUsernameIgnoreCase(username);
    }

    @EventListener(BeforeDeleteRole.class)
    public void on(final BeforeDeleteRole event) {
        // remove many-to-many relations at owning side
        usersRepository.findAllByRoleId(event.getId()).forEach(users ->
                users.getRole().removeIf(role -> role.getId().equals(event.getId())));
    }

    public Users findByUsername(String name) {
        return usersRepository.findByUsernameIgnoreCase(name);
    }

    public void logoutAllDevices(String username) {
        Users user = usersRepository.findByUsernameIgnoreCase(username);
        if (user == null) {
            return;
        }
        user.setTokenVersion(user.getTokenVersion() + new Random().nextInt());
        usersRepository.save(user);
    }


}
