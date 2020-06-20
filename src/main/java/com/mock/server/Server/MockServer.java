package com.mock.server.Server;

import com.mock.server.Query.*;
import com.mock.server.Query.MockQuery.MockQuery;
import com.mock.server.Query.MockResponse;
import com.mock.server.Query.MockSchema.MockSchemaQuery;
import com.mock.server.URITree.*;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;

import java.util.*;

/* Most of the verifications and the exceptions can be eliminated if we consider all the requests
are coming from the slackBot and are verified */

@Service
@Scope("prototype")
public class MockServer {

    private static final Logger logger = LoggerFactory.getLogger(MockServer.class);
    private final TreeNode root;

    public MockServer() {
        root = new TreeNode(new DirName("ROOT"));
    }


    /*##########################################################################################
    #################################### ADDING MOCK QUERY #####################################
    ##########################################################################################*/

    /**
     * Common Helper Function for traversing the URI TREE and add node if it does not exists
     *
     * @param pathList Path to add
     * @return The leaf node where the response of this path may exists
     */
    private TreeNode traverseAndAdd(ArrayList <Directory> pathList) {
        TreeNode trav = root;
        for(Directory dir : pathList) {
            if(dir instanceof DirName) {
                if(!trav.getChildNames().containsKey(dir.getDirName()))
                    trav.addChildName(dir.getDirName(), new TreeNode(dir));
                trav = trav.getChildNames().get(dir.getDirName());
            }else {
                if(!trav.getChildPatterns().containsKey(dir.getDirName()))
                    trav.addChildPatters(dir.getDirName(), new TreeNode(dir));
                trav = trav.getChildPatterns().get(dir.getDirName());
            }
        }
        return trav;
    }

    /**
     * @param mockQuery a MockQuery
     * @throws JSONException if requestBody is not a valid json
     */
    public void addMockQuery(MockQuery mockQuery) throws JSONException {

        Verifier.verifyMethodAndQuery(
                mockQuery.getMockRequest().getMethod(),
                mockQuery.getMockRequest().getRequestBody(),
                mockQuery.getMockRequest().getQueryParameters(),
                mockQuery.getMockRequest().getQueryParametersRegex()
        );

        ArrayList <Directory> pathList = Verifier.getPathList(
                mockQuery.getMockRequest().getMethod(),
                mockQuery.getMockRequest().getPath(),
                mockQuery.getMockRequest().getQueryParameters(),
                mockQuery.getMockRequest().getQueryParametersRegex()
        );

        if(mockQuery.getMockRequest().getMethod() == Method.POST ||
                mockQuery.getMockRequest().getMethod() == Method.PUT ||
                mockQuery.getMockRequest().getMethod() == Method.DEL) {
            JSONObject payloadBody = new JSONObject(mockQuery.getMockRequest().getRequestBody());
            TreeNode node = traverseAndAdd(pathList);
            PayloadResponse payloadResponse = new PayloadResponse(
                    mockQuery.getMockRequest().getCheckMode(),
                    payloadBody,
                    mockQuery.getMockResponse()
            );
            node.addAPayload(payloadResponse);
        }else {
            TreeNode trav = traverseAndAdd(pathList);
            trav.updateResponse(mockQuery.getMockResponse());
        }

    }


    /*##########################################################################################
    ####################################### DELETE REQUEST #####################################
    ##########################################################################################*/

    /**
     * Common helper function to traverse the URI tree and find the node having the corresponding response
     * The here simply strings are compared for equality, no pattern matching is required
     *
     * @param pathList The list of patters/simple directory names
     * @return the root having the response, no backtrack
     */
    private TreeNode traverseAndFind(ArrayList <Directory> pathList) {

        TreeNode trav = root;
        for(Directory dir : pathList) {
            logger.info("->" + trav.getName());
            if(dir instanceof DirName && trav.getChildNames().containsKey(dir.getDirName()))
                trav = trav.getChildNames().get(dir.getDirName());
            else if(dir instanceof DirPattern && trav.getChildPatterns().containsKey(dir.getDirName()))
                trav = trav.getChildPatterns().get(dir.getDirName());
            else return null;
        }

        return trav;
    }

    public void deleteMockRequest(DeleteMockRequest deleteMockRequest) {

        if(deleteMockRequest.getRequestBody() != null) {
            throw new IllegalArgumentException("This operation will clear all the data stored for " +
                    "this request. It does not need a payload body!");
        }

        Verifier.verifyMethodAndQuery(
                deleteMockRequest.getMethod(),
                null,
                deleteMockRequest.getQueryParameters(),
                deleteMockRequest.getQueryParametersRegex()
        );

        ArrayList <Directory> pathList = Verifier.getPathList(
                deleteMockRequest.getMethod(),
                deleteMockRequest.getPath(),
                deleteMockRequest.getQueryParameters(),
                deleteMockRequest.getQueryParametersRegex());

        TreeNode trav = traverseAndFind(pathList);
        if(trav == null) throw new IllegalArgumentException("No such path not exists!");
        trav.deleteAll();
    }

