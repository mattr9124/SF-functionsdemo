package com.searchindexer;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.Org;
import com.salesforce.functions.jvm.sdk.SalesforceFunction;
import com.salesforce.functions.jvm.sdk.data.DataApi;
import com.salesforce.functions.jvm.sdk.data.Record;
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;
import com.salesforce.functions.jvm.sdk.data.error.DataApiException;
import com.searchindexer.indexer.SolrDocumentBuilder;
import com.searchindexer.indexer.valueprovider.ValueProviderRegistryImpl;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Describe SearchIndexerFunction here.
 */
public class SearchIndexerFunction implements SalesforceFunction<SearchIndexerInput, SearchIndexerOutput> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchIndexerFunction.class);


    @Override
    public SearchIndexerOutput apply(InvocationEvent<SearchIndexerInput> event, Context context)
            throws Exception {

        SearchIndexerInput searchIndexerInput = event.getData();

        String url = searchIndexerInput.serviceUrl;

        if (url == null || url.isBlank()) {
            return fail("No URL found!");
        }

        LOGGER.info("Using Solr instance URL {}", url);

        List<SearchIndexerInput.SearchField> searchFields = searchIndexerInput.searchFields;

        if (searchFields == null || searchFields.isEmpty()) {
            return fail("No search fields found");
        }

        Org org = context.getOrg().orElseThrow();

        DataApi dataApi = org.getDataApi();

        List<Record> productRecords = queryForProducts(dataApi);
        if (productRecords.isEmpty()) {
            return fail("No products found");
        }

        LOGGER.info("Found {} products to index", productRecords.size());

        LOGGER.info("Building input documents");
        List<SolrInputDocument> inputDocuments = buildInputDocuments(searchIndexerInput, org, productRecords);

        SolrClient solrClient = getSolrClient(url);

        LOGGER.info("Clearing previous index");
        clearIndex(solrClient);

        LOGGER.info("Indexing new documents");
        indexDocuments(inputDocuments, solrClient);

        LOGGER.info("Indexing complete");

        SearchIndexerOutput searchIndexerOutput = new SearchIndexerOutput();

        searchIndexerOutput.numberOfProductsIndexed = productRecords.size();
        searchIndexerOutput.success = true;

        return searchIndexerOutput;
    }

    private SearchIndexerOutput fail(String errorMessage) {
        SearchIndexerOutput searchIndexerOutput = new SearchIndexerOutput();
        searchIndexerOutput.numberOfProductsIndexed = 0;
        searchIndexerOutput.success = false;
        searchIndexerOutput.errorMessages = Collections.singletonList(errorMessage);
        return searchIndexerOutput;
    }

    private void indexDocuments(List<SolrInputDocument> inputDocuments, SolrClient solrClient) throws SolrServerException, IOException {
        solrClient.add(inputDocuments);
        solrClient.commit();
    }

    private void clearIndex(SolrClient solrClient) throws SolrServerException, IOException {
        solrClient.deleteByQuery("*:*");
    }

    private SolrClient getSolrClient(String url) {
        return new HttpSolrClient.Builder(url)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }

    private List<SolrInputDocument> buildInputDocuments(SearchIndexerInput searchIndexerInput, Org org, List<Record> productRecords) {
        SolrDocumentBuilder solrDocumentBuilder = new SolrDocumentBuilder(new ValueProviderRegistryImpl());
        return productRecords.parallelStream()
                .map(product -> solrDocumentBuilder.buildSolrDocument(org, searchIndexerInput, product))
                .toList();
    }

    private List<Record> queryForProducts(DataApi dataApi) throws DataApiException, IOException {
        String productQuery = getQuery(); // TODO see if we can make the query configurable
        RecordQueryResult productResult = dataApi.query(productQuery);
        return productResult.getRecords();
    }

    private String getQuery() throws IOException {
        return new String(getClass().getResourceAsStream("/product-query.soql").readAllBytes());
    }
}
