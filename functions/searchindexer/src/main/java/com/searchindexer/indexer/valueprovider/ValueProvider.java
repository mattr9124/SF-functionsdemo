package com.searchindexer.indexer.valueprovider;

import com.salesforce.functions.jvm.sdk.data.Record;

public interface ValueProvider<T> {
    T getValue(Record product);
}
