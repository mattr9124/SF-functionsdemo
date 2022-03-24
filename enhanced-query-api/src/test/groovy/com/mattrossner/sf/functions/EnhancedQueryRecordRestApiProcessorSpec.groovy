package com.mattrossner.sf.functions

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.salesforce.functions.jvm.runtime.sdk.RecordQueryResultImpl
import com.salesforce.functions.jvm.sdk.data.Record
import spock.lang.Specification;

class EnhancedQueryRecordRestApiProcessorSpec extends Specification {
    def "process normal record"() {
        setup: "create a processor"
        def processor = new EnhancedQueryRecordRestApiProcessor()

        and: "get a sample JSON response"
        def json = readJson '/prod-cat-ids-only.json'

        when: "Processing normal record"

        def queryResult = new RecordQueryResultImpl(processor.processResponse(200, [:], json))

        then: "Should be able to access all flat fields"

        queryResult
        queryResult.totalSize == 9

        and: "first sample is correct"

        Record first = queryResult.records.first()
        first.getStringField ('ProductId').isPresent()
        first.getStringField ('ProductId').get() == '01t7a00000AmE0IAAV'
        first.getStringField ('ProductCategoryId').isPresent()
        first.getStringField ('ProductCategoryId').get() == '0ZG7a0000000GeWGAU'
    }

    def "process records with lookups"() {
        setup: "create a processor"
        def processor = new EnhancedQueryRecordRestApiProcessor()

        and: "get a sample JSON response"
        def json = readJson '/prod-cat-with-name.json'

        when: "Processing normal record"

        def queryResult = new RecordQueryResultImpl(processor.processResponse(200, [:], json))

        then: "Should be able to access all flat fields and lookup fields"

        queryResult
        queryResult.totalSize == 9

        and: "first sample is correct"

        Record first = queryResult.records.first()
        first.getStringField ('ProductId').isPresent()
        first.getStringField ('ProductId').get() == '01t7a00000AmE0IAAV'
        first.getStringField ('ProductCategoryId').isPresent()
        first.getStringField ('ProductCategoryId').get() == '0ZG7a0000000GeWGAU'
        first.getStringField ('ProductCategory.Name').get() == 'Espresso Machines'
    }

    def readJson(String path) {
        def jsonString = new String(getClass().getResourceAsStream(path).readAllBytes())
        def rootEle = new Gson().fromJson(jsonString, JsonElement.class)
        rootEle.getAsJsonObject().get('result')
    }

}
