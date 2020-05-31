package com.mock.server;

import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;

// https://github.com/skyscreamer/JSONassert

public class Payload {

    private final JSONObject payload;
    private final boolean mode;
    private final int key;

    Payload(int key, boolean mode, JSONObject payload) {
        this.key=key;
        this.mode=mode;
        this.payload=payload;
    }

    public boolean equals(JSONObject object) {
        try {
            JSONAssert.assertEquals(payload, object, mode);
            return true;
        }catch(JSONException e){
            return false;
        }
    }

    public JSONObject getPayload(){ return payload;}
    public int getKey(){ return key; }
}
