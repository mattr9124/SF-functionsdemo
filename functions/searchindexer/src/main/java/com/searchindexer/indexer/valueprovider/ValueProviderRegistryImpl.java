package com.searchindexer.indexer.valueprovider;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ValueProviderRegistryImpl implements ValueProviderRegistry {
    @Override
    public ValueProvider<List<String>> getValueProvider(String valueProvider) {
        // TODO build some kind of registry
        try {
            return (ValueProvider<List<String>>) Class.forName(valueProvider).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create value provider %s".formatted(valueProvider), e);
        }
    }
}
