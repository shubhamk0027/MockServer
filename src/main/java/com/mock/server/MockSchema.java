package com.mock.server;

import org.json.JSONObject;

public class MockSchema {

    private Method method = Method.POST;
    private boolean checkMode = false;
    private JSONObject schema;
    private String path;
    private String queryParameters;
    private String queryParametersRegex;

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

    public JSONObject getSchema() {
        return schema;
    }

    public MockSchema setSchema(JSONObject schema) {
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
