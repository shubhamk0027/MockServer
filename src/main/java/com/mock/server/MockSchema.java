package com.mock.server;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

public class MockSchema {

    public Method method = Method.POST;
    public boolean checkMode = false;
    public String schema;
    public String path;
    public String queryParameters;
    public String queryParametersRegex;

    public Method getMethod() {
        return method;
    }

    public MockSchema setPath(String path) {
        this.path=path;
        return this;
    }

    public String getPath(){ return path; }

    public MockSchema setMethod(Method method) {
        this.method = method;
        return this;
    }

    public boolean isCheckMode() { return checkMode; }

    public MockSchema strictCheckMode(boolean checkMode) {
        this.checkMode = checkMode;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public MockSchema setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getQueryParameters() {
        return queryParameters;
    }

    public MockSchema setQueryParameters(String queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public String getQueryParametersRegex() {
        return queryParametersRegex;
    }

    public MockSchema setQueryParametersRegex(String queryParametersRegex) {
        this.queryParametersRegex = queryParametersRegex;
        return this;
    }
}
