package com.searchindexer.indexer.valueprovider;

import com.salesforce.functions.jvm.sdk.Org;
import com.searchindexer.SearchIndexerInput;

import java.lang.reflect.InvocationTargetException;

public class ValueProviderRegistryImpl implements ValueProviderRegistry {

    private final static String VALUE_PROVIDER_PACKAGE = ValueProviderRegistryImpl.class.getPackageName();

    @Override
    public ValueProvider<?> getValueProvider(Org org, SearchIndexerInput searchIndexerInput, String valueProvider) {
        // TODO build some kind of registry
        try {
            return (ValueProvider<?>) Class.forName(VALUE_PROVIDER_PACKAGE + "." + valueProvider)
                    .getDeclaredConstructor(Org.class, SearchIndexerInput.class).newInstance(org, searchIndexerInput);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create value provider %s".formatted(valueProvider), e);
        }
    }
}
