package com.searchindexer.indexer.valueprovider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.salesforce.functions.jvm.sdk.Org;
import com.salesforce.functions.jvm.sdk.data.DataApi;
import com.salesforce.functions.jvm.sdk.data.Record;
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;
import com.searchindexer.SearchIndexerInput;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BuyerGroupValueProvider extends BaseValueProvider<List<String>> {
    final static Cache<String, List<Record>> policyProductCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    public BuyerGroupValueProvider(Org org, SearchIndexerInput searchIndexerInput) {
        super(org, searchIndexerInput);
    }

    @Override
    public List<String> getValue(Record product) {
        List<String> policyIds = getPolicyIdsForProduct(getDataApi(), product.getStringField("Id").get());
        return getBuyerGroupsForProduct(getDataApi(), policyIds);
    }

    private List<String> getPolicyIdsForProduct(DataApi dataApi, String id) {
        List<Record> policyIds = policyProductCache.get(id, f -> {
            String query = "SELECT PolicyId FROM CommerceEntitlementProduct WHERE ProductId = '%s'".formatted(id);
            return querySwallowException(dataApi, query).getRecords();
        });

        return policyIds.stream()
                .map(rec -> rec.getStringField("PolicyId").get())
                .collect(Collectors.toList());
    }

    private List<String> getBuyerGroupsForProduct(DataApi dataApi, List<String> policyIds) {
        String query = "SELECT BuyerGroupId " +
                "FROM CommerceEntitlementBuyerGroup " +
                "WHERE PolicyId IN (%s)".formatted(
                        policyIds.stream().map(id -> "'%s'".formatted(id)).collect(Collectors.joining(","))
                );

        RecordQueryResult buyerGroups = querySwallowException(dataApi, query);

        return buyerGroups.getRecords().stream().map(rec -> rec.getStringField("BuyerGroupId").get()).collect(Collectors.toList());
    }
}
