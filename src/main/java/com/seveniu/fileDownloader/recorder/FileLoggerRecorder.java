package com.seveniu.fileDownloader.recorder;

import com.seveniu.fileDownloader.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by seveniu on 5/24/16.
 * ImageMysqlRecorder
 */
@Component
public class FileLoggerRecorder implements FileRecorder {
    private Logger logger = LoggerFactory.getLogger("IMAGE_RECORD");

    @Override
    public void record(String url, String imageOriginName, String extension, String md5, String storage) {
        logger.info("url:{},originName:{},extension:{},md5:{}");
    }

    @Override
    public Result getRecorder(String url) {
        return null;
    }
}
