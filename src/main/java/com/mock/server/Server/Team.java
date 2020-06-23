package com.mock.server.Server;

public class Team {

    private final String teamKey;
    private final String teamName;
    private final String password;
    private final MockServer mockServer;

    public Team(String teamKey, String teamName, String password, MockServer mockServer) {
        this.teamKey = teamKey;
        this.teamName = teamName;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

}
