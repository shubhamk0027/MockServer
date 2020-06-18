package com.mock.server.Server;

import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;

// https://github.com/skyscreamer/JSONassert

public class Payload {

    private JSONObject payload;
    private final boolean mode;
    private final int key;

    Payload(int key, boolean mode, JSONObject payload) {
        this.key=key;
        this.mode=mode;
        this.payload=payload;
    }

    public boolean equals(JSONObject object) {
        if(payload==null) return false;
        try {
            JSONAssert.assertEquals(payload, object, mode);
            return true;
        }catch(AssertionError e){
            return false;
        }
    }

    public void setNull(){ payload =null; }
    public JSONObject getPayload(){ return payload;}
    public int getKey(){ return key; }
}
