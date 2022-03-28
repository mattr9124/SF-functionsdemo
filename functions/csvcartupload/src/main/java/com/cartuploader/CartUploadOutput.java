package com.cartuploader;

import java.util.List;

public class CartUploadOutput {

  final List<CartEntry> cartEntries;

  public CartUploadOutput(List<CartEntry> cartEntries) {
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
