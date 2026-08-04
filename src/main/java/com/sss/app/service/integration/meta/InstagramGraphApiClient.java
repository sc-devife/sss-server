package com.sss.app.service.integration.meta;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InstagramGraphApiClient extends MetaGraphApiClient {

    public InstagramGraphApiClient(RestClient restClient) {
        super(restClient);
    }

    @Override
    public String providerCode() {
        return "instagram";
    }
}
