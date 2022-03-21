package com.searchindexer

import com.salesforce.functions.jvm.sdk.Context
import com.salesforce.functions.jvm.sdk.InvocationEvent
import com.salesforce.functions.jvm.sdk.Org
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult
import spock.lang.Specification

class SearchIndexerFunctionTest extends Specification {

    def "no products found"() {

        setup:

        def event = Mock(InvocationEvent<SearchIndexerInput>)
        def context = Mock(Context)
        setupMockData(context)

        SearchIndexerFunction function = new SearchIndexerFunction()
        SearchIndexerInput input = new SearchIndexerInput()
        input.serviceUrl = 'http://ec2-3-134-97-229.us-east-2.compute.amazonaws.com:8983/solr/commerce'
        input.searchFields = [new SearchIndexerInput.SearchField()]

        event.getData() >> input

        when: "calling function with no products to index"

        def output = function.apply(event, context)

        then: "error no products found"

        output
        !output.success
        output.numberOfProductsIndexed == 0
        output.errorMessages
        output.errorMessages.size() == 1
        output.errorMessages[0] == 'No products found'
    }

    private void setupMockData(Context context) {
        def results = Mock(RecordQueryResult)

        results.records >> []

        def org = DeepMock(Org)
        context.org >> Optional.of(org)
        context.org.get().dataApi.query(_) >> results
    }
}
