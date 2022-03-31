package com.cartuploader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CsvConverter implements Converter {
    @Override
    public List<CartUploadOutput.CartEntry> convert(InputStream inputStream) throws IOException {
        String dataAsString = new String(inputStream.readAllBytes());

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
        return cartEntries;
    }
}
