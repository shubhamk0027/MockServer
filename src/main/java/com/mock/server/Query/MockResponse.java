package com.mock.server.Query;

import com.mock.server.URITree.Leaf;

import java.util.HashMap;
import java.util.Map;

public class MockResponse implements Leaf {

    private int statusCode;
    private String responseBody;
    private Map <String, String> headers;

    public MockResponse() {

    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Map <String, String> getHeaders() {
        return headers;
    }

    public MockResponse withStatusCode(int status) {
        this.statusCode = status;
        return this;
    }

    public MockResponse withResponseBody(String responseBody) {
        this.responseBody = responseBody;
        return this;
    }

    public MockResponse withHeaders(Map <String, String> headers) {
        this.headers = headers;
        return this;
    }

    public MockResponse withHeader(String key, String val) {
        if(headers == null) headers = new HashMap <>();
        headers.put(key, val);
        return this;
    }

}
