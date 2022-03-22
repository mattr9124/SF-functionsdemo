package com.searchindexer.valueprovider

import com.salesforce.functions.jvm.sdk.Org
import com.salesforce.functions.jvm.sdk.data.DataApi
import com.salesforce.functions.jvm.sdk.data.Record
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult
import com.searchindexer.SearchIndexerInput
import com.searchindexer.indexer.valueprovider.BuyerGroupValueProvider
import com.searchindexer.indexer.valueprovider.ValueProvider
import spock.lang.Specification

class BuyerGroupValueProviderTest extends Specification {

    def "get buyer groups for product"() {
        setup: "create some mocks"

        def (Org org, Record product) = setupMocks()

        ValueProvider<List<String>> valueProvider = new BuyerGroupValueProvider(org, new SearchIndexerInput())

        when:

        def result = valueProvider.getValue product
        println result

        then:

        result
        result.size() == 2
        result.contains('0ZI7a0000004ERKGA2')
        result.contains('0ZI7a0000004ESwGAM')

    }

    private List setupMocks() {
        def org = Mock(Org)
        def dataApi = Mock(DataApi)

        org.getDataApi() >> dataApi

        def comEntProduct = Mock(RecordQueryResult)
        def policyIds = ['1Ce7a00000000mECAQ', '1Ce7a00000000nlCAA'].collect {buildSimpleRecord 'PolicyId', it }
        comEntProduct.records >> policyIds

        def comEntBg = Mock(RecordQueryResult)
        def bgIds = ['0ZI7a0000004ERKGA2', '0ZI7a0000004ESwGAM'].collect {buildSimpleRecord 'BuyerGroupId', it }
        comEntBg.records >> bgIds

        dataApi.query(_) >>> [comEntProduct, comEntBg]

        def product = Mock(Record)
        product.getStringField("Id") >> Optional.of("01t7a00000AmE0GAAV");
        [org, product]
    }

    Record buildSimpleRecord(def field, def id) {
        Record record = Mock(Record)
        record.getStringField(field) >> Optional.of(id)
        record
    }

}
