package com.mock.server;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.stereotype.Service;

@Service
public class RedisClient {

    private final RedissonClient redissonClient;

    private RedisClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redissonClient = Redisson.create(config);
    }

    // Overwriting the value at the same path
    public void addVal(String key, String jsonString) {
        RBucket <String> rBucket = redissonClient.getBucket(key);
        rBucket.set(jsonString);
    }

    public void deleteKey(String key) {
        redissonClient.getBucket(key).delete();
    }

    public String getVal(String key) {
        RBucket <String> rBucket = redissonClient.getBucket(key);
        if(!rBucket.isExists()) throw new IllegalStateException("This value has been deleted!");
        return rBucket.get();
    }

}
// Even really really long keys don't have a large impact on the speed of redis
