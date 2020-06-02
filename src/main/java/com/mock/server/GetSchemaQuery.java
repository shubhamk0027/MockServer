package com.mock.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetSchemaQuery {
    private static final Logger logger = LoggerFactory.getLogger(GetSchemaQuery.class);
    private String teamKey;
    private String path;

    public String getTeamKey() {
        return teamKey;
    }

    public String getPath() {
        return path;
    }

    public void setTeamName(String teamName){
        this.teamKey=teamName;
    }

    public void setPath(String path){
        this.path=path;
    }

    public void log(){
        logger.info("Schema Query......");
        logger.info("teamKey: "+teamKey);
        logger.info("path: "+path);
    }
}

