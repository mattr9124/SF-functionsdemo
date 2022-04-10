package com.fileuploader;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.Org;
import com.salesforce.functions.jvm.sdk.data.DataApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class LargefileuploadFunctionTest {

    @Test
    @DisplayName("Large file function test")
    public void testLargeFileFunction() throws Exception {

        InvocationEvent<UploadInput> event = buildMockEvent();
        Context context = buildMockContext();
        HttpClientProvider mockProvider = buildMockHttpProvider();

        // when upload a file
        LargefileuploadFunction function = new LargefileuploadFunction(mockProvider);

        UploadOutput output = function.apply(event, context);

        // then output should have 3 cart entries
        assertNotNull(output);
        assertEquals(3, output.cartEntries.size());

    }

    private HttpClientProvider buildMockHttpProvider() throws IOException, InterruptedException {
        HttpClientProvider mockProvider = mock(HttpClientProvider.class);
        HttpClient client = mock(HttpClient.class);
        when(mockProvider.getHttpClient()).thenReturn(client);
        InputStream is = getDataAsStream();
        HttpResponse<InputStream> response = mock(HttpResponse.class);
        when(response.body()).thenReturn(is);
        when(response.statusCode()).thenReturn(200);
        when(client.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        return mockProvider;
    }

    private Context buildMockContext() {
        DataApi dataApi = mock(DataApi.class);
        when(dataApi.getAccessToken()).thenReturn("54321");
        Org org = mock(Org.class);
        when(org.getApiVersion()).thenReturn("v54.0");
        when(org.getDataApi()).thenReturn(dataApi);
        when(org.getBaseUrl()).thenReturn(URI.create("https://test.com"));
        Context context = mock(Context.class);
        when(context.getOrg()).thenReturn(Optional.of(org));
        return context;
    }

    private InvocationEvent<UploadInput> buildMockEvent() {
        InvocationEvent<UploadInput> event = mock(InvocationEvent.class);
        UploadInput input = new UploadInput();
        input.contentVersionId = "12345";
        when(event.getData()).thenReturn(input);
        return event;
    }

    private InputStream getDataAsStream() {
        return getClass().getResourceAsStream("/csvsamplefile.csv");
    }

}
