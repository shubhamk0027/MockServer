package com.mock.server.URITree;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TreeNode {

    private int id;
    private final Directory directory;

    // separated to first match with the names, if not found then with the patterns
    private final ConcurrentHashMap <String, TreeNode> childPatterns;
    // list of the regex patterns treeNodes, searched linearly
    private final ConcurrentHashMap <String, TreeNode> childNames;
    // map of string -> TreeNodes, searched by name

    public TreeNode(Directory dir) {
        this.directory=dir;
        id = 0;
        childNames= new ConcurrentHashMap <>();
        childPatterns= new ConcurrentHashMap <>();

    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public String getName() { return directory.getDirName(); }
    public boolean matches(String s){ return directory.matches(s); }

    public ConcurrentHashMap <String, TreeNode> getChildNames() { return childNames; }
    public ConcurrentHashMap <String, TreeNode> getChildPatterns(){ return childPatterns; }

    public void addChildName(String name, TreeNode node) { childNames.put(name,node); }
    public void addChildPatters(String name,TreeNode node){ childPatterns.put(name,node); }

    /**
     * Since it is possible that a prefix path is to be deleted,
     * therefore the childNames, and childPatterns can still have valid responses.
     * @return the initial id that was cleared
     */
    public synchronized int empty(){
        if(this.id==0) throw new IllegalArgumentException("This path does not exists!");
        int oldId=this.id;
        this.id=0;
        return oldId;
    }

}

