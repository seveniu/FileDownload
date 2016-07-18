package com.seveniu.fileDownloader;

import com.seveniu.common.str.StrUtil;
import com.seveniu.thriftServer.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by seveniu on 7/15/16.
 * DownloadResult
 */
public class DownloadResult {
    private Map<String,Result> resultList;

    public DownloadResult(int size) {
        resultList = new HashMap<>(size);
    }

    public synchronized void add(Result result) {
        if(result == null || StrUtil.isEmpty(result.getUrl() )) {
            return;
        }
        resultList.put(result.getUrl(),result);
    }

    public Map<String, Result> getResultList() {
        return resultList;
    }
}
