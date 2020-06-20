package com.mock.server.Query.MockSchema;

import com.mock.server.Query.MockResponse;
import org.springframework.stereotype.Service;

// A MockSchemaQuery is a pair of MockRequest and corresponding mockResponse
@Service
public class MockSchemaQuery {

    private MockSchema mockSchema;
    private MockResponse mockResponse;

    public MockSchemaQuery() {

    }

    public MockSchema getMockSchema() {
        return mockSchema;
    }

    public MockResponse getMockResponse() {
        return mockResponse;
    }

    public MockSchemaQuery inCase(MockSchema mockSchema) {
        this.mockSchema = mockSchema;
        return this;
    }

    public MockSchemaQuery respondWith(MockResponse mockResponse) {
        this.mockResponse = mockResponse;
        return this;
    }

}
