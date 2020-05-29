package com.mock.server;

import org.redisson.Redisson;
import org.redisson.RedissonBucket;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RedisClient {

    private final RedissonClient redissonClient;
    private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);

    private RedisClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redissonClient= Redisson.create(config);
    }

    public void addVal(String path, String jsonString){
        // add the response to the list
        RList <String> list = redissonClient.getList(path);
        list.add(jsonString);
    }

    public String getVal(String path){
        // send some random value from the possible list of responses
        RList<String> list = redissonClient.getList(path);
        if(list.size()!=0) return list.get(new Random().nextInt(list.size()));
        throw new IllegalArgumentException("Path not found!");
    }

    public int getCounter(){
        RBucket<Integer> rBucket= redissonClient.getBucket("counter");
        return  rBucket.get();
    }

    public void setCounter(int val){
        RBucket<Integer> rBucket= redissonClient.getBucket("counter");
        rBucket.set(val);
    }

    public void remove(String path){
        // deletes all the responses from that path
        RedissonBucket bucket = (RedissonBucket) redissonClient.getBucket(path);
        bucket.delete();
    }

}
