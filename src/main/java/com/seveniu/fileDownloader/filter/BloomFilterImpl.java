package com.seveniu.fileDownloader.filter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import com.seveniu.def.SystemError;
import com.seveniu.util.ShutdownHook;
import com.seveniu.util.ShutdownHookManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by seveniu on 5/24/16.
 * BloomFilterImpl
 */
@Component("bloomFilter")
public class BloomFilterImpl implements FileFilter, ShutdownHook {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private BloomFilter<CharSequence> images;
    private AtomicInteger count = new AtomicInteger();
    private String serializablePath;
    private static final int SERIALIZABLE_THRESHOLD = 2000;

    @Autowired
    public BloomFilterImpl(@Value("${fileDownloader.bloomPath}") String serializablePath) {
        this.serializablePath = serializablePath;
        ShutdownHookManager.get().register(this);
        deserialization();
    }


    @Override
    public boolean containUrl(String url) {
        return images.mightContain(url);
    }

    @Override
    public void put(String url, String md5) {
        images.put(md5);
        images.put(url);
        count.incrementAndGet();
        if (count.get() > SERIALIZABLE_THRESHOLD) {
            count.set(0);
            serializableSync();
        }
    }

    @Override
    public boolean contain(String md5) {
        return images.mightContain(md5);
    }

    private void deserialization() {

        Funnel<CharSequence> funnel = Funnels.stringFunnel(Charset.forName("UTF-8"));
        try {
            File file = new File(serializablePath);
            if (file.exists()) {
                images = BloomFilter.readFrom(new FileInputStream(serializablePath), funnel);
            } else {
                images = BloomFilter.create(funnel, 10000000, 0.0001);
            }
        } catch (IOException e) {
            e.printStackTrace();
            SystemError.shutdown(SystemError.IMAGE_BLOOMFILTER_DESERIALIZATION_ERROR);
        }
    }

    private void serializable() {
        try {
            File file = new File(serializablePath);
            if (!file.exists()) {
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
            }
            images.writeTo(new FileOutputStream(serializablePath));
            logger.info("bloom filter serialize");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AtomicBoolean inSerialize = new AtomicBoolean();

    private void serializableSync() {
        if (inSerialize.getAndSet(true)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                serializable();
                inSerialize.set(false);
            }
        }, "image-serializable").start();
    }

    @Override
    public void shutdown() {
    }

}
