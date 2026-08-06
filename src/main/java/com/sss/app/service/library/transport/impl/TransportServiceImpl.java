package com.sss.app.service.library.transport.impl;

import com.sss.app.dto.library.transport.TransportCreateRequestDTO;
import com.sss.app.dto.library.transport.TransportResponseDTO;
import com.sss.app.dto.library.transport.TransportUpdateRequestDTO;
import com.sss.app.entity.library.transport.Transport;
import com.sss.app.entity.users.User;
import com.sss.app.exception.ResourceNotFoundException;
import com.sss.app.helper.library.transport.TransportHelper;
import com.sss.app.mapper.library.transport.TransportMapper;
import com.sss.app.repository.library.transport.TransportRepository;
import com.sss.app.security.OrgAccessGuard;
import com.sss.app.service.library.transport.TransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TransportServiceImpl implements TransportService {

    private final TransportRepository transportRepository;
    private final TransportMapper transportMapper;
    private final TransportHelper transportHelper;
    private final OrgAccessGuard orgAccessGuard;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public TransportResponseDTO create(TransportCreateRequestDTO dto) {
        Transport transport = transportMapper.toEntityCreate(dto);
        transport.setOrgId(currentUser().getOrgId());
        if (dto.getProviderId() != null) {
            transport.setProvider(transportHelper.resolveProvider(dto.getProviderId()));
        }
        if (dto.getEscapePointId() != null) {
            transport.setEscapePoint(transportHelper.resolveEscapePoint(dto.getEscapePointId()));
        }
        Transport saved = transportRepository.save(transport);
        return transportMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransportResponseDTO getById(UUID id) {
        return transportMapper.toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransportResponseDTO> getAll() {
        return transportRepository.findAllByOrgIdAndDeletedAtIsNull(currentUser().getOrgId())
                .stream()
                .map(transportMapper::toResponse)
                .toList();
    }

    @Override
    public TransportResponseDTO update(UUID id, TransportUpdateRequestDTO dto) {
        Transport transport = findEntityById(id);
        transportMapper.updateEntityFromDto(dto, transport);
        if (dto.getProviderId() != null) {
            transport.setProvider(transportHelper.resolveProvider(dto.getProviderId()));
        }
        if (dto.getEscapePointId() != null) {
            transport.setEscapePoint(transportHelper.resolveEscapePoint(dto.getEscapePointId()));
        }
        Transport saved = transportRepository.save(transport);
        return transportMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        Transport transport = findEntityById(id);
        transport.setDeletedAt(LocalDateTime.now());
        transport.setStatus("archived");
    }

    private Transport findEntityById(UUID id) {
        Transport transport = transportRepository.findByUid(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transport", id));
        orgAccessGuard.requireAccessToOrg(transport.getOrgId());
        return transport;
    }
}
