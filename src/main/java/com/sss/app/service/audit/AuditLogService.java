package com.sss.app.service.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sss.app.dto.audit.AuditLogResponseDTO;
import com.sss.app.entity.audit.AuditLog;
import com.sss.app.entity.users.User;
import com.sss.app.repository.UserRepository;
import com.sss.app.repository.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Single shared entry point for writing/reading audit history (Section 13
 * DRY principle) — every module that needs an audit trail (Lead now, Escape/
 * Itinerary/Quote later) calls record()/history() here rather than rolling
 * its own logging.
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private User currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof User user ? user : null;
    }

    public void record(String entityType, Long entityId, String action, Object previousValue, Object newValue) {
        User user = currentUser();
        AuditLog log = AuditLog.builder()
                .orgId(user != null ? user.getOrgId() : null)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .performedBy(user != null ? user.getSeqp() : null)
                .previousValue(toJson(previousValue))
                .newValue(toJson(newValue))
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> history(String entityType, Long entityId) {
        return auditLogRepository.findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    /**
     * Same as history(), but mapped to the response DTO with performedBy
     * resolved to a display name — the one place this resolution happens,
     * so every controller's audit-log endpoint shows the same "who" instead
     * of a bare user id.
     */
    public List<AuditLogResponseDTO> historyResponses(String entityType, Long entityId) {
        List<AuditLog> logs = history(entityType, entityId);

        Map<Long, String> namesById = new HashMap<>();
        for (AuditLog log : logs) {
            if (log.getPerformedBy() != null && !namesById.containsKey(log.getPerformedBy())) {
                namesById.put(log.getPerformedBy(),
                        userRepository.findById(log.getPerformedBy()).map(User::getName).orElse(null));
            }
        }

        return logs.stream().map(log -> {
            AuditLogResponseDTO dto = new AuditLogResponseDTO();
            dto.setAction(log.getAction());
            dto.setPerformedBy(log.getPerformedBy());
            dto.setPerformedByName(log.getPerformedBy() != null ? namesById.get(log.getPerformedBy()) : null);
            dto.setPreviousValue(log.getPreviousValue());
            dto.setNewValue(log.getNewValue());
            dto.setCreatedAt(log.getCreatedAt());
            return dto;
        }).toList();
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }
}
