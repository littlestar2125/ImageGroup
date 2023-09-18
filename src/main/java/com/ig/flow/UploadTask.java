package com.ig.flow;

import com.alibaba.fastjson.JSON;
import com.ig.common.Main;
import com.ig.config.Config;
import com.ig.domain.FileInfo;
import com.ig.util.RedisUtil;
import redis.clients.jedis.Jedis;

import java.util.List;

public class UploadTask implements Runnable {

    public UploadTask() {
    }

    @Override
    public void run() {
        do {
            Jedis jedis;
            do {
                jedis = RedisUtil.getJedis();
            } while (jedis == null);
            List<String> fileInfoList = jedis.brpop(10, Config.getProperties("stream.key"));
            if (fileInfoList == null || fileInfoList.size() == 0) {
                continue;
            }
            try {
                FileInfo fileInfo = JSON.parseObject(fileInfoList.get(1), FileInfo.class);
                Main.uploadNumber.incrementAndGet();
            } catch (Exception ex) {
                jedis.lpush(Config.getProperties("stream.key"), fileInfoList.get(1));
            }
            RedisUtil.returnResource(jedis);
        } while (!Main.scanExecutor.isTerminated());

    }

//    @Override
//    public void close() throws Exception {
//        if (jedis != null) {
//            jedis.close();
//        }
//    }
}
