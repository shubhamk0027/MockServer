package com.mock.server;

public class DevTeam {
    private String key;
    private String teamName;
    private String adminId;
    private MockServer mockServer;

    DevTeam(String key, String teamName, String adminId, MockServer mockServer){
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
