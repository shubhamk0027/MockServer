package com.mock.server;

import java.util.regex.Pattern;

public class DirPattern implements Directory {

    private String name;
    private Pattern pattern;

    DirPattern(String name, Pattern pattern) {
        this.name=name;
        this.pattern = pattern;
    }

    @Override
    public boolean matches(String s){
        return pattern.matcher(s).matches();
    }

    public String getDirName(){
        return name;
    }
}
