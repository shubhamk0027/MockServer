package com.mock.server.Query;

public class GetSchemaQuery {

    private Method method;
    private String teamKey;
    private String path;

    public GetSchemaQuery() {

    }

    public String getTeamKey() {
        return teamKey;
    }

    public String getPath() {
        return path;
    }

    public Method getMethod() {
        return method;
    }

    public void setTeamKey(String teamKey) {
        this.teamKey = teamKey;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

}

