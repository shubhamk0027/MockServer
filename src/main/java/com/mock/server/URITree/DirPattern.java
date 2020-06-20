package com.mock.server.URITree;

import java.util.regex.Pattern;

// When the directory in the path is represented as a regular expression

public class DirPattern implements Directory {

    private final String name;              // the regular expression
    private final Pattern pattern;          // the compiled regular expression

    public DirPattern(String name, Pattern pattern) {
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
