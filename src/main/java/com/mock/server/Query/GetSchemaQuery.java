package com.mock.server.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetSchemaQuery {

    private static final Logger logger = LoggerFactory.getLogger(GetSchemaQuery.class);

    private Method method;
    private String teamKey;
    private String path;

    public String getTeamKey() {
        return teamKey;
    }
    public String getPath() { return path; }
    public Method getMethod() { return method; }

    public void setTeamKey(String teamKey){ this.teamKey =teamKey; }
    public void setPath(String path){ this.path=path; }
    public void setMethod(Method method) { this.method= method;}

    public void log(){
        logger.info("Schema Query......");
        logger.info("Method: "+method.val);
        logger.info("teamKey: "+ teamKey);
        logger.info("path: "+path);
    }
}

