package com.searchindexer.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.salesforce.functions.jvm.sdk.Org;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class SalesforceRestClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(SalesforceRestClient.class);

    public final static String BASE_DATA_URL = "/services/data/%s";
    public final static String BASE_APEX_URL = "/services/apexrest";

    private final HttpClient httpClient;

    public SalesforceRestClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public SalesforceRestClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public JsonElement execute(Org org) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(buildEndpoint(org)))
                .setHeader("Authorization", "Bearer %s".formatted(org.getDataApi().getAccessToken()))
                .build();

        LOGGER.info("Sending request: {}", httpRequest.uri());

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        String body = httpResponse.body();

        if (httpResponse.statusCode() != 200) {
            LOGGER.error("HTTP CLIENT ERROR");
            LOGGER.error("Status: {}", httpResponse.statusCode());
            LOGGER.error("Body: {}", body);

            throw new RuntimeException("API Error - check logs"); // TODO handle better
        }

        HttpHeaders headers = httpResponse.headers();


        if (!headers.map().get("Content-Type").stream()
                .filter(s -> s.contains("application/json"))
                .findAny().isPresent()) {
            LOGGER.error("NON JSON RESPONSE");
            LOGGER.error("Status: {}", httpResponse.statusCode());
            LOGGER.error("Body: {}", body);
            throw new RuntimeException("API Error - check logs");
        }

        Gson gson = new Gson();
        JsonElement json = gson.fromJson(gson.fromJson(body, JsonElement.class).getAsString(), JsonElement.class);

        return json;
    }

    private String buildEndpoint(Org org) {
        String url = org.getBaseUrl() + getEndPoint().formatted(org.getApiVersion());

        Map<String, String> params = getParameters();

        String queryString = params.entrySet().stream()
                .map(e -> "%s=%s".formatted(e.getKey(), encodeValue(e.getValue())))
                .collect(Collectors.joining("&"));

        return url + "?" + queryString;
    }

    private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // swallow
            return null;
        }
    }

    protected abstract String getEndPoint();

    protected abstract Map<String, String> getParameters();
}
