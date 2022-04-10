package com.fileuploader;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.Org;
import com.salesforce.functions.jvm.sdk.SalesforceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Describe LargefileuploadFunction here.
 */
public class LargefileuploadFunction implements SalesforceFunction<UploadInput, UploadOutput> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LargefileuploadFunction.class);

    public final static String VERSION_URL = "/services/data/v%s/sobjects/ContentVersion/%s/VersionData";

    private HttpClientProvider httpClientProvider;

    public LargefileuploadFunction() {
        this.httpClientProvider = new HttpClientProvider() {};
    }

    // package private/for testing purposes only
    LargefileuploadFunction(HttpClientProvider httpClientProvider) {
        this.httpClientProvider = httpClientProvider;
    }

    @Override
    public UploadOutput apply(InvocationEvent<UploadInput> event, Context context)
            throws Exception {

        String contentVersionId = event.getData().contentVersionId;

        LOGGER.info("Downloading content version with ID {}", contentVersionId);

        HttpClient client = httpClientProvider.getHttpClient();

        Org org = context.getOrg().get();

        HttpRequest request = getRequestBuilder(org)
                .uri(URI.create(buildEndpoint(org, contentVersionId)))
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            LOGGER.error("Error response code: {}", response.statusCode() );
            LOGGER.error(response.body().toString());
        }

        Converter converter = new CsvConverter();

        InputStream inputStream = response.body();

        List<UploadOutput.CartEntry> cartEntries = converter.convert(inputStream);

        return new UploadOutput(cartEntries);
    }

    protected HttpRequest.Builder getRequestBuilder(Org org) {
        return HttpRequest.newBuilder()
                .GET()
                .setHeader("Authorization", "Bearer %s".formatted(org.getDataApi().getAccessToken()));
    }

    protected String buildEndpoint(Org org, String contentVersionId) {
        return org.getBaseUrl() + VERSION_URL.formatted(org.getApiVersion(), contentVersionId);
    }
}
