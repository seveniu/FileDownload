package com.seveniu.fileDownloader.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by seveniu on 5/24/16.
 * ImageFileSave
 */
@Component
public class FileFileStorage implements FileStorage {

    private Path storagePath = null;

    @Autowired
    public FileFileStorage(@Value("${fileDownloader.storagePath}") String storagePath) throws IOException {
        this.storagePath = this.getStoragePath(storagePath);
        System.out.println(this.storagePath);
        if (!Files.exists(this.storagePath)) {
            Files.createDirectory(this.storagePath);
        }
        if (Files.exists(this.storagePath) && !Files.isDirectory(this.storagePath)) {
            throw new IllegalArgumentException("error storage path ,is not dir");
        }
    }

    @Override
    public void save(byte[] bytes, String fileName) throws IOException {
        Path path = Paths.get(this.storagePath.toString(), fileName);
        Files.write(path, bytes);
    }

    private Path getStoragePath(String pathString) {
        Path path = Paths.get(pathString);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path).normalize();
        }
        return path;
    }

}
