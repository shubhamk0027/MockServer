package com.mock.server;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

// https://www.baeldung.com/java-org-json
// this feature is only for post and put method
// for the rest it will be ignored!

@Component
public class PayloadsAndSchema {

    private final ArrayList<POSTData> postData ;
    private final AtomicInteger payLoadCount;

    private PayloadsAndSchema(){
        postData = new ArrayList <>();
        payLoadCount= new AtomicInteger(0);
    }

    private int getPayLoadCount(){
        return payLoadCount.incrementAndGet();
    }

    public int addPayload(int key,boolean mode, JSONObject object){
        synchronized (this){
            if(key>postData.size()){
                postData.add(new POSTData());
            }
        }
        int ret=getPayLoadCount();
        Payload payload = new Payload(ret,mode,object);
        postData.get(key-1).addPayload(payload);
        return ret;
    }

    public int checkPayload(int key, JSONObject object) throws Exception {
        key = postData.get(key-1).anyMatchPayload(object);
        if(key==-1) throw new Exception("No matching payload found!");
        return key;
    }

    public void addSchema(int key, boolean mode, JSONObject object){
        synchronized (this){
            if(key>postData.size()){
                postData.add(new POSTData());
            }
        }
        postData.get(key-1).setSchema(SchemaLoader.load(object),mode);
    }

}
