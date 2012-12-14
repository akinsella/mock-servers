package org.apache.james;

import org.apache.james.filesystem.api.FileSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BasicFileSystem implements FileSystem {

    @Override
    public InputStream getResource(String url) throws IOException {
            return getClass().getResourceAsStream(url);
    }

    @Override
    public File getFile(String fileURL) throws FileNotFoundException {
            return new File(fileURL);
    }

    @Override
    public File getBasedir() throws FileNotFoundException {
            return new File(".");
    }

}