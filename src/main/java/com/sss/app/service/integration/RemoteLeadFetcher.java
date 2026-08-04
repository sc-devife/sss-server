package com.sss.app.service.integration;

import java.util.Map;

/**
 * For channels whose webhook delivers only an id, not the full lead (Meta's
 * leadgen webhook: you get leadgen_id/page_id/form_id/ad_id and must call
 * back to the Graph API for field_data). LeadChannelAdapter alone can't
 * model this — it assumes the raw payload it normalizes is already complete.
 * A channel whose webhook IS self-contained (WhatsApp, LinkedIn, generic
 * webhook) has no need to implement this at all.
 */
public interface RemoteLeadFetcher {

    /** Matches the LeadChannel.code() this fetcher serves, e.g. "facebook"/"instagram". */
    String providerCode();

    /** Fetches the full lead record from the provider given the id the webhook told us about. */
    Map<String, Object> fetchLead(String remoteLeadId, String accessToken);
}
