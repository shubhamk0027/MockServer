package com.mock.server.Query;

public class DeleteTeamQuery {

    private String teamKey;
    private String adminId;

    DeleteTeamQuery() {

    }

    public String getTeamKey() {
        return teamKey;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setTeamKey(String teamKey) {
        this.teamKey = teamKey;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

}
