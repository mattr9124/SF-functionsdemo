package com.cartuploader;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.SalesforceFunction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Describe CsvCartUploadFunction here.
 */
public class CsvCartUploadFunction implements SalesforceFunction<CartUploadInput, CartUploadOutput> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvCartUploadFunction.class);

    @Override
    public CartUploadOutput apply(InvocationEvent<CartUploadInput> event, Context context)
            throws Exception {

        byte[] data = extractDataFromEvent(event);

        String dataAsString = new String(data);

        CSVParser csvRecords = CSVParser.parse(dataAsString, CSVFormat.Builder
                .create()
                .setHeader("sku", "qty")
                .setSkipHeaderRecord(true)
                .setDelimiter(';')
                .build());

        List<CartUploadOutput.CartEntry> cartEntries = csvRecords.getRecords().stream()
                .map(record -> new CartUploadOutput.CartEntry(
                        record.get("sku"),
                        Integer.parseInt(record.get("qty")))
                ).toList();

        return new CartUploadOutput(cartEntries);
    }

    private byte[] extractDataFromEvent(InvocationEvent<CartUploadInput> event) {
        return Base64.getDecoder().decode(event.getData().base64EncodedData);
    }
}
