package com.mock.server.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockQuery {

    private MockRequest mockRequest;
    private MockResponse mockResponse;
    private static final Logger logger = LoggerFactory.getLogger(MockQuery.class);

    public MockQuery inCase(MockRequest mockRequest){
        this.mockRequest=mockRequest;
        return this;
    }

     public void log(){
        logger.info(".................................");
        logger.info("MockQuery Formed with MockRequest");
        logger.info("Method: "+getMockRequest().getMethod().val);
        logger.info("PATH: "+getMockRequest().getPath());
        logger.info("QString: "+getMockRequest().getQueryParameters());
        logger.info("QRegex: "+getMockRequest().getQueryParametersRegex());
        logger.info("JsonReq: "+getMockRequest().getJsonBody());
        logger.info("Status: "+getMockResponse().getStatus());
        logger.info("Headers: "+getMockResponse().getHeaders().toString());
        logger.info("JsonRes: "+getMockResponse().getJsonBody());
    }

    public MockQuery respondWith(MockResponse mockResponse){
        this.mockResponse=mockResponse;
        return this;
    }

    public MockRequest getMockRequest() {
        return mockRequest;
    }

    public MockResponse getMockResponse() {
        return mockResponse;
    }
}
