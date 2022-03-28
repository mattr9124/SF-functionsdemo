package com.cartuploader;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.SalesforceFunction;
import com.salesforce.functions.jvm.sdk.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Describe ExampleFunction here.
 */
public class ExampleFunction implements SalesforceFunction<FunctionInput, FunctionOutput> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleFunction.class);

    private static final String NO_ORG_ID = "000000000000000000";
    private static final String ERR_NO_ORG = "No Org specified";

    @Override
    public FunctionOutput apply(InvocationEvent<FunctionInput> event, Context context)
            throws Exception {

        if (!context.getOrg().isPresent()) {
            LOGGER.error(ERR_NO_ORG);
            return new FunctionOutput(Collections.emptyList(), ERR_NO_ORG);
        }

        if (NO_ORG_ID.equals(context.getOrg().get().getId())) {
            LOGGER.error(ERR_NO_ORG);
            return new FunctionOutput(Collections.emptyList(), ERR_NO_ORG);
        }

        List<Record> records =
                context.getOrg().get().getDataApi().query("SELECT Id, Name FROM Account").getRecords();

        LOGGER.info("Function successfully queried {} account records!", records.size());

        List<Account> accounts = records.stream()
                .map(record -> makeAccount(record))
                .collect(Collectors.toList());

        return new FunctionOutput(accounts);
    }

    private Account makeAccount(Record record) {
        return new Account(record.getStringField("Id").get(),
                record.getStringField("Name").get());
    }
}
