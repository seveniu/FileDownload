package com.seveniu.fileDownloader.filter;

import org.springframework.stereotype.Component;

/**
 * Created by seveniu on 5/24/16.
 * ImageRepeatCheck
 */
@Component
public interface FileFilter {


    boolean containUrl(String url);

    void put(String url, String md5);

    boolean contain(String md5);
}
