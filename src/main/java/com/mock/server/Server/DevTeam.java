package com.mock.server.Server;

public class DevTeam {

    private final String key;
    private final String teamName;
    private final String adminId;
    private final MockServer mockServer;

    public DevTeam(String key, String teamName, String adminId, MockServer mockServer){
        this.key=key;
        this.teamName=teamName;
        this.adminId=adminId;
        this.mockServer=mockServer;
    }

    public String getKey(){ return  key; }
    public String getTeamName(){ return teamName; }
    public MockServer getMockServer() { return  mockServer; }
    public String getAdminId(){ return adminId; }

}
