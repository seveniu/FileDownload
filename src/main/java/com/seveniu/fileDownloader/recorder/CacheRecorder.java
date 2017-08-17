package com.seveniu.fileDownloader.recorder;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.seveniu.fileDownloader.Result;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

/**
 * Created by seveniu on 7/16/16.
 * CacheRecorder
 */
@Component
public class CacheRecorder implements FileRecorder {

    private LoadingCache<String, Result> urlCache = CacheBuilder.newBuilder()
            .maximumSize(100000)
            .build(new CacheLoader<String, Result>() {
                public Result load(String key) {
                    return new Result();
                }
            });

    @Override
    public void record(String url, String originName, String extension, String md5, String storageName) {
        urlCache.put(url, new Result(url,storageName,originName,extension));
    }

    @Override
    public Result getRecorder(String url) {
        try {
            Result result =  urlCache.get(url);
            if(result.getUrl() == null) {
                return null;
            } else {
                return result;
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

}
