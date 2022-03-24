package com.mattrossner.sf.functions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.salesforce.functions.jvm.runtime.sdk.restapi.*;
import com.salesforce.functions.jvm.runtime.sdk.restapi.Record;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class EnhancedQueryRecordRestApiProcessor implements RestApiRequest<QueryRecordResult> {

    final private RestApiRequest<QueryRecordResult> delegate;

    public EnhancedQueryRecordRestApiProcessor(RestApiRequest<QueryRecordResult> delegateRequest) {
        this.delegate = delegateRequest;
    }

    @Override
    public QueryRecordResult processResponse(int statusCode, Map<String, String> headers, JsonElement json) throws RestApiErrorsException {
        final Gson gson = new Gson();

        if (statusCode != 200) {
            throw new RestApiErrorsException(ErrorResponseParser.parse(json));
        } else {
            final JsonObject body = json.getAsJsonObject();
            final boolean done = body.get("done").getAsBoolean();
            final long totalSize = body.get("totalSize").getAsLong();
            final List<com.salesforce.functions.jvm.runtime.sdk.restapi.Record> records = new ArrayList<>();

            final String nextRecordsPath;
            if (body.get("nextRecordsUrl") == null || body.get("nextRecordsUrl").isJsonNull()) {
                nextRecordsPath = null;
            } else {
                nextRecordsPath = body.get("nextRecordsUrl").getAsString();
            }

            for (JsonElement jsonElement : body.get("records").getAsJsonArray()) {
                final JsonObject jsonObject = jsonElement.getAsJsonObject();

                final JsonObject attributesObject = jsonObject.get("attributes").getAsJsonObject();
                final Map<String, JsonPrimitive> attributes =
                        gson.fromJson(
                                attributesObject, new TypeToken<Map<String, JsonPrimitive>>() {
                                }.getType());

                final Map<String, JsonPrimitive> values = new HashMap<>();
for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
    if (entry.getKey().equals("attributes")) {
        continue;
    }

    if (entry.getValue().isJsonPrimitive()) {
        values.put(entry.getKey(), entry.getValue().getAsJsonPrimitive());
    } else if (entry.getValue().isJsonNull()) {
        // We don't add the value if it's null.
    } else if (entry.getValue().isJsonObject()) {
        // sub object - we can probably do something recursive
        // but for now I just want to handle 1 level deep
        String subLevelKey = entry.getKey();
        JsonObject subObject = entry.getValue().getAsJsonObject();
        subObject.entrySet().stream()
                .filter(subEntry -> subEntry.getValue().isJsonPrimitive())
                .forEach(subEntry -> values.put(
                        String.join(".", subLevelKey, subEntry.getKey()),
                        subEntry.getValue().getAsJsonPrimitive()));
    } else {
        throw new RuntimeException("Unexpected value in record response: " + entry.getValue());
    }
}

                records.add(new Record(attributes, values));
            }

            return new QueryRecordResult(totalSize, done, records, nextRecordsPath);
        }
    }

    @Override
    public HttpMethod getHttpMethod() {
        return delegate.getHttpMethod();
    }

    @Override
    public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
        return delegate.createUri(baseUri, apiVersion);
    }

    @Override
    public Optional<JsonElement> getBody() {
        return delegate.getBody();
    }
}