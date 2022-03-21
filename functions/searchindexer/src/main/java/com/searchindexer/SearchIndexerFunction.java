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
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
                //"http://ec2-3-134-97-229.us-east-2.compute.amazonaws.com:8983/solr/commerce";
        Org org = context.getOrg().orElseThrow();

//        DataApi dataApi = new EnhancedDataApi(org.getDataApi()); doesn't work :( --> customer classloader blocks me
        DataApi dataApi = org.getDataApi();

        // TODO worry about batch size later - look at queryMore API
//        List<Record> productRecords = queryForProducts(dataApi, searchIndexerInput.getBatchSize());

        List<Record> productRecords = queryForProducts(dataApi);

        List<SolrInputDocument> inputDocuments = buildInputDocuments(searchIndexerInput.searchFields, dataApi, productRecords);

        SolrClient solrClient = getSolrClient(url);

        clearIndex(solrClient);

        indexDocuments(inputDocuments, solrClient);

        return new SearchIndexerOutput();
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

    private List<SolrInputDocument> buildInputDocuments(List<SearchIndexerInput.SearchField> searchFields, DataApi dataApi, List<Record> productRecords) {
        SolrDocumentBuilder solrDocumentBuilder = new SolrDocumentBuilder();
        return productRecords.parallelStream()
                .map(product -> solrDocumentBuilder.buildSolrDocument(searchFields, product))
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
