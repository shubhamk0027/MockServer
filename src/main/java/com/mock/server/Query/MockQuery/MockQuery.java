package com.mock.server.Query.MockQuery;
import com.mock.server.Query.MockResponse;
import org.springframework.stereotype.Service;

@Service
public class MockQuery {

    private MockRequest mockRequest;
    private MockResponse mockResponse;

    public MockQuery inCase(MockRequest mockRequest){
        this.mockRequest=mockRequest;
        return this;
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
