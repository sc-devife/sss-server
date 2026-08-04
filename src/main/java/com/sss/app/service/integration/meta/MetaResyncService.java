package com.sss.app.service.integration.meta;

import com.sss.app.entity.integration.meta.LeadImportAttempt;
import com.sss.app.exception.ConflictException;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.integration.meta.LeadImportAttemptRepository;
import com.sss.app.security.OrgAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Manual re-sync of a failed lead import. Re-fetches the token/lead fresh
 * (never trusts the stale raw_merged_payload for the token — the connection
 * may have been reconnected since the failure) via
 * MetaWebhookOrchestrationService.retryImport, then updates the same
 * LeadImportAttempt row in place.
 */
@Component
@RequiredArgsConstructor
public class MetaResyncService {

    private final LeadImportAttemptRepository importAttemptRepository;
    private final MetaWebhookOrchestrationService orchestrationService;
    private final OrgAccessGuard orgAccessGuard;

    public LeadImportAttempt resync(Long attemptId) {
        LeadImportAttempt attempt = importAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new NotFoundException("Import attempt not found"));

        orgAccessGuard.requireAccessToOrg(attempt.getOrgId());

        if (!LeadImportAttempt.STATUS_FAILED.equals(attempt.getStatus())) {
            throw new ConflictException("Only failed import attempts can be resynced");
        }

        return orchestrationService.retryImport(attempt);
    }
}
