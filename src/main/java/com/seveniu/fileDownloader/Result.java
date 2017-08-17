package com.seveniu.fileDownloader;

/**
 * Created by seveniu on 8/17/17.
 * *
 */
public class Result {
    private String url; // required
    private String storageName; // required
    private String orginName; // required
    private String extension; // required

    public Result() {

    }

    public Result(String url, String storageName, String orginName, String extension) {
        this.url = url;
        this.storageName = storageName;
        this.orginName = orginName;
        this.extension = extension;
    }

    public String getUrl() {
        return this.url;
    }


    public String getStorageName() {
        return this.storageName;
    }

    public String getOrginName() {
        return this.orginName;
    }

}

