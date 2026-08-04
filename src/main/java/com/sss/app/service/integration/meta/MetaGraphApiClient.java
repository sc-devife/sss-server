package com.sss.app.service.integration.meta;

import com.sss.app.service.integration.RemoteLeadFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Thin wrapper over the Meta Graph API's leadgen/node endpoints. Lives
 * entirely in the integration package — never touches Lead/LeadRepository
 * directly, per the "don't couple Facebook/Instagram to the Lead module"
 * requirement. Facebook and Instagram Lead Ads share the exact same Graph
 * API shape, so InstagramGraphApiClient is a one-line subclass.
 */
@Component
@RequiredArgsConstructor
public class MetaGraphApiClient implements RemoteLeadFetcher {

    private static final String GRAPH_BASE = "https://graph.facebook.com/v19.0";

    private final RestClient restClient;

    @Override
    public String providerCode() {
        return "facebook";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchLead(String leadgenId, String accessToken) {
        return restClient.get()
                .uri(GRAPH_BASE + "/{id}?fields=field_data,ad_id,ad_name,adset_id,campaign_id,campaign_name,form_id,created_time&access_token={token}",
                        leadgenId, accessToken)
                .retrieve()
                .body(Map.class);
    }

    /** Lightweight existence/ownership check used at connect time, before persisting anything. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyAccount(String accountId, String accessToken) {
        return restClient.get()
                .uri(GRAPH_BASE + "/{id}?fields=name&access_token={token}", accountId, accessToken)
                .retrieve()
                .body(Map.class);
    }
}
