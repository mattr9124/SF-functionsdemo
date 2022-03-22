package com.searchindexer.indexer.valueprovider;

import com.salesforce.functions.jvm.sdk.Org;
import com.searchindexer.SearchIndexerInput;

import java.lang.reflect.InvocationTargetException;

public class ValueProviderRegistryImpl implements ValueProviderRegistry {
    @Override
    public ValueProvider<?> getValueProvider(Org org, SearchIndexerInput searchIndexerInput, String valueProvider) {
        // TODO build some kind of registry
        try {
            return (ValueProvider<?>) Class.forName(valueProvider)
                    .getDeclaredConstructor(Org.class, SearchIndexerInput.class).newInstance(org, searchIndexerInput);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create value provider %s".formatted(valueProvider), e);
        }
    }
}
