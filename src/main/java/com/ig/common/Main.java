package com.ig.common;

import com.ig.config.Config;
import com.ig.flow.UploadTask;
import com.ig.util.MyCustomThreadFactory;
import com.ig.flow.ScannerTask;
import com.ig.util.RedisUtil;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: zhili
 * @date: 2022/5/18 22:02
 */
public class Main {

    public static AtomicInteger scanNumber = new AtomicInteger(0);
    public static AtomicInteger uploadNumber = new AtomicInteger(0);

    public static ThreadPoolExecutor scanExecutor;


    //不添加redis 6.226分钟
    //添加redis

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
//        清除redis中的uploadList
        Jedis jedis;
        do {
            jedis = RedisUtil.getJedis();
        } while (jedis == null);
        jedis.del(Config.getProperties("stream.key"));
        RedisUtil.returnResource(jedis);

        //创建一个图片列表
        List<String> paths = new ArrayList<>() {
            {
                add("E:\\MY\\lz");
                add("E:\\MY\\李智");
                add("E:\\MY\\imgZip");
            }
        };
        //扫描线程
        scanExecutor = new ThreadPoolExecutor(paths.size(), 10, 30L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new MyCustomThreadFactory("扫描线程"),
                new ThreadPoolExecutor.DiscardPolicy());

        ThreadPoolExecutor upload = new ThreadPoolExecutor(10, 10, 30L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new MyCustomThreadFactory("扫描线程"),
                new ThreadPoolExecutor.DiscardPolicy());

        for (int i = 0; i < upload.getMaximumPoolSize(); i++) {
            upload.execute(new UploadTask());
        }

        //开始扫描
        for (var path : paths) {
            scanExecutor.execute(new ScannerTask(path));
        }

        scanExecutor.shutdown();
        upload.shutdown();

        try {
            scanExecutor.awaitTermination(10, TimeUnit.HOURS);
            upload.awaitTermination(10, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(scanNumber.get());
        System.out.println(uploadNumber.get());
        System.out.println("Used Time:" + (System.currentTimeMillis() - startTime));
    }
}
