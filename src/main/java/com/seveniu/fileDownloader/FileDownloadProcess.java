package com.seveniu.fileDownloader;

import com.seveniu.common.str.StrUtil;
import com.seveniu.fileDownloader.filter.FileFilter;
import com.seveniu.fileDownloader.recorder.FileRecorder;
import com.seveniu.fileDownloader.storage.FileStorage;
import com.seveniu.thriftServer.Result;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by seveniu on 7/14/16.
 * FileDownloadProcess
 */
public class FileDownloadProcess {
    private FileFilter fileFilter;
    private FileStorage fileStorage;
    private FileRecorder fileRecorder;

    public FileDownloadProcess(FileFilter fileFilter, FileStorage fileStorage, FileRecorder fileRecorder) {
        this.fileFilter = fileFilter;
        this.fileStorage = fileStorage;
        this.fileRecorder = fileRecorder;
    }

    private static ThreadLocal<MessageDigest> threadLocal = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        }
    };

    /**
     * 返回 md5.extension
     */
    public Result process(String url, String fileName, String fileType, byte[] bytes) {
        //文件名
        String originName;
        // 扩展名
        String extension;
        if (StrUtil.isNotEmpty(fileName)) {
            originName = fileName.substring(0, fileName.lastIndexOf("."));
            extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            originName = getName(url);
            extension = getExtension(url);
        }
        if (fileType != null && fileType.length() > 0) {
            extension = fileType;
        }

        String md5 = md5(bytes);

        String storageName = md5 + "." + extension;
        // 去重
        if (fileFilter.contain(md5)) {
            fileRecorder.record(url, originName, extension, md5, storageName);
        } else {
            // 保存
            try {
                fileStorage.save(bytes, storageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileFilter.put(url, md5);
            // 记录
            fileRecorder.record(url, originName, extension, md5, storageName);
        }
        return new Result(url, storageName, originName, extension);
    }


    protected String getExtension(String url) {
        String temp = url.substring(url.lastIndexOf(".") + 1).trim();
        Pattern pattern = Pattern.compile("(\\w+)");
        Matcher m = pattern.matcher(temp);
        if (m.find()) {
            return m.group(1);
        }
        return "jpeg";
    }

    protected String md5(byte[] bytes) {
        MessageDigest messagedigest = threadLocal.get();
        if (messagedigest == null) {
            return null;
        }
        return new String(Hex.encodeHex(messagedigest.digest(bytes)));
    }

    public String getName(String url) {
        String temp = url.substring(url.lastIndexOf("/") + 1).trim();
        int index = temp.lastIndexOf(".");
        if (index < 0) {
            return "";
        }
        return temp.substring(0, index);
    }
}
