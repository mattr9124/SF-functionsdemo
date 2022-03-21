package com.searchindexer.indexer.valueprovider;

import com.salesforce.functions.jvm.sdk.data.DataApi;
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;
import com.salesforce.functions.jvm.sdk.data.error.DataApiException;

public abstract class BaseValueProvider<T> implements ValueProvider<T> {

    protected RecordQueryResult querySwallowException(DataApi dataApi, String query) {
        try {
            return dataApi.query(query);
        } catch (DataApiException e) {
            throw new RuntimeException(e);
        }
    }
}
