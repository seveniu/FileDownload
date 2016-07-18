package com.seveniu.thriftServer;

import com.seveniu.fileDownloader.FileDownloadManager;
import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by seveniu on 7/3/16.
 * ThriftServer
 */
@Service
public class ThriftServer {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile boolean isRunning;
    @Autowired
    FileDownloadManager fileDownloadManager;

    @Autowired
    public ThriftServer(@Value("${fileDownloader.port}") int port) {
        startServer(port);
    }

    public void startServer(int port) {
        if (isRunning) {
            logger.info("thrift server is running");
            return;
        }
        new Thread(() -> {

            try {
                TServerSocket socket = new TServerSocket(port);
                DownloaderThrift.Processor processor = new DownloaderThrift.Processor<>(new Server());
                TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(socket).processor(processor));
                isRunning = true;
                server.serve();
            } catch (TTransportException e) {
                e.printStackTrace();
            }
        }, "thrift-server-thread").start();
        logger.info("start crawl thrift server at : {}", port);
    }

    private static final Map<String, Result> empty = new HashMap<>(0);

    private class Server implements DownloaderThrift.Iface {

        @Override
        public Map<String, Result> download(List<String> urls) throws TException {
            try {
                return fileDownloadManager.download(urls);
            } catch (InterruptedException e) {
                return empty;
            }
        }
    }

}
