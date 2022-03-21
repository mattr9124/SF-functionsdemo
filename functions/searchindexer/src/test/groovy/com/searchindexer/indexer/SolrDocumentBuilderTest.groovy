package com.searchindexer.indexer

import com.salesforce.functions.jvm.sdk.data.DataApi
import com.salesforce.functions.jvm.sdk.data.Record
import com.searchindexer.SearchIndexerInput
import com.searchindexer.indexer.valueprovider.ValueProvider
import com.searchindexer.indexer.valueprovider.ValueProviderRegistry
import spock.lang.Specification

class SolrDocumentBuilderTest extends Specification {

    def "build input document"() {

        def valueProviderRegistry = createMockValueProviderReg()

        def builder = new SolrDocumentBuilder(valueProviderRegistry)
        def searchFields = getSearchFields()
        def product = buildProductMock()
        def dataApi = Mock(DataApi)

        when: "building a Solr document"

        def document = builder.buildSolrDocument dataApi, searchFields, product

        then: "Should receive correct document ready for indexing"

        document

        document.values().size() == 7
        document.containsKey('id')
        document.containsKey('Name_s')
        document.containsKey('ProductCode_s')
        document.containsKey('Description')
        document.containsKey('Ranking_d')
        document.containsKey('Categories')
        document.containsKey('BuyerGroups')
    }

    def buildProductMock() {
        def product = Mock(Record)

        product.getStringField('Id') >> Optional.of('01t7a00000AmE0GAAV')

        product.getStringField('Name') >> Optional.of('Capricorn I Group Espresso Machine')
        product.getStringField('ProductCode') >> Optional.of('ID-PEM')
        product.getStringField('Description') >> Optional.of('The Capricorn I group espresso machine is the perfect addition to your restaurant, coffee shop, or cafe! Not only does this machine give you the opportunity to add one-of-a kind hot beverages to your menu, but its stainless steel dual boiler system and automatic functionality ensure fast, efficient service for your customers. A user-friendly option for any high-volume establishment, this espresso machine is sure to take your customer\'s morning fix to the next level.')
        product.getDoubleField('Ranking') >> Optional.of(5)

        product.getStringField(_) >> Optional.empty()
        product.getDoubleField(_) >> Optional.empty()
        product.getLongField(_) >> Optional.empty()
        product
    }

    def getSearchFields() {
        def name = new SearchIndexerInput.SearchField()
        name.type = SearchIndexerInput.FieldType.String
        name.multiValue = false
        name.name = 'Name'

        def sku = new SearchIndexerInput.SearchField()
        sku.type = SearchIndexerInput.FieldType.String
        sku.multiValue = false
        sku.name = 'ProductCode'
        
        def description = new SearchIndexerInput.SearchField()
        description.type = SearchIndexerInput.FieldType.Text
        description.multiValue = false
        description.name = 'Description'

        def ranking = new SearchIndexerInput.SearchField()
        ranking.type = SearchIndexerInput.FieldType.Number
        ranking.multiValue = false
        ranking.name = 'Ranking'

        def categories = new SearchIndexerInput.SearchField()
        categories.type = SearchIndexerInput.FieldType.String
        categories.multiValue = true
        categories.name = 'Categories'
        categories.valueProvider = 'com.searchindexer.indexer.valueprovider.CategoryValueProvider'

        def buyerGroups = new SearchIndexerInput.SearchField()
        buyerGroups.type = SearchIndexerInput.FieldType.String
        buyerGroups.multiValue = true
        buyerGroups.name = 'BuyerGroups'
        buyerGroups.valueProvider = 'com.searchindexer.indexer.valueprovider.BuyerGroupValueProvider'

        [name, sku, description, ranking, categories, buyerGroups]
    }

    def createMockValueProviderReg() {
        def valueProviderRegistry = Mock(ValueProviderRegistry)

        def categoryProvider = Mock(ValueProvider)
        categoryProvider.getValue(_, _) >> ['Espresso Machines', 'Machines', 'Products']

        def bgProvider = Mock(ValueProvider)
        bgProvider.getValue(_, _) >> ['0ZI7a0000004ERKGA2', '0ZI7a0000004ESwGAM']

        valueProviderRegistry.getValueProvider('com.searchindexer.indexer.valueprovider.CategoryValueProvider') >> categoryProvider
        valueProviderRegistry.getValueProvider('com.searchindexer.indexer.valueprovider.BuyerGroupValueProvider') >> bgProvider

        valueProviderRegistry
    }

}
