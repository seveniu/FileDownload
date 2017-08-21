package com.seveniu.fileDownloader;

import com.seveniu.fileDownloader.filter.FileFilter;
import com.seveniu.fileDownloader.recorder.FileRecorder;
import com.seveniu.fileDownloader.storage.FileStorage;
import com.seveniu.util.AppContext;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by seveniu
 * on 5/25/16.
 * ImageDownloader
 */
@Component
public class FileDownloadManager {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private int threadNum;
    private CloseableHttpClient httpClient;

    private ThreadPoolExecutor executor;
    //    @Autowired
//    @Qualifier("${fileDownloader.filter}")
    @Resource(name = "${fileDownloader.filter}")
    private FileFilter fileFilter;
    @Autowired
    private FileStorage fileStorage;
    @Resource(name = "${fileDownloader.recorder}")
    private FileRecorder fileRecorder;
    private int httpClientThreadNum;

    @Autowired
    public FileDownloadManager(@Value("${fileDownloader.threadNum}") int threadNum) {
        this.threadNum = threadNum;
        this.httpClientThreadNum = threadNum * 2;
        logger.info("thread num : {}, download thread num : {}", threadNum, httpClientThreadNum);

        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNum, new ThreadFactory() {
            AtomicInteger count = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "file-download-thread-" + count.getAndIncrement());
            }
        });
        this.httpClient = getHttpClient();
    }

    public Map<String, Result> download(List<String> urls) throws InterruptedException {
        if (urls == null || urls.size() == 0) {
            return Collections.emptyMap();
        }
        DownloadResult result = new DownloadResult(urls.size());
        FileDownloadJob[] fileDownloadJobs = new FileDownloadJob[urls.size()];
        for (int i = 0; i < fileDownloadJobs.length; i++) {
            fileDownloadJobs[i] = new FileDownloadJob(urls.get(i), fileFilter, fileStorage, fileRecorder, httpClient, result);
        }

        CountDownExecutor countDownExecutor = new CountDownExecutor(executor, fileDownloadJobs);
        countDownExecutor.await(urls.size() * 10, TimeUnit.SECONDS);
        return result.getResultList();
    }

    private CloseableHttpClient getHttpClient() {

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(5, TimeUnit.MINUTES);
        cm.setMaxTotal(httpClientThreadNum);
        cm.setDefaultMaxPerRoute(40);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(4 * 1000) // 建立连接的时间
                .setConnectionRequestTimeout(10 * 1000) // 等待数据的时间，在建立连接之后
                .setSocketTimeout(10 * 1000).build(); // 从连接池中取连接等待的时间
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(cm).build();

    }

    public static FileDownloadManager get() {
        return AppContext.getBean(FileDownloadManager.class);
    }
}
