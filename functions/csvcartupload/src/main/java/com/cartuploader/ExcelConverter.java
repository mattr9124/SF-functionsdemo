package com.cartuploader;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelConverter implements Converter {
    @Override
    public List<CartUploadOutput.CartEntry> convert(InputStream inputStream) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

        XSSFSheet mainSheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = mainSheet.rowIterator();
        // skip header
        rowIterator.next();

        List<CartUploadOutput.CartEntry> entries = new ArrayList<>();
        rowIterator.forEachRemaining(row -> {
            String sku = row.getCell(0).getStringCellValue();
            int qty = (int) row.getCell(1).getNumericCellValue();

            entries.add(new CartUploadOutput.CartEntry(sku, qty));
        });

        return entries;
    }
}
