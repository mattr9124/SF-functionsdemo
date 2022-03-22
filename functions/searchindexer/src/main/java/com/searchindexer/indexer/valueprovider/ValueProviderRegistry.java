package com.searchindexer.indexer.valueprovider;

import com.salesforce.functions.jvm.sdk.Org;
import com.searchindexer.SearchIndexerInput;

public interface ValueProviderRegistry {

    ValueProvider<?> getValueProvider(Org org, SearchIndexerInput searchIndexerInput, String valueProvider);

}
