package com.fileuploader;

import java.util.List;

public class UploadOutput {
    final List<CartEntry> cartEntries;

    public UploadOutput(List<CartEntry> cartEntries) {
        this.cartEntries = cartEntries;
    }

    static class CartEntry {
        final String sku;
        final Integer quantity;

        public CartEntry(String sku, Integer quantity) {
            this.sku = sku;
            this.quantity = quantity;
        }
    }
}
