package com.searchindexer.http;

import java.util.Map;

public class ManagedContentClient extends SalesforceRestClient {

    private final String communityId;

    public ManagedContentClient(String communityId) {
        this.communityId = communityId;
    }

    @Override
    protected String getEndPoint() {
        return BASE_APEX_URL + "/cmswrapper/getmanagedcontent";
    }

    @Override
    protected Map<String, String> getParameters() {
        return Map.of("communityId", communityId,
                "language", "en_US");
    }
}
