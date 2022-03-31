package com.cartuploader;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.SalesforceFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CsvCartUploadFunctionTest {

    @DisplayName("Test CSV Parsing Function")
    @Test
    public void testParseCsv() throws Exception {
        CartUploadInput input = new CartUploadInput();
        input.base64EncodedData = getBase64CsvData("/csvsamplefile.csv");
        input.contentType = "text/csv";

        runTest(input);

    }

    @DisplayName("Test Excel Parsing Function")
    @Test
    public void testParseExcel() throws Exception {
        CartUploadInput input = new CartUploadInput();

        input.base64EncodedData = getBase64CsvData("/uploadsample.xlsx");
        input.contentType = "application/vnd.excel";

        runTest(input);

    }

    private void runTest(CartUploadInput input) throws Exception {
        InvocationEvent<CartUploadInput> event = Mockito.mock(InvocationEvent.class);
        Context context = Mockito.mock(Context.class);

        Mockito.when(event.getData()).thenReturn(input);

        SalesforceFunction<CartUploadInput, CartUploadOutput> function = new CsvCartUploadFunction();

        CartUploadOutput functionOutput = function.apply(event, context);

        assertNotNull(functionOutput);

        List<CartUploadOutput.CartEntry> cartEntries = functionOutput.cartEntries;

        assertNotNull(cartEntries);

        assertEquals(3, cartEntries.size());

        Map<String, Integer> sampleData = Map.of(
                "abc", 4,
                "def", 3,
                "ghi", 2
        );

        cartEntries.forEach(cartEntry -> {
            assertTrue(sampleData.containsKey(cartEntry.sku), "Missing SKU %s".formatted(cartEntry.sku));
            assertEquals(sampleData.get(cartEntry.sku), cartEntry.quantity, "Incorrect quantity for %s".formatted(cartEntry.sku));
        });
    }

    private String getBase64CsvData(String inputFile) throws IOException {
        byte [] data = getClass().getResourceAsStream(inputFile).readAllBytes();
        return Base64.getEncoder().encodeToString(data);
    }
}
