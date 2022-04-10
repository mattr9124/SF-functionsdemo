package com.fileuploader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface Converter {

    List<UploadOutput.CartEntry> convert(InputStream inputStream) throws IOException;

}
