package com.sss.app.service.integration.impl;

import com.sss.app.service.integration.meta.MetaFieldMappingResolver;
import org.springframework.stereotype.Component;

/**
 * Instagram Lead Ads use the identical Graph API leadgen webhook/fetch
 * mechanism as Facebook Lead Ads, differing only in which account type owns
 * the form (Instagram Business Account vs. Facebook Page) — so this is a
 * thin subclass reusing all of MetaLeadChannelAdapter's normalization logic.
 */
@Component
public class InstagramLeadChannelAdapter extends MetaLeadChannelAdapter {

    public InstagramLeadChannelAdapter(MetaFieldMappingResolver fieldMappingResolver) {
        super(fieldMappingResolver);
    }

    @Override
    public String channelCode() {
        return "instagram";
    }
}
