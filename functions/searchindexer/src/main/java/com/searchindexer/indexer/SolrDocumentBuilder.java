package com.searchindexer.indexer;

import com.salesforce.functions.jvm.sdk.Org;
import com.salesforce.functions.jvm.sdk.data.DataApi;
import com.salesforce.functions.jvm.sdk.data.Record;
import com.searchindexer.SearchIndexerInput;
import com.searchindexer.indexer.valueprovider.ValueProvider;
import com.searchindexer.indexer.valueprovider.ValueProviderRegistry;
import org.apache.solr.common.SolrInputDocument;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SolrDocumentBuilder {

    private ValueProviderRegistry valueProviderRegistry;

    public SolrDocumentBuilder(ValueProviderRegistry valueProviderRegistry) {
        this.valueProviderRegistry = valueProviderRegistry;
    }

    public SolrInputDocument buildSolrDocument(Org org, SearchIndexerInput searchIndexerInput, Record product) {
        List<SearchIndexerInput.SearchField> searchFields = searchIndexerInput.searchFields;

        SolrInputDocument document = new SolrInputDocument();
        // TODO get this from config
        String id = product.getStringField("Id").get();

        // always set Id
        document.addField("id", id);

        searchFields.forEach(searchField -> {
            if (searchField.valueProvider == null || searchField.valueProvider.isBlank()) {
                String indexedSuffix = getSuffix(searchField);

                String indexedName = searchField.name + indexedSuffix;

                Optional<?> value = switch (searchField.type) {
                    case String, Text -> product.getStringField(searchField.name);
                    case Number ->      product.getDoubleField(searchField.name);
                    case Date ->        product.getLongField(searchField.name); // TODO check if date even works
                };

                value.ifPresent(v -> document.addField(indexedName, v));
            } else {
                ValueProvider<?> valueProvider = valueProviderRegistry.getValueProvider(org, searchIndexerInput, searchField.valueProvider);
                Object value = valueProvider.getValue(product);

                if (value instanceof Collection<?>) {
                    Collection<?> values = (Collection<?>) value;
                    if (!values.isEmpty()) {
                        document.addField(searchField.name, values);
                    }
                } else if (value != null) {
                    String indexedSuffix = getSuffix(searchField);
                    String indexedName = searchField.name + indexedSuffix;
                    document.addField(indexedName, value);
                }
            }
        });
        return document;
    }

    private String getSuffix(SearchIndexerInput.SearchField searchField) {
        String indexedSuffix = switch (searchField.type) {
            case String -> "_s";
            case Text -> "_t";
            case Number -> "_d";
            case Date -> "_dt";
        };
        return indexedSuffix;
    }


}
