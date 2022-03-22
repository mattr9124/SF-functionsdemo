package com.searchindexer.indexer.valueprovider;

import com.salesforce.functions.jvm.sdk.Org;
import com.searchindexer.SearchIndexerInput;

import java.util.List;

public interface ValueProviderRegistry {

    ValueProvider<List<String>> getValueProvider(Org org, SearchIndexerInput searchIndexerInput, String valueProvider);

}
