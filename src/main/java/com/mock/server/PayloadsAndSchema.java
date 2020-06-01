package com.mock.server;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

// https://www.baeldung.com/java-org-json
// this feature is only for post and put method
// for the rest it will be ignored!

@Component
// @Scope("prototype")
public class PayloadsAndSchema {

    private final ArrayList<POSTData> postData ;
    private int payLoadCount;

    private PayloadsAndSchema(){
        postData = new ArrayList <>();
        payLoadCount= 0;
    }

    public synchronized int addPayload(int key,boolean mode, JSONObject object){
        // this whole operation is made synchronized to make sure the addition is atomic.
        // otherwise inconsistency can occur!
        if(key>postData.size()){
            postData.add(new POSTData());
        }
        int ret=++payLoadCount;
        try {
            Payload payload = new Payload(ret, mode, object);
            postData.get(key - 1).addPayload(payload);
        }catch(Exception e){
            payLoadCount--;
            throw e;
        }
        return ret;
    }

    public int checkPayload(int key, JSONObject object) throws Exception {
        key = postData.get(key-1).anyMatchPayload(object);
        if(key==-1) throw new Exception("No matching payload found!");
        return key;
    }

    public void addSchema(int key, boolean mode, Schema schema){
        synchronized (this){
            if(key>postData.size()){
                postData.add(new POSTData());
            }
        }
        postData.get(key-1).setSchema(schema,mode);
    }

    public String getSchema(int key) throws IllegalAccessException {
        if(key>postData.size()) {
            throw new IllegalAccessException("No Schema Present");
        }
        return postData.get(key-1).getSchema();
    }
}
