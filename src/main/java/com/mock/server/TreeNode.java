package com.mock.server;
import java.util.HashMap;

public class TreeNode {
    private final String val;
    private final HashMap <String,TreeNode> child = new HashMap <>();
    private int id;

    public TreeNode(String val){
        this.val=val;
        id=-1;
    }

    public String getVal() { return val; }
    public HashMap<String, TreeNode> getChildren() { return child; }
    public int getId() { return id;}
    public void setId(int id) { this.id = id;}
    public void addChild(String val, TreeNode node) { child.put(val,node); }

}
