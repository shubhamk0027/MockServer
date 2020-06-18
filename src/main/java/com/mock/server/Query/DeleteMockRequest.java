package com.mock.server.Query;

public class DeleteMockRequest {
    private String teamKey;
    private Method method;
    private String path;
    private String queryParameters;
    private String queryParametersRegex;

    public String getTeamKey() { return teamKey; }
    public void setTeamKey(String teamKey) { this.teamKey = teamKey; }
    public Method getMethod() { return method; }
    public void setMethod(Method method) { this.method = method; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getQueryParameters() { return queryParameters; }
    public void setQueryParameters(String queryParameters) { this.queryParameters = queryParameters; }
    public String getQueryParametersRegex() { return queryParametersRegex; }
    public void setQueryParametersRegex(String queryParametersRegex) { this.queryParametersRegex = queryParametersRegex; }

}
