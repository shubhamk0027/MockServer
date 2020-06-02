package com.mock.server;

import org.json.JSONObject;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
// even really really long keys don't have a large impact on the speed of redis

@Service
public class RedisClient {

    private final RedissonClient redissonClient;
    private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);

    private RedisClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redissonClient= Redisson.create(config);
    }

    public void addVal(String key, String jsonString){
        RList <String> list = redissonClient.getList(key);
        list.add(jsonString);
    }

    public void deleteKey(String key){
        redissonClient.getBucket(key).delete();
    }

    public String getVal(String key){
        RList<String> list = redissonClient.getList(key);
        if(list.size()!=0) return list.get(new Random().nextInt(list.size()));
        throw new IllegalArgumentException("Path not found!");
    }

}

