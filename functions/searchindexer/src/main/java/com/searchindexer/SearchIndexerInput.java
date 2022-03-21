package com.searchindexer;

import java.util.List;

public class SearchIndexerInput {
    public int batchSize;
    public String serviceUrl;
    public List<SearchField> searchFields;

    public class SearchField {
        public FieldType type;
        public boolean multiValue;
        public String name;
        public String valueProvider;
    }

    public enum FieldType {
        String, Number, Date, Text
    }
}
