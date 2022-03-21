package com.searchindexer.indexer.valueprovider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.salesforce.functions.jvm.sdk.data.DataApi;
import com.salesforce.functions.jvm.sdk.data.Record;
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CategoryValueProvider extends BaseValueProvider<List<String>> {

    final static Cache<String, List<Record>> categoryCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    @Override
    public List<String> getValue(DataApi dataApi, Record product) {
        String id = product.getStringField("Id").get();
        String categoryQuery = "SELECT ProductId, ProductCategoryId\n" +
                "FROM ProductCategoryProduct\n" +
                "WHERE ProductId = '%s'".formatted(id);

        RecordQueryResult categories = querySwallowException(dataApi, categoryQuery);

        List<Record> allCategories = categoryCache.get(id, f -> querySwallowException(dataApi, "SELECT Id, Name, ParentCategoryId FROM ProductCategory").getRecords());
        //build category hierarchy
        Map<String, Category> categoryMap = buildCategoryMap(allCategories);

        List<String> hierarchy = new ArrayList<>();

        categories.getRecords().stream()
                .map(cat -> cat.getStringField("ProductCategoryId").get())
                .forEach( catId -> {
                            Category nextCategory = categoryMap.get(catId);
                            hierarchy.add(nextCategory.name);
                            while (!nextCategory.parentId.isEmpty()) {
                                nextCategory = categoryMap.get(nextCategory.parentId);
                                hierarchy.add(nextCategory.name);
                            }
                        }
                );

        return hierarchy;
    }


    private Map<String, Category> buildCategoryMap(List<Record> allCategories) {
        return allCategories.stream().map(rec ->
                new Category(rec.getStringField("Id").get(),
                        rec.getStringField("ParentCategoryId").orElse(""),
                        rec.getStringField("Name").get())).collect(
                Collectors.toMap(cat -> cat.id, Function.identity()));
    }

    private class Category {
        final String id;
        final String parentId;
        final String name;

        public Category(String id, String parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Category category = (Category) o;

            if (id != null ? !id.equals(category.id) : category.id != null) return false;
            if (parentId != null ? !parentId.equals(category.parentId) : category.parentId != null) return false;
            return name != null ? name.equals(category.name) : category.name == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }

}
