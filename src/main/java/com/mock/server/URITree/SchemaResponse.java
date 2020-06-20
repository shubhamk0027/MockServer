package com.mock.server.URITree;

import com.mock.server.Query.MockResponse;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;

// Schema Check details
// https://github.com/everit-org/json-schema
// http://json-schema.org/understanding-json-schema/

public class SchemaResponse implements Leaf {

    private final Schema schema;
    private final MockResponse response;

    public SchemaResponse(Schema schema, MockResponse response) {
        this.schema = schema;
        this.response = response;
    }

    public String getSchema() {
        return schema.toString();
    }

    public MockResponse getResponse(JSONObject jsonObject) throws ValidationException {
        // If Schema is validated then return response or throw ValidationException Error
        schema.validate(jsonObject);
        return response;
    }
}