package com.searchindexer.indexer.valueprovider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.salesforce.functions.jvm.sdk.Org;
import com.salesforce.functions.jvm.sdk.data.Record;
import com.searchindexer.SearchIndexerInput;
import com.searchindexer.http.ManagedContentClient;
import com.searchindexer.http.SalesforceRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MediaValueProvider extends BaseValueProvider<String> {
    private final static Logger LOGGER = LoggerFactory.getLogger(MediaValueProvider.class);

    final static Cache<String, Map<String, String>> mediaCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    final static Cache<String, List<Record>> productMediaCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    public MediaValueProvider(Org org, SearchIndexerInput searchIndexerInput) {
        super(org, searchIndexerInput);
    }

    @Override
    public String getValue(Record product) {
        Map<String, String> mediaIdUrlMap = mediaCache.get(getSearchIndexerInput().communityId, f -> getMediaIdUrlMap());
        List<Record> productMedias = productMediaCache.get(getSearchIndexerInput().communityId, f -> getProductMedia(mediaIdUrlMap.keySet()));

        Optional<Record> productMedia = productMedias.stream()
                .filter(pm -> pm.getStringField("ProductId").get().equals(product.getStringField("Id").get()))
                .findFirst();

        if (productMedia.isPresent()) {
            String mediaId = productMedia.get().getStringField("ElectronicMediaId").get();

            if (mediaIdUrlMap.containsKey(mediaId)) {
                return mediaIdUrlMap.get(mediaId);
            }
        }
        return null;
    }

    private List<Record> getProductMedia(Set<String> contentIds) {
        String soql = "SELECT ProductId, ElectronicMediaId " +
                "FROM ProductMedia " +
                "WHERE ElectronicMediaId IN (%s)".formatted(
                        contentIds.stream()
                                .map(id -> "'" + id + "'")
                                .collect(Collectors.joining(",")));

        return querySwallowException(getDataApi(), soql).getRecords();
    }

    private Map<String, String> getMediaIdUrlMap() {
        SalesforceRestClient client = new ManagedContentClient(getSearchIndexerInput().communityId);
        Map<String, String> mediaIdUrlMap = new HashMap<>();
        try {
            JsonElement result = client.execute(getOrg());


            result.getAsJsonObject()
                    .get("items")
                    .getAsJsonArray()
                    .iterator()
                    .forEachRemaining(item -> {
                        JsonObject itemAsJsonObject = item.getAsJsonObject();
                        String mediaId = itemAsJsonObject.get("managedContentId").getAsString();
                        String url = itemAsJsonObject.get("contentNodes")
                                .getAsJsonObject()
                                .get("source")
                                .getAsJsonObject()
                                .get("url")
                                .getAsString();
                        mediaIdUrlMap.put(mediaId, url);
                    });
        } catch (Exception e) {
            LOGGER.error("Error getting media", e);
        }
        return mediaIdUrlMap;
    }
}
