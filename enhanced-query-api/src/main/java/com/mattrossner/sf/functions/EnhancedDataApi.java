package com.mattrossner.sf.functions;

import com.salesforce.functions.jvm.runtime.sdk.EmptyRecordQueryResultImpl;
import com.salesforce.functions.jvm.runtime.sdk.RecordQueryResultImpl;
import com.salesforce.functions.jvm.runtime.sdk.restapi.*;
import com.salesforce.functions.jvm.sdk.data.*;
import com.salesforce.functions.jvm.sdk.data.Record;
import com.salesforce.functions.jvm.sdk.data.builder.RecordBuilder;
import com.salesforce.functions.jvm.sdk.data.builder.UnitOfWorkBuilder;
import com.salesforce.functions.jvm.sdk.data.error.DataApiException;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class EnhancedDataApi implements DataApi {

    final private DataApi delegate;
    private final Object restApi;
    private final Method restApiExecute;
    private final Class dataApiImpClass;
    private final Method exceptionMapMethod;

    public EnhancedDataApi(DataApi delegate) {
        this.delegate = delegate;
        try {
            Field restApiField = delegate.getClass().getDeclaredField("restApi");
            restApiField.setAccessible(true);
            restApi = restApiField.get(delegate);
            restApiExecute = restApi.getClass().getDeclaredMethod("execute", RestApiRequest.class);

            dataApiImpClass = Class.forName("com.salesforce.functions.jvm.runtime.sdk.DataApiImpl");
            exceptionMapMethod = dataApiImpClass.getDeclaredMethod("mapException", RestApiErrorsException.class);
            exceptionMapMethod.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Unable to find restAPI on delegate data API. This was temporary/experimental anyway!");
        }
    }

    @Override
    @Nonnull
    public RecordQueryResult query(String soql) throws DataApiException {
        RestApiRequest<QueryRecordResult> request = new EnhancedQueryRecordRestApiProcessor(new QueryRecordRestApiRequest(soql));
        return new RecordQueryResultImpl(executeRequest(request));
    }

    @Override
    @Nonnull
    public RecordQueryResult queryMore(RecordQueryResult queryResult) throws DataApiException {
        RecordQueryResultImpl impl = (RecordQueryResultImpl) queryResult;

        if (impl.getNextRecordsPath().isPresent()) {
            return new RecordQueryResultImpl(
                    executeRequest(new EnhancedQueryRecordRestApiProcessor(new QueryNextRecordsRestApiRequest(impl.getNextRecordsPath().get()))));
        }

        return new EmptyRecordQueryResultImpl(impl.getQueryRecordResult());
    }

    @Override
    public RecordModificationResult create(Record record) throws DataApiException {
        return delegate.create(record);
    }

    @Override
    public RecordModificationResult update(Record record) throws DataApiException {
        return delegate.update(record);
    }

    @Override
    public RecordModificationResult delete(String type, String id) throws DataApiException {
        return delegate.delete(type, id);
    }

    @Override
    public RecordBuilder newRecordBuilder(String type) {
        return delegate.newRecordBuilder(type);
    }

    @Override
    public RecordBuilder newRecordBuilder(Record record) {
        return delegate.newRecordBuilder(record);
    }

    @Override
    public UnitOfWorkBuilder newUnitOfWorkBuilder() {
        return delegate.newUnitOfWorkBuilder();
    }

    @Override
    public Map<ReferenceId, RecordModificationResult> commitUnitOfWork(UnitOfWork unitOfWork) throws DataApiException {
        return delegate.commitUnitOfWork(unitOfWork);
    }

    @Override
    public String getAccessToken() {
        return delegate.getAccessToken();
    }

    private QueryRecordResult executeRequest(RestApiRequest<QueryRecordResult> request) throws DataApiException {
        try {
            return (QueryRecordResult) restApiExecute.invoke(restApi, request);
        } catch (Exception e) {
            if (e instanceof RestApiErrorsException) {
                try {
                    throw (DataApiException) exceptionMapMethod.invoke(null, e);
                } catch (Exception re) {
                    e.printStackTrace();
                }
            }
            throw new DataApiException("Error occurred during REST API callout: " + e.getMessage(), e);
        }
    }
}
