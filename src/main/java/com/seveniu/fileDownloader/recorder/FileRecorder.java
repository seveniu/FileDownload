package com.seveniu.fileDownloader.recorder;

import com.seveniu.thriftServer.Result;

/**
 * Created by seveniu on 5/24/16.
 * ImageRecode
 */
public interface FileRecorder {

    void record(String url, String imageOriginName, String extension, String md5, String storageName);

    Result getRecorder(String url);
}
