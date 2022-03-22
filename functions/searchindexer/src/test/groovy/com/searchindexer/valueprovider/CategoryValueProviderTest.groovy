package com.searchindexer.valueprovider

import com.salesforce.functions.jvm.sdk.Org
import com.salesforce.functions.jvm.sdk.data.DataApi
import com.salesforce.functions.jvm.sdk.data.Record
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult
import com.searchindexer.SearchIndexerInput
import com.searchindexer.indexer.valueprovider.CategoryValueProvider
import com.searchindexer.indexer.valueprovider.ValueProvider
import spock.lang.Specification

class CategoryValueProviderTest extends Specification {

    def "get categories for product"() {

        setup: "create some mocks"

        def (Org org, Record product) = setupMocks()

        ValueProvider<List<String>> valueProvider = new CategoryValueProvider(org, new SearchIndexerInput())

        when:

        def result = valueProvider.getValue product

        then:

        result
        result.size() == 3

        result.contains 'Espresso Machines'
        result.contains 'Machines'
        result.contains 'Products'

    }

    private List setupMocks() {
        def org = Mock(Org)
        def dataApi = Mock(DataApi)

        org.getDataApi() >> dataApi

        def categories = Mock(RecordQueryResult)
        categories.records >> buildCategoryRecords()

        def productCategories = Mock(RecordQueryResult)
        productCategories.records >> buildProductCategoryRecords()

        dataApi.query(_) >>> [categories, productCategories]

        def product = Mock(Record)
        product.getStringField("Id") >> Optional.of("01t7a00000AmE0IAAV");
        [org, product]
    }

    List<Record> buildCategoryRecords() {
        Record record = Mock(Record)
        record.getStringField("ProductId") >> Optional.of("01t7a00000AmE0IAAV")
        record.getStringField("ProductCategoryId") >> Optional.of("0ZG7a0000000GeWGAU")
        [record]
    }

    List<Record> buildProductCategoryRecords() {
        Record record0 = makeCategoryRecord('0ZG7a0000000GeQGAU', 'Products', null)
        Record record1 = makeCategoryRecord('0ZG7a0000000GeXGAU', 'Coffee Machines', '0ZG7a0000000GeVGAU')
        Record record2 = makeCategoryRecord('0ZG7a0000000GeVGAU', 'Machines', '0ZG7a0000000GeQGAU')
        Record record3 = makeCategoryRecord('0ZG7a0000000GeWGAU', 'Espresso Machines', '0ZG7a0000000GeVGAU')
        Record record4 = makeCategoryRecord('0ZG7a0000000GeYGAU', 'Coffee Beans', '0ZG7a0000000GeQGAU')
        [record0, record1, record2, record3, record4]
    }

    private Record makeCategoryRecord(def id, def name, def parentId) {
        Record record = Mock(Record)
        record.getStringField("Id") >> Optional.of(id)
        record.getStringField("Name") >> Optional.of(name)
        record.getStringField("ParentCategoryId") >> (parentId == null ? Optional.empty() : Optional.of(parentId))
        record
    }
}
