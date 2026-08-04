package com.sss.app.service.integration.impl;

import com.sss.app.dto.integration.IntegrationConnectRequestDTO;
import com.sss.app.dto.integration.IntegrationConnectionResponseDTO;
import com.sss.app.entity.integration.IntegrationConnection;
import com.sss.app.entity.integration.meta.MetaChannelConfig;
import com.sss.app.entity.users.User;
import com.sss.app.exception.BadRequestException;
import com.sss.app.repository.integration.IntegrationConnectionRepository;
import com.sss.app.repository.integration.meta.MetaChannelConfigRepository;
import com.sss.app.security.crypto.TokenEncryptionService;
import com.sss.app.service.integration.LeadChannel;
import com.sss.app.service.integration.IntegrationConnectionService;
import com.sss.app.service.integration.meta.MetaGraphApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntegrationConnectionServiceImpl implements IntegrationConnectionService {

    private static final Set<String> META_CHANNELS = Set.of("facebook", "instagram");

    private final IntegrationConnectionRepository integrationConnectionRepository;
    private final MetaChannelConfigRepository metaChannelConfigRepository;
    private final List<MetaGraphApiClient> metaGraphApiClients;
    private final TokenEncryptionService tokenEncryptionService;

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
            if (connection != null && META_CHANNELS.contains(channel.code())) {
                applyMetaFields(dto, connection.getSeqp());
            }
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
        connection.setAutoCreateLeads(request.getAutoCreateLeads() == null || request.getAutoCreateLeads());

        if (META_CHANNELS.contains(channelCode)) {
            connection.setConfig(null); // Meta connections store their config in meta_channel_configs, not this text column
            IntegrationConnection saved = integrationConnectionRepository.save(connection);
            connectMetaChannel(channelCode, orgId, saved, request);
            return toResponse(saved);
        }

        connection.setConfig(request.getSecret());
        IntegrationConnection saved = integrationConnectionRepository.save(connection);
        return toResponse(saved);
    }

    private void connectMetaChannel(String channelCode, Long orgId, IntegrationConnection connection, IntegrationConnectRequestDTO request) {
        String accountId = "instagram".equals(channelCode) ? request.getIgAccountId() : request.getPageId();
        if (accountId == null || accountId.isBlank()) {
            throw new BadRequestException("instagram".equals(channelCode) ? "igAccountId is required" : "pageId is required");
        }
        if (request.getAccessToken() == null || request.getAccessToken().isBlank()) {
            throw new BadRequestException("accessToken is required");
        }

        // Fail fast on a bad/expired token rather than silently storing something unusable.
        MetaGraphApiClient graphClient = resolveGraphClient(channelCode);
        try {
            graphClient.verifyAccount(accountId, request.getAccessToken());
        } catch (Exception e) {
            throw new BadRequestException("Could not verify this access token against the given "
                    + ("instagram".equals(channelCode) ? "Instagram Business Account id" : "Facebook Page id")
                    + " — check the token and id are correct and the token hasn't expired.");
        }

        TokenEncryptionService.EncryptedValue encrypted = tokenEncryptionService.encrypt(request.getAccessToken());

        MetaChannelConfig config = metaChannelConfigRepository.findByConnectionId(connection.getSeqp())
                .orElseGet(() -> MetaChannelConfig.builder().connectionId(connection.getSeqp()).orgId(orgId).build());

        config.setPlatform(channelCode);
        config.setPageId("facebook".equals(channelCode) ? accountId : null);
        config.setIgAccountId("instagram".equals(channelCode) ? accountId : null);
        config.setPageName(request.getPageName());
        config.setTokenType("manual_long_lived");
        config.setEncryptedAccessToken(encrypted.ciphertextBase64());
        config.setTokenIv(encrypted.ivBase64());
        config.setTokenLastVerifiedAt(LocalDateTime.now());
        config.setConnectedByUserId(currentUser().getSeqp());
        metaChannelConfigRepository.save(config);
    }

    private MetaGraphApiClient resolveGraphClient(String channelCode) {
        return metaGraphApiClients.stream()
                .filter(c -> c.providerCode().equals(channelCode))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Graph API client registered for channel " + channelCode));
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
        if (META_CHANNELS.contains(connection.getChannelCode())) {
            applyMetaFields(dto, connection.getSeqp());
        }
        return dto;
    }

    private void applyMetaFields(IntegrationConnectionResponseDTO dto, Long connectionId) {
        metaChannelConfigRepository.findByConnectionId(connectionId).ifPresent(config -> {
            dto.setPlatform(config.getPlatform());
            dto.setPageId(config.getPageId());
            dto.setIgAccountId(config.getIgAccountId());
            dto.setPageName(config.getPageName());
            dto.setTokenLastVerifiedAt(config.getTokenLastVerifiedAt());
        });
    }
}
