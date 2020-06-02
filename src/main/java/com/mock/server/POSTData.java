package com.mock.server;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

//https://github.com/everit-org/json-schema

public class POSTData {

    private static final Logger logger= LoggerFactory.getLogger(POSTData.class);

    private Schema schema;
    private final ArrayList <Payload> payloads;

    POSTData(){ payloads= new ArrayList <>(); }

    // lock this PostData
    public synchronized void setSchema(Schema schema){
        logger.info("Schema Updated to "+ schema);
        this.schema=schema;
    }

    public String getSchema(){
        return schema.toString();
    }

    private void verifySchema(JSONObject jsonObject) throws ValidationException {
        if(schema==null) return;
        schema.validate(jsonObject);
    }

    public void addPayload(Payload payload){
        verifySchema(payload.getPayload());
        logger.info("Schema Verified!");
        synchronized (payloads){
            payloads.add(payload);
        }
    }

    public int anyMatchPayload(JSONObject jsonObject){
        int key=-1;
        synchronized (payloads){ // else iterator will through concurrent modification exception
            for(Payload payload : payloads) {
                if(payload.equals(jsonObject)) {
                    logger.info("Match found with body->"+ payload.getKey());
                    key = payload.getKey();
                    break;
                }else{
                    logger.info("Didn't Match with body->"+ payload.getKey());
                }
            }
        }
        return key;
    }
}
