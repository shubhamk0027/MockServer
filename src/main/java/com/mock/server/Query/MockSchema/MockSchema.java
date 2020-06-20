package com.mock.server.Query.MockSchema;

import com.mock.server.Query.Method;


public class MockSchema {

    private String teamKey;                 // Team Secret API Key
    private Method method;                  // POST PUT DEL GET HEAD TRACE OPTIONS
    private String path;                    // some/simple/or/[a-zA-Z0-9]+/path/with/regex
    private String jsonSchema;              // {\"properties\":{\"itemName\":{\"type\":\"string\"},\"price\":{\"type\":\"number\"}}}"
    private String queryParameters;         // ?simple=query&parameter=(s)
    private String queryParametersRegex;    // ?complex=[a-zA-Z]+&query=parameters[0-9]+

    public MockSchema() {

    }

    public String getTeamKey() { return teamKey; }

    public Method getMethod() {
        return method;
    }

    public String getJsonSchema() {
        return jsonSchema;
    }

    public String getQueryParameters() {
        return queryParameters;
    }

    public String getQueryParametersRegex() {
        return queryParametersRegex;
    }

    public String getPath() {
        return path;
    }


    public MockSchema fromTeam(String teamKey) {
        this.teamKey = teamKey;
        return this;
    }

    public MockSchema hasMethod(Method method) {
        this.method = method;
        return this;
    }

    public MockSchema hasPath(String path) {
        this.path = path;
        return this;
    }

    public MockSchema hasQueryParameters(String queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public MockSchema hasQueryParametersRegex(String queryParametersRegex) {
        this.queryParametersRegex = queryParametersRegex;
        return this;
    }

    public MockSchema hasJsonSchema(String jsonSchema) {
        this.jsonSchema = jsonSchema;
        return this;
    }

}