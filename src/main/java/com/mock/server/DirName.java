package com.mock.server;

public class DirName implements Directory {

    private final String name;

    public DirName(String name){
        this.name=name;
    }

    @Override
    public boolean matches(String name){
        return this.name.equals(name);
    }

    @Override
    public String getDirName() {
        return name;
    }
}
