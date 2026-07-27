package com.sss.app.service.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sss.app.entity.audit.AuditLog;
import com.sss.app.entity.users.User;
import com.sss.app.repository.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Single shared entry point for writing/reading audit history (Section 13
 * DRY principle) — every module that needs an audit trail (Lead now, Trip/
 * Itinerary/Quote later) calls record()/history() here rather than rolling
 * its own logging.
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
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

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }
}
