package com.searchindexer.indexer;

import com.salesforce.functions.jvm.sdk.data.Record;
import com.searchindexer.SearchIndexerInput;
import com.searchindexer.indexer.valueprovider.ValueProvider;
import org.apache.solr.common.SolrInputDocument;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class SolrDocumentBuilder {

    public SolrInputDocument buildSolrDocument(List<SearchIndexerInput.SearchField> searchFields, Record product) {
        SolrInputDocument document = new SolrInputDocument();
        // TODO get this from config
        String id = product.getStringField("Id").get();

        // always set Id
        document.addField("id", id);

        searchFields.forEach(searchField -> {
            if (searchField.valueProvider.isBlank()) {
                String indexedName = searchField.name;

                switch (searchField.type) {
                    case String -> indexedName += "_t";
                    case Number -> indexedName += "_d";
                    case Date -> indexedName += "_dt";
                } // text we do nothing? TBC

                document.addField(indexedName, product.getStringField(searchField.name).get());
            } else {
                ValueProvider<List<String>> valueProvider = getValueProvider(searchField.valueProvider);
            }
        });

//            document.addField("name_t", product.getStringField("Name").get());
//            document.addField("description_t", product.getStringField("Description").get());
//            document.addField("sku_t", product.getStringField("ProductCode").get());

        return document;
    }


    private ValueProvider<List<String>> getValueProvider(String valueProvider) {
        // TODO build some kind of registry
        try {
            return (ValueProvider<List<String>>) Class.forName(valueProvider).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create value provider %s".formatted(valueProvider), e);
        }
    }

}
