package com.searchindexer.indexer.valueprovider;

import com.salesforce.functions.jvm.sdk.data.DataApi;
import com.salesforce.functions.jvm.sdk.data.Record;

public interface ValueProvider<T> {
    T getValue(DataApi dataApi, Record product);
}
