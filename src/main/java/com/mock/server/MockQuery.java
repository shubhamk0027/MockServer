package com.mock.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockQuery {

    // no headers considered for  now
    public enum Method{
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DEL("DEL");
        public String val;
        Method (String val) { this.val= val; }
    }

    public Method method;
    public String path;
    public MockResponse response;


    // BUILDER................................................
    public static class Builder{
        private Method method;
        private String path;
        private MockResponse mockResponse;

        Builder(){
            method=Method.GET;
            mockResponse=new MockResponse();
        }

        public MockQuery build(){
            MockQuery mockQuery= new MockQuery();
            mockQuery.method=method;
            mockQuery.path=path;
            mockQuery.response=mockResponse;
            return mockQuery;
        }

        public Builder setMethod(Method method){ this.method=method; return this;}
        public Builder setPath(String path){ this.path= path; return this;}
        public Builder setResponseBody(String body){ this.mockResponse.body=body; return this;}
        public Builder setResponseCode(int status){ this.mockResponse.status=status; return this;}
    }


    // Object Mapper................................................................................
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(MockQuery.class);


    // not handled exceptions
    // have to add generalization to the requestBodyJson
    public static boolean isValidPath(String path) {
        if(path==null || path.length()==0) throw new IllegalArgumentException("Path can not be Null or empty");
        if(path.charAt(0)!='/') throw  new IllegalArgumentException("Path must be relative");
        return  true;
    }

    // exceptions raised here will be handled by mockserver
    public static MockQuery getMockRequest(String json) throws JsonProcessingException {
        MockQuery res =mapper.readValue(json, MockQuery.class);

        logger.info("REQUEST:  "+res.method+" "+res.path);
        logger.info("RESPONSE: "+res.response.status+" "+res.response.body);

        isValidPath(res.path);// will throw exp if not
        if(res.method==null) throw new IllegalArgumentException("Invalid Method");
        if(res.response.status==null) throw new IllegalArgumentException("Response Status can not be empty!");
        if(res.response.body==null) res.response.body="";

        for(int i=1;i<res.path.length();i++) {
            if(res.path.charAt(i)=='/' && res.path.charAt(i-1)=='/') throw new IllegalArgumentException("Cant have empty directories");
        }

        return res;
    }

}
