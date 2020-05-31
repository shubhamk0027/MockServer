package com.mock.server;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import java.util.ArrayList;

// https://www.baeldung.com/java-org-json
// this feature is only for post and put method
// for the rest it will be ignored!

@Component
public class PayloadsAndSchema {

    private final ArrayList<ArrayList <JSONObject>> payloads = new ArrayList<ArrayList<JSONObject>>();
    private final ArrayList <JSONObject> schema = new ArrayList <>();
    private int payLoadCount;

    private PayloadsAndSchema(){
        payLoadCount=0;
    }

    public void checkSchema(int key, JSONObject object){
        // schema check algo
    }

    // implement schema check before adding to the payload
    public int addPayload(int key, JSONObject object){
        checkSchema(key,object);
        synchronized (payloads){
            if(key>payloads.size()){
                payloads.add(new ArrayList <>());
            }
        }
        int ret;
        synchronized (payloads.get(key-1)){
            payloads.get(key-1).add(object);
            ret = ++payLoadCount;
        }
        return ret;
    }


    public int checkPayload(int key, JSONObject object){
        // check if there is any payload in key array matching object
        // JSONObject with = payloads.get(id-1);
        // return exception if is not equal!
        return key;
    }
}
