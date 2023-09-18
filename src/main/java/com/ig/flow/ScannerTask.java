package com.ig.flow;

import com.alibaba.fastjson.JSON;
import com.ig.common.Main;
import com.ig.config.Config;
import com.ig.domain.FileInfo;
import com.ig.util.RedisUtil;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * @author: zhili
 * @date: 2022/5/29 1:23
 */
public class ScannerTask implements Runnable {

    private final String path;

    public ScannerTask(String path) {
        this.path = path;
    }

    @Override
    public void run() {
        FileVisitor<Path> fileVisitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Objects.requireNonNull(file);
                Objects.requireNonNull(attrs);
                Objects.requireNonNull(file.toFile());
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".jpg") || fileName.endsWith(".png")
                        || fileName.endsWith(".JPG") || fileName.endsWith(".jpeg")
                        || fileName.endsWith(".gif")) {
                    //生成fileInfo
                    FileInfo fileInfo = new FileInfo(file);
                    boolean sumbit = false;
//                    提交线程
                    do {
                        Jedis jedis = null;
                        try {
                            jedis = RedisUtil.getJedis();
                            if (jedis == null) {
                                continue;
                            }
                            jedis.rpush(Config.getProperties("stream.key"), JSON.toJSONString(fileInfo));
                            sumbit = true;
                        } catch (Exception ex) {
                            continue;
                        } finally {
                            RedisUtil.returnResource(jedis);
                        }
                        Main.scanNumber.incrementAndGet();
                    } while (!sumbit);
//                    Future<String> result = com.ig.common.Main.threadPoolExecutor.submit(new UploadTask(fileInfo));
                }
//                return FileVisitResult.TERMINATE;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println(dir.toString());
                return super.preVisitDirectory(dir, attrs);
            }
        };
        try {
            Files.walkFileTree(Paths.get(path), fileVisitor);
        } catch (IOException e) {
            // TODO 记录错误信息等待后续处理
//            e.printStackTrace();
            System.out.println("扫描出错:" + e.getMessage());
        }
    }
}

