package com.mock.server.URITree;

import com.mock.server.Query.MockResponse;
import org.everit.json.schema.Schema;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

public class TreeNode {

    private final Directory directory;
    private Leaf leafValue;

    /* Separated for first matching among the directory Names, if not found then among the directory Patterns */

    private final ConcurrentHashMap <String, TreeNode> childPatterns;
    private final ConcurrentHashMap <String, TreeNode> childNames;

    public TreeNode(Directory dir) {
        this.directory = dir;
        childNames = new ConcurrentHashMap <>();
        childPatterns = new ConcurrentHashMap <>();
        leafValue = null;
    }

    /*#######################################################################################################
    ################################## OPERATIONS FOR URI TREE TRAVERSALS ###################################
    ########################################################################################################*/

    public String getName() {
        return directory.getDirName();
    }

    public boolean matches(String s) {
        return directory.matches(s);
    }

    public ConcurrentHashMap <String, TreeNode> getChildNames() {
        return childNames;
    }

    public ConcurrentHashMap <String, TreeNode> getChildPatterns() {
        return childPatterns;
    }

    public void addChildName(String name, TreeNode node) {
        childNames.put(name, node);
    }

    public void addChildPatters(String name, TreeNode node) {
        childPatterns.put(name, node);
    }

    public Leaf getLeaf() {
        return leafValue;
    }

    /*#######################################################################################################
    ############################## OPERATIONS FOR THE LEAVES OF POST  NODES #################################
    ########################################################################################################*/

    // By performing the operations here, synchronizing the local nodes is avoided

    // If Leaf Node is has a PayloadList then

    public synchronized void addAPayload(PayloadResponse payloadResponse) {
        if(leafValue == null) leafValue = new PayloadList();
        else if(leafValue instanceof SchemaResponse)
            throw new IllegalArgumentException("A JSON SCHEMA exists at this path, " +
                    "to continue, delete that path and try adding the payloadResponse again!");
        PayloadList payloadList = (PayloadList) leafValue;
        payloadList.addPayload(payloadResponse);
    }

    public synchronized void deleteAPayload(JSONObject requestBody) {
        if(leafValue == null || leafValue instanceof SchemaResponse)
            throw new IllegalArgumentException("No Matching payload found!");
        if(!((PayloadList) leafValue).deletePayload(requestBody))
            throw new IllegalArgumentException("No matching PayloadResponse Body found!");
    }

    // If the Leaf Node is a SchemaResponse

    public synchronized void addSchema(Schema schema, MockResponse mockResponse) {
        if(leafValue != null && leafValue instanceof PayloadList)
            throw new IllegalArgumentException("A PayloadResponse List is also attached " +
                    "to this path, try deleting the payload List at his path and then continue!");
        leafValue = new SchemaResponse(schema, mockResponse);
    }

    public synchronized String getSchema() {
        if(leafValue == null) throw new IllegalArgumentException("No value exists at this path!");
        if(leafValue instanceof PayloadList)
            throw new IllegalArgumentException("A PayloadResponse list is present at this path! " +
                    "No PayloadResponse Schema found!");
        return ((SchemaResponse) leafValue).getSchema();
    }

    // If SchemaResponse, validate the request body against the schema and return response
    // Else check if a matching payload exists in the payload list

    public synchronized MockResponse getResponse(JSONObject jsonObject) {
        if(leafValue == null) throw new IllegalArgumentException("No value exists at this path!");
        if(leafValue instanceof SchemaResponse)
            return ((SchemaResponse) leafValue).getResponse(jsonObject);
        MockResponse response = ((PayloadList) leafValue).getMatch(jsonObject);
        if(response == null) throw new IllegalArgumentException("No matching payload found!");
        return response;
    }

    /*#######################################################################################################
    ############################## OPERATIONS FOR THE LEAVES OF GET NODES ###################################
    ########################################################################################################*/

    public void updateResponse(MockResponse mockResponse) {
        leafValue = mockResponse;
    }

    public synchronized MockResponse getResponse() {
        if(leafValue == null) throw new IllegalArgumentException("No value exists at this path!");
        return (MockResponse) leafValue;
    }

    /*#######################################################################################################
    ############################## OPERATIONS FOR THE LEAVES OF BOTH TYPE ###################################
    ########################################################################################################*/

    public synchronized void deleteAll() {
        this.leafValue = null;
    }

}

