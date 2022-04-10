package com.fileuploader;

import java.net.http.HttpClient;

public interface HttpClientProvider {
    default HttpClient getHttpClient() {
        return HttpClient.newBuilder().build();
    }
}
