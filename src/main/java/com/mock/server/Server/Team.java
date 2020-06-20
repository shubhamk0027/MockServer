package com.mock.server.Server;

public class Team {

    private final String teamKey;
    private final String teamName;
    private final String adminId;
    private final MockServer mockServer;

    public Team(String teamKey, String teamName, String adminId, MockServer mockServer) {
        this.teamKey = teamKey;
        this.teamName = teamName;
        this.adminId = adminId;
        this.mockServer = mockServer;
    }

    public String getTeamKey() {
        return teamKey;
    }

    public String getTeamName() {
        return teamName;
    }

    public MockServer getMockServer() {
        return mockServer;
    }

    public String getAdminId() {
        return adminId;
    }

}
