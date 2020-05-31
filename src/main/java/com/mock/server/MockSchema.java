package com.mock.server;

import org.json.JSONObject;

public class MockSchema {

    private Method method = Method.GET;
    private boolean checkMode = false;
    private JSONObject schema;
    private String path;
    private String queryParameters;
    private String queryParametersRegex;

    public Method getMethod() {
        return method;
    }

    public void setPath(String path) { this.path=path; }

    public String getPath(){ return path; }

    public void setMethod(Method method) { this.method = method; }

    public boolean isCheckMode() { return checkMode; }

    public void setCheckMode(boolean checkMode) {
        this.checkMode = checkMode;
    }

    public JSONObject getSchema() {
        return schema;
    }

    public void setSchema(JSONObject schema) {
        this.schema = schema;
    }

    public String getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(String queryParameters) {
        this.queryParameters = queryParameters;
    }

    public String getQueryParametersRegex() {
        return queryParametersRegex;
    }

    public void setQueryParametersRegex(String queryParametersRegex) {
        this.queryParametersRegex = queryParametersRegex;
    }

}
