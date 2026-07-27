package com.sss.app.service.integration.impl;

import com.sss.app.dto.integration.IntegrationConnectRequestDTO;
import com.sss.app.dto.integration.IntegrationConnectionResponseDTO;
import com.sss.app.entity.integration.IntegrationConnection;
import com.sss.app.entity.users.User;
import com.sss.app.exception.BadRequestException;
import com.sss.app.repository.integration.IntegrationConnectionRepository;
import com.sss.app.service.integration.LeadChannel;
import com.sss.app.service.integration.IntegrationConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntegrationConnectionServiceImpl implements IntegrationConnectionService {

    private final IntegrationConnectionRepository integrationConnectionRepository;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public List<IntegrationConnectionResponseDTO> listForOrg() {
        Long orgId = currentUser().getOrgId();
        Map<String, IntegrationConnection> existing = integrationConnectionRepository.findAllByOrgId(orgId).stream()
                .collect(Collectors.toMap(IntegrationConnection::getChannelCode, c -> c));

        return LeadChannel.ALL.stream().map(channel -> {
            IntegrationConnectionResponseDTO dto = new IntegrationConnectionResponseDTO();
            dto.setChannelCode(channel.code());
            dto.setLabel(channel.label());
            dto.setAvailable(channel.available());
            IntegrationConnection connection = existing.get(channel.code());
            dto.setStatus(connection != null ? connection.getStatus() : "disconnected");
            dto.setAutoCreateLeads(connection != null ? connection.getAutoCreateLeads() : true);
            dto.setLastSyncedAt(connection != null ? connection.getLastSyncedAt() : null);
            return dto;
        }).toList();
    }

    @Override
    public IntegrationConnectionResponseDTO connect(String channelCode, IntegrationConnectRequestDTO request) {
        if (!LeadChannel.exists(channelCode)) {
            throw new BadRequestException("Unknown channel: " + channelCode);
        }
        if (!LeadChannel.isAvailable(channelCode)) {
            throw new BadRequestException("This channel is coming soon and isn't connectable yet");
        }

        Long orgId = currentUser().getOrgId();
        IntegrationConnection connection = integrationConnectionRepository.findByOrgIdAndChannelCode(orgId, channelCode)
                .orElseGet(() -> IntegrationConnection.builder().orgId(orgId).channelCode(channelCode).build());

        connection.setStatus("connected");
        connection.setConfig(request.getSecret());
        connection.setAutoCreateLeads(request.getAutoCreateLeads() == null || request.getAutoCreateLeads());
        IntegrationConnection saved = integrationConnectionRepository.save(connection);
        return toResponse(saved);
    }

    @Override
    public IntegrationConnectionResponseDTO disconnect(String channelCode) {
        Long orgId = currentUser().getOrgId();
        IntegrationConnection connection = integrationConnectionRepository.findByOrgIdAndChannelCode(orgId, channelCode)
                .orElseThrow(() -> new BadRequestException("No connection exists for channel: " + channelCode));
        connection.setStatus("disconnected");
        return toResponse(integrationConnectionRepository.save(connection));
    }

    private IntegrationConnectionResponseDTO toResponse(IntegrationConnection connection) {
        IntegrationConnectionResponseDTO dto = new IntegrationConnectionResponseDTO();
        LeadChannel channel = LeadChannel.ALL.stream()
                .filter(c -> c.code().equals(connection.getChannelCode()))
                .findFirst()
                .orElse(new LeadChannel(connection.getChannelCode(), connection.getChannelCode(), false));
        dto.setChannelCode(connection.getChannelCode());
        dto.setLabel(channel.label());
        dto.setAvailable(channel.available());
        dto.setStatus(connection.getStatus());
        dto.setAutoCreateLeads(connection.getAutoCreateLeads());
        dto.setLastSyncedAt(connection.getLastSyncedAt());
        return dto;
    }
}
