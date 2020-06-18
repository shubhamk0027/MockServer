package com.mock.server.Server;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;


public class PayloadBody {

    private JSONObject payload;
    private boolean mode;

    PayloadBody(boolean mode, JSONObject payload) {
        this.mode=mode;
        this.payload=payload;
    }

    public void equals(JSONObject object) {
        try {
            JSONAssert.assertEquals(payload, object, mode);
        }catch(AssertionError e){
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void setPayloadBody(JSONObject jsonObject){ this.payload = jsonObject;}
    public void setCheckMode(boolean mode){this.mode=mode;}
}
