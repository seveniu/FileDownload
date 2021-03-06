package com.seveniu.fileDownloader;

import com.seveniu.fileDownloader.filter.FileFilter;
import com.seveniu.fileDownloader.recorder.FileRecorder;
import com.seveniu.fileDownloader.storage.FileStorage;
import com.seveniu.util.UserAgent;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.tomcat.util.http.fileupload.ParameterParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by seveniu on 7/14/16.
 * FileDownloadJob
 */
public class FileDownloadJob implements Runnable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Logger downloadErrorLogger = LoggerFactory.getLogger("download-error");
    private static final RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10 * 1000).build();

    private String url;
    private FileDownloadProcess fileDownloadProcess;
    private CloseableHttpClient httpClient;
    private FileFilter fileFilter;
    private FileStorage fileStorage;
    private FileRecorder fileRecorder;
    private DownloadResult resultList;
    private static AtomicInteger downloadCounter = new AtomicInteger();

    public FileDownloadJob(String url, FileFilter fileFilter, FileStorage fileStorage,
                           FileRecorder fileRecorder, CloseableHttpClient httpClient, DownloadResult resultList) {

        this.url = url;
        this.fileDownloadProcess = new FileDownloadProcess(fileFilter, fileStorage, fileRecorder);
        this.fileFilter = fileFilter;
        this.fileStorage = fileStorage;
        this.fileRecorder = fileRecorder;
        this.resultList = resultList;
        this.httpClient = httpClient;
    }

    @Override
    public void run() {
        if (fileFilter.contain(url)) {
            Result result = fileRecorder.getRecorder(url);
            if (result != null) {
                logger.info("url exist : {}", url);
                resultList.add(result);
                return;
            }
        }
        long startTime = System.currentTimeMillis();
//        logger.info("start download url : {}", url);
        Result result = download(url, fileDownloadProcess);
        if (result == null) {
            return;
        }
        logger.debug("file download done, cost : {}ms ----  url : - {} name : {}", System.currentTimeMillis() - startTime, url, result.getStorageName());
        resultList.add(result);
    }

    private static String getReferer(String url) {
        try {
            URL url1 = new URL(url);
            return url1.getProtocol() + "://" + url1.getHost();
        } catch (MalformedURLException e) {
            return url;
        }
    }


    public Result download(String url, FileDownloadProcess fileDownloadProcess) {
        CloseableHttpResponse responseGet;
        HttpGet httpGet = null;

        InputStream inputStream = null;
        try {
            // 以get方法执行请求
            if (!url.startsWith("http")) {
                return null;
            }
            httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            httpGet.addHeader("User-Agent", UserAgent.getUserAgent());
            httpGet.addHeader("referer", getReferer(url));

            // 获得服务器响应的所有信息
            responseGet = httpClient.execute(httpGet);

            if (responseGet.getStatusLine().getStatusCode() != 200) {
                return null;
            }
            Header cd = responseGet.getLastHeader("Content-Disposition");
            String fileName = null;
            if (cd != null) {
                fileName = getFileName(cd.getValue());
            }
            Header contentTypeHeader = responseGet.getLastHeader("Content-Type");
            String contentType = null;
            if (contentTypeHeader != null) {
                contentType = getFileType(contentTypeHeader.getValue());

            }
            HttpEntity entity = responseGet.getEntity();
            inputStream = entity.getContent();
            byte[] bytes = IOUtils.toByteArray(inputStream);
            downloadCounter.incrementAndGet();
            if (downloadCounter.get() % 100 == 0) {
                logger.info("download count : {}", downloadCounter.get());
            }
            return fileDownloadProcess.process(url, fileName, contentType, bytes);
        } catch (Exception e) {
            //TODO:下载 错误 时,记录
            logger.warn("file download error url: {} , error : {}", url, e);
            downloadErrorLogger.error("file download error url:{}, error : {}", url, e);
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
    }


    private String getFileName(String pContentDisposition) {
        String fileName = null;
        if (pContentDisposition != null) {
            String cdl = pContentDisposition.toLowerCase();
            if (cdl.startsWith("form_data") || cdl.startsWith("attachment")) {
                ParameterParser parser = new ParameterParser();
                parser.setLowerCaseNames(true);
                // Parameter parser can handle null input
                Map params = parser.parse(pContentDisposition, ';');
                if (params.containsKey("filename")) {
                    fileName = (String) params.get("filename");
                    if (fileName != null) {
                        fileName = fileName.trim();
                    } else {
                        // Even if there is no value, the parameter is present,
                        // so we return an empty file name rather than no file
                        // name.
                        fileName = "";
                    }
                }
            }
        }
        return fileName;
    }

    private String getFileType(String fileType) {
        if (fileType != null && fileType.length() > 0) {
            return fileType.split("/")[1];
        }

        return null;
    }

}
