package com.mock.server;

import org.springframework.stereotype.Component;

import java.util.Map;

public class RedisValue {

    public int status;
    public String resBody;
    public Map <String,String> resHeaders;

    public RedisValue(){}

    public RedisValue(MockResponse mockResponse){
        status = mockResponse.getStatus();
        resBody = mockResponse.getJsonBody().toString();
        resHeaders= mockResponse.getHeaders();
    }

}
