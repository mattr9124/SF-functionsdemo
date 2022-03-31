package com.cartuploader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface Converter {

    List<CartUploadOutput.CartEntry> convert(InputStream inputStream) throws IOException;

}
