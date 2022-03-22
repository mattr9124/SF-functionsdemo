package com.searchindexer.valueprovider

import com.salesforce.functions.jvm.sdk.Org
import com.salesforce.functions.jvm.sdk.data.DataApi
import com.salesforce.functions.jvm.sdk.data.Record
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult
import com.searchindexer.SearchIndexerInput
import com.searchindexer.indexer.valueprovider.PriceValueProvider
import spock.lang.Specification

class PriceValueProviderTest extends Specification {
    def "get price for product"() {

        setup: "create some mocks"

        def (Org org, Record product) = setupMocks()

        def searchInput = new SearchIndexerInput();
        searchInput.storeId = '0ZE7a0000008oPbGAI'

        def valueProvider = new PriceValueProvider(org, searchInput)

        when:

        def result = valueProvider.getValue product

        then:

        result
        result == 599.99d
    }

    private List setupMocks() { // TODO can refactor a bit - it's same in a few classes now ...
        def org = Mock(Org)
        def dataApi = Mock(DataApi)

        org.getDataApi() >> dataApi

        def prices = Mock(RecordQueryResult)
        prices.records >> buildPriceRecords()

        dataApi.query(_) >>> [prices]

        def product = Mock(Record)
        product.getStringField("Id") >> Optional.of("01t7a00000AmE0IAAV");
        [org, product]
    }

    List<Record> buildPriceRecords() {
        Record record = Mock(Record)
        record.getStringField("Product2Id") >> Optional.of("01t7a00000AmE0IAAV")
        record.getDoubleField("UnitPrice") >> Optional.of(599.99d)
        [record]
    }
}
