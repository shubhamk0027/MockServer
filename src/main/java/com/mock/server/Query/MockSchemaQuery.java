package com.mock.server.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockSchemaQuery {

    private static final Logger logger = LoggerFactory.getLogger(MockSchemaQuery.class);

    private String teamKey;
    private Method method = Method.POST;
    private String schema;
    private String path;
    private String queryParameters;
    private String queryParametersRegex;

    public String getPath(){ return path; }

    public Method getMethod() {
        return method;
    }

    public String getSchema() {
        return schema;
    }

    public String getQueryParameters() {
        return queryParameters;
    }

    public String getQueryParametersRegex() {
        return queryParametersRegex;
    }

    public String getTeamKey(){ return teamKey; }

    public MockSchemaQuery setTeamKey(String teamKey){
        this.teamKey=teamKey;
        return this;
    }

    public MockSchemaQuery setPath(String path) {
        this.path=path;
        return this;
    }

    public MockSchemaQuery setMethod(Method method) {
        this.method = method;
        return this;
    }

    public MockSchemaQuery setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public MockSchemaQuery setQueryParameters(String queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public MockSchemaQuery setQueryParametersRegex(String queryParametersRegex) {
        this.queryParametersRegex = queryParametersRegex;
        return this;
    }

    public void log(){
        logger.info("Mock Schema ......");
        logger.info("teamKey: "+teamKey);
        logger.info("method: "+method);
        logger.info("schema: "+schema);
        logger.info("path: "+path);
        logger.info("queryParameters: "+queryParameters);
        logger.info("queryParametersRegex: "+queryParametersRegex);
    }
}
