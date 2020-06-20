package com.mock.server.URITree;

import com.mock.server.Query.MockResponse;
import org.json.JSONObject;

import java.util.ArrayList;

// Not thread safe,
// Concurrency is handled by the caller
// A POST path can lead to multiple Payload, Response pair

public class PayloadList implements Leaf {

    private final ArrayList <PayloadResponse> payloadResponses;

    public PayloadList() {
        this.payloadResponses = new ArrayList <>();
    }

    public void addPayload(PayloadResponse payloadResponse) {
        payloadResponses.add(payloadResponse);
    }

    public MockResponse getMatch(JSONObject object) {
        MockResponse response = null;
        for(PayloadResponse payloadResponse : payloadResponses) {
            response = payloadResponse.matches(object);
            if(response != null) break;
        }
        return response;
    }

    public boolean deletePayload(JSONObject jsonObject) {
        for(PayloadResponse payloadResponse : payloadResponses)
            if(payloadResponse.isSameThenDelete(jsonObject)) {
                return true;
            }
        return false;
    }
}
