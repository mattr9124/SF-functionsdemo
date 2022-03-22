package com.searchindexer.indexer.valueprovider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.salesforce.functions.jvm.sdk.Org;
import com.salesforce.functions.jvm.sdk.data.Record;
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;
import com.searchindexer.SearchIndexerInput;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PriceValueProvider extends BaseValueProvider<Double> {
    final static Cache<String, Map<String, Double>> priceCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    public PriceValueProvider(Org org, SearchIndexerInput searchIndexerInput) {
        super(org, searchIndexerInput);
    }

    @Override
    public Double getValue(Record product) {
        Map<String, Double> prices = priceCache.get(getSearchIndexerInput().storeId, f -> getStrikethroughPrices());

        String productId = product.getStringField("Id").get();

        if (prices.containsKey(productId)) {
            return prices.get(productId);
        }

        return null;
    }


    private Map<String, Double> getStrikethroughPrices() {
        String soql = "SELECT Product2Id, UnitPrice FROM PricebookEntry " +
                "WHERE Pricebook2Id IN (" +
                "SELECT StrikethroughPricebookId FROM WebStore " +
                "WHERE Id = '%s')".formatted(getSearchIndexerInput().storeId);

        RecordQueryResult prices = querySwallowException(getDataApi(), soql);

        return prices.getRecords().stream().collect(Collectors.toMap(
                k -> k.getStringField("Product2Id").get(),
                v -> v.getDoubleField("UnitPrice").get()));
    }
}
