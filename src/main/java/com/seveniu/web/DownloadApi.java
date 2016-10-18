package com.seveniu.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.seveniu.common.json.Json;
import com.seveniu.fileDownloader.FileDownloadManager;
import com.seveniu.thriftServer.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by seveniu on 7/16/16.
 */
@Controller
@RequestMapping("/api/download")
public class DownloadApi {

    @Autowired
    FileDownloadManager fileDownloadManager;

    @RequestMapping(value = "/", method = RequestMethod.POST, produces = "text/json;charset=UTF-8")
    @ResponseBody
    public String getAll(String urls) {
        List<String> urlList = Json.toObject(urls, new TypeReference<List<String>>() {
        });
        try {
            Map<String, Result> results = fileDownloadManager.download(urlList);
            return Json.toJson(results);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "[]";
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/json;charset=UTF-8")
    @ResponseBody
    public String get(String url) {
        try {
            Map<String, Result> results = fileDownloadManager.download(Collections.singletonList(url));
            return Json.toJson(results);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "[]";
        }
    }
}
