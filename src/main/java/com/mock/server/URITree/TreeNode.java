package com.mock.server.URITree;

import java.util.HashMap;

public class TreeNode {

    private int id;
    private final Directory directory;
    private final HashMap <String, TreeNode> child = new HashMap <>();

    public TreeNode(Directory dir) {
        this.directory=dir;
        id = -1;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public String getName() { return directory.getDirName(); }
    public boolean matches(String s){ return directory.matches(s); }

    public HashMap <String, TreeNode> getChildren() {
        return child;
    }
    public void addChild(String name, TreeNode node) {
        child.put(name, node);
    }
}

