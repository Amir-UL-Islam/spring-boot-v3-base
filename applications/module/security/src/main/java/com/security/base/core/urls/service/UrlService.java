package com.security.base.core.urls.service;

import com.security.base.core.urls.repository.UrlsRepository;
import com.security.base.core.urls.model.dto.UrlDTO;
import com.security.base.core.urls.model.entity.Url;
import com.security.base.events.BeforeDeletePrivilege;
import com.security.base.util.NotFoundException;
import com.problemfighter.pfspring.restapi.rr.RequestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UrlService implements RequestResponse {

    private final UrlsRepository urlsRepository;

    public Page<UrlDTO> findAll(final String filter, final Pageable pageable) {
        Page<Url> page;
        if (filter != null) {
            Long longFilter = null;
            try {
                longFilter = Long.parseLong(filter);
            } catch (final NumberFormatException numberFormatException) {
                // keep null - no parseable input
            }
            page = urlsRepository.findAllById(longFilter, pageable);
        } else {
            page = urlsRepository.findAll(pageable);
        }
        return new PageImpl<>(
                responseProcessor().entityToDTO(page.getContent(), UrlDTO.class),
                pageable,
                page.getTotalElements()
        );
    }

    public UrlDTO get(final Long id) {
        return urlsRepository.findById(id)
                .map(urls -> responseProcessor().entityToDTO(urls, UrlDTO.class))
                .orElseThrow(NotFoundException::new);
    }

    public Set<UrlDTO> getByPrivilege(final Long id) {
        return urlsRepository.findAllByPrivilegeId(id).stream()
                .map(url -> responseProcessor().entityToDTO(url, UrlDTO.class))
                .collect(Collectors.toSet());
    }

    public Long create(final UrlDTO urlsDTO) {
        final Url urls = new Url();
        requestProcessor().process(urlsDTO, urls);
        return urlsRepository.save(urls).getId();
    }

    public void update(final Long id, final UrlDTO urlsDTO) {
        final Url urls = urlsRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        requestProcessor().process(urlsDTO, urls);
        urlsRepository.save(urls);
    }

    public void delete(final Long id) {
        final Url url = urlsRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        // Remove this URL from all privileges that reference it (owning side)
        url.getPrivileges().forEach(privilege -> privilege.getUrls().remove(url));

        urlsRepository.delete(url);
    }


    public boolean endpointExists(final String endpoint) {
        return urlsRepository.existsByEndpointIgnoreCase(endpoint);
    }

    @EventListener(BeforeDeletePrivilege.class)
    public void on(final BeforeDeletePrivilege event) {
        // remove many-to-many relations at owning side
        urlsRepository.findAllByPrivilegeId(event.getId()).forEach(role ->
                role.getPrivileges().removeIf(privilege -> privilege.getId().equals(event.getId())));
    }

}
