package com.mock.server.URITree;

import com.mock.server.Query.MockResponse;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;

public class PayloadResponse {

    private final boolean checkMode;
    private JSONObject actualRequestBody;
    private MockResponse mockResponse;

    public PayloadResponse(
            boolean checkMode,
            JSONObject actualRequestBody,
            MockResponse mockResponse) {
        this.checkMode = checkMode;
        this.actualRequestBody = actualRequestBody;
        this.mockResponse = mockResponse;
    }

    public MockResponse matches(JSONObject requestBody) {
        if(actualRequestBody == null) return null;            // a deleted PayloadResponse
        try {
            JSONAssert.assertEquals(requestBody, actualRequestBody, checkMode);
        }catch(AssertionError e) {
            return null;
        }
        return mockResponse;
    }

    public boolean isSameThenDelete(JSONObject jsonObject) {
        try {
            JSONAssert.assertEquals(jsonObject, actualRequestBody, true);
            actualRequestBody = null;
            mockResponse = null;
        }catch(AssertionError e) {
            return false;
        }
        return true;
    }

}
