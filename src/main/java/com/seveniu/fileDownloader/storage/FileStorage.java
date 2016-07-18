package com.seveniu.fileDownloader.storage;

import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by seveniu on 5/24/16.
 * ImageSave
 */
@Component
public interface FileStorage {

    void save(byte[] bytes, String fileName) throws IOException;

}