    public void deleteAPayloadResponse(DeleteMockRequest deleteMockRequest) {

        if(deleteMockRequest.getMethod() != Method.POST &&
                deleteMockRequest.getMethod() != Method.PUT &&
                deleteMockRequest.getMethod() != Method.DEL)
            throw new IllegalArgumentException("This Operation is only allowed for methods having a payload body(POST/PUT/DEL)!");

        JSONObject jsonObject = new JSONObject(deleteMockRequest.getRequestBody());

        Verifier.verifyMethodAndQuery(
                deleteMockRequest.getMethod(),
                deleteMockRequest.getRequestBody(),
                deleteMockRequest.getQueryParameters(),
                deleteMockRequest.getQueryParametersRegex()
        );

        ArrayList <Directory> pathList = Verifier.getPathList(
                deleteMockRequest.getMethod(),
                deleteMockRequest.getPath(),
                deleteMockRequest.getQueryParameters(),
                deleteMockRequest.getQueryParametersRegex());

        TreeNode trav = traverseAndFind(pathList);
        if(trav == null) throw new IllegalArgumentException("No such path not exists!");
        trav.deleteAPayload(jsonObject);
    }


    /*##########################################################################################
    ####################################### FIND RESPONSE  #####################################
    ##########################################################################################*/

    /**
     * Helper function for finding a path(may contain regex) matching a given path as a List of strings.
     * > First Match among the simple directory names first.
     * > Then Match among the regex linearly one by one
     * > backtrack if needed, since multiple paths are possible of a particular pathList (because of regex)
     *
     * @param trav     The current Tree Node
     * @param pathList The path list for which we need to find a match
     * @param id       The index in path list
     * @return The first node matching the path
     */

    private TreeNode findMatch(TreeNode trav, ArrayList <String> pathList, int id) {
        TreeNode node = trav.getChildNames().get(pathList.get(id));
        if(node != null) {
            if(id == pathList.size() - 1) {
                if(node.getLeaf() != null) return node;
            }else {
                TreeNode res = findMatch(node, pathList, id + 1);
                if(res != null) return res;
            }
        }
        for(Map.Entry <String, TreeNode> itr : trav.getChildPatterns().entrySet()) {
            if(itr.getValue().matches(pathList.get(id))) {
                if(id == pathList.size() - 1) {
                    if(itr.getValue().getLeaf() != null) return itr.getValue();
                }else {
                    TreeNode res = findMatch(itr.getValue(), pathList, id + 1);
                    if(res != null) return res;
                }
            }
        }
        return null;
    }

    public MockResponse postTypeResponse(ArrayList <String> pathList, JSONObject jsonObject)
            throws IllegalArgumentException, ValidationException {
        TreeNode node = findMatch(root, pathList, 0);
        if(node == null) throw new IllegalArgumentException("Directory with this path and payload does not exists!");
        return node.getResponse(jsonObject);
    }


    public MockResponse getTypeResponse(ArrayList <String> pathList) throws IllegalArgumentException {
        TreeNode node = findMatch(root, pathList, 0);
        if(node == null) throw new IllegalArgumentException("This path does not exists!");
        return node.getResponse();
    }


    /*##########################################################################################
    ##################################### ADD MOCK SCHEMA  #####################################
    ##########################################################################################*/

    public void addSchema(MockSchemaQuery mockSchemaQuery) {

        if(mockSchemaQuery.getMockSchema().getMethod() != Method.POST
                && mockSchemaQuery.getMockSchema().getMethod() != Method.DEL
                && mockSchemaQuery.getMockSchema().getMethod() != Method.PUT)
            throw new IllegalArgumentException(mockSchemaQuery.getMockSchema().getMethod().val +
                    " does not support schema checks");

        if(mockSchemaQuery.getMockSchema().getQueryParameters() != null &&
                mockSchemaQuery.getMockSchema().getQueryParametersRegex() != null)
            throw new IllegalArgumentException("Schema can not have both simple and regex query " +
                    "parameters at the same time");

        ArrayList <Directory> pathList = Verifier.getPathList(
                mockSchemaQuery.getMockSchema().getMethod(),
                mockSchemaQuery.getMockSchema().getPath(),
                mockSchemaQuery.getMockSchema().getQueryParameters(),
                mockSchemaQuery.getMockSchema().getQueryParametersRegex()
        );

        TreeNode trav = traverseAndAdd(pathList);
        Schema schema = SchemaLoader.load(new JSONObject(mockSchemaQuery.getMockSchema().getJsonSchema()));
        trav.addSchema(schema, mockSchemaQuery.getMockResponse());
    }


    public String getSchema(ArrayList <String> pathList) throws IllegalArgumentException {
        TreeNode node = findMatch(root, pathList, 0);
        if(node == null) throw new IllegalArgumentException("Path does not exists!");
        return node.getSchema();
    }

}