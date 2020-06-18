package com.mock.server.Query;

import java.util.HashMap;
import java.util.Map;

// this jsonBody will be converted to the string before inserting it to the redis
// before insertion it will go through a schema check
// these operations are not atomic, it is possible that path is inserted but the json schema does not match!
// in that case the payload will not be added and so there will not be any effect except few extra nodes on the URItree

public class MockResponse {

    private int status;
    private String jsonBody;
    private Map <String,String> headers;

    public int getStatus() {
        return status;
    }
    public String getJsonBody() {
        return jsonBody;
    }
    public Map <String, String> getHeaders() {
        return headers;
    }

    public MockResponse withStatus(int status){
        this.status=status;
        return this;
    }

    public MockResponse withBody(String jsonBody){
        this.jsonBody=jsonBody;
        return this;
    }

    public MockResponse withHeaders(Map<String,String> headers){
        this.headers=headers;
        return this;
    }

    public MockResponse withHeader(String key, String val){
        if(headers==null) headers= new HashMap <>();
        headers.put(key,val);
        return this;
    }

}
