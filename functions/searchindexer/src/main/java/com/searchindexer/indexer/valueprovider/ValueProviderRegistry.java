package com.searchindexer.indexer.valueprovider;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface ValueProviderRegistry {

    ValueProvider<List<String>> getValueProvider(String valueProvider);

}
