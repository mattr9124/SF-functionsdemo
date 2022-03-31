package com.cartuploader;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.SalesforceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

/**
 * Describe CsvCartUploadFunction here.
 */
public class CsvCartUploadFunction implements SalesforceFunction<CartUploadInput, CartUploadOutput> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvCartUploadFunction.class);

    @Override
    public CartUploadOutput apply(InvocationEvent<CartUploadInput> event, Context context)
            throws Exception {

        CartUploadInput cartUploadInput = event.getData();

        Converter converter = getConverterForContentType(cartUploadInput.contentType);

        InputStream inputStream = extractDataFromEvent(event);

        List<CartUploadOutput.CartEntry> cartEntries = converter.convert(inputStream);

        return new CartUploadOutput(cartEntries);
    }

    private Converter getConverterForContentType(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type cannot be null");
        }

        if (contentType.contains("text/csv")) {
            return new CsvConverter();
        } else if (contentType.contains("excel")) {
            return new ExcelConverter();
        }

        throw new RuntimeException("Cannot find appropriate converter for content type %s".formatted(contentType));
    }

    private InputStream extractDataFromEvent(InvocationEvent<CartUploadInput> event) {
        return new ByteArrayInputStream(Base64.getDecoder().decode(event.getData().base64EncodedData));
    }
}
