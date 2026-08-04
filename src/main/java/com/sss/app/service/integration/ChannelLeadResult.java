package com.sss.app.service.integration;

/**
 * Outcome of a channel-sourced lead-creation attempt, for the orchestrator
 * to log into lead_import_attempts without re-querying anything.
 */
public record ChannelLeadResult(Long leadId, boolean wasDuplicate) {}
