package com.mock.server.URITree;

// When directory int the path is a simple string without any regular expression

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
