package com.mock.server.Server;
import org.everit.json.schema.Schema;
import org.json.JSONObject;

public class PayloadSchema {
    private Schema schema;

    PayloadSchema(Schema schema){
        this.schema=schema;
    }

    public void setSchema(Schema schema){ this.schema=schema;}
    public void verify(JSONObject jsonObject){  schema.validate(jsonObject);  } // schema will never be null!
    public Schema getSchema() { return schema; }

}
