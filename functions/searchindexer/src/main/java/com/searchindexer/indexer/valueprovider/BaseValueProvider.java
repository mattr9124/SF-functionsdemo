package com.searchindexer.indexer.valueprovider;

import com.salesforce.functions.jvm.sdk.Org;
import com.salesforce.functions.jvm.sdk.data.DataApi;
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;
import com.salesforce.functions.jvm.sdk.data.error.DataApiException;
import com.searchindexer.SearchIndexerInput;

public abstract class BaseValueProvider<T> implements ValueProvider<T> {

    private final Org org;
    private final SearchIndexerInput searchIndexerInput;

    public BaseValueProvider(Org org, SearchIndexerInput searchIndexerInput) {
        this.org = org;
        this.searchIndexerInput = searchIndexerInput;
    }

    protected RecordQueryResult querySwallowException(DataApi dataApi, String query) {
        try {
            return dataApi.query(query);
        } catch (DataApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Org getOrg() {
        return org;
    }

    public DataApi getDataApi() {
        return org.getDataApi();
    }

    public SearchIndexerInput getSearchIndexerInput() {
        return searchIndexerInput;
    }
}
