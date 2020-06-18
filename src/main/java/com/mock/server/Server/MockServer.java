package com.mock.server.Server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mock.server.Query.*;
import com.mock.server.RedisClient;
import com.mock.server.URITree.DirName;
import com.mock.server.URITree.DirPattern;
import com.mock.server.URITree.Directory;
import com.mock.server.URITree.TreeNode;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;

import java.util.*;


/**
 * Most of the verifications and the exceptions can be eliminated
 * if we consider all the requests are coming from
 * the slackBot and are verified!
 *
 * https://github.com/skyscreamer/JSONassert - STRICT which matches all fields,
 * order of arrays and no additional fields allowed, and ONLY_MATCHING_FIELDS which only matches fields provided in the request matcher
 *
 */


@Service
@Scope("prototype")
public class MockServer {

    private static final Logger logger = LoggerFactory.getLogger(MockServer.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final TreeNode root;
    private String teamName;
    private Integer getTypeCounter;

    private final ArrayList<PayloadSchema> schemas;        // -v
    private final ArrayList<PayloadBody> payloadBodies;      // +v

    public MockServer() {
        root = new TreeNode(new DirName("ROOT"));
        schemas= new ArrayList <>();
        payloadBodies = new ArrayList <>();
        getTypeCounter = 0;
    }

    public void setTeamName(String teamName){ this.teamName=teamName; }


    /* ************************************************* REDIS KEY STRUCTURE ******************8*************************************/

    private String getTypeRedisKey(int counter){  return teamName+" G "+counter; }

    private String postTypeRedisKey(int counter){ return teamName+" P "+counter; }




    /* ************************************************* ADDING MOCK QUERY ******************8*************************************/

    /**
     * Common Helper function for Verification for Adding the Mock Query
     * @param mockQuery a MockQuery
     * @throws JsonProcessingException if Payload is An Invalid JSON
     */
    public void addMockQuery(MockQuery mockQuery) throws JsonProcessingException {

        Verifier.verifyMethodAndQuery(
                mockQuery.getMockRequest().getMethod(),
                mockQuery.getMockRequest().getJsonBody(),
                mockQuery.getMockRequest().getQueryParameters(),
                mockQuery.getMockRequest().getQueryParametersRegex()
        );

        ArrayList <Directory> pathList = Verifier.getPathList(
                mockQuery.getMockRequest().getMethod(),
                mockQuery.getMockRequest().getPath(),
                mockQuery.getMockRequest().getQueryParameters(),
                mockQuery.getMockRequest().getQueryParametersRegex()
        );

        if(mockQuery.getMockRequest().getMethod()== Method.POST ||
                mockQuery.getMockRequest().getMethod()==Method.PUT ||
                    mockQuery.getMockRequest().getMethod()==Method.DEL) {
            addPostTypeMockQuery(pathList,mockQuery);
        } else {
            addGetTypeMockQuery(pathList,mockQuery);
        }

    }

    /**
     * Common Helper Function for traversing the URI TREE and add node if they do not exists
     * @param pathList Path to find
     * @return The root node at which the redis Key exists
     */
    private TreeNode traverseAndAdd(ArrayList<Directory> pathList){
        TreeNode trav = root;
        for(Directory dir : pathList) {
            if(dir instanceof DirName){             // add a name
                if(!trav.getChildNames().containsKey(dir.getDirName()))
                    trav.addChildName(dir.getDirName(), new TreeNode(dir));
                trav = trav.getChildNames().get(dir.getDirName());
            }else{                                  // add a pattern
                if(!trav.getChildPatterns().containsKey(dir.getDirName()))
                    trav.addChildPatters(dir.getDirName(), new TreeNode(dir));
                trav = trav.getChildPatterns().get(dir.getDirName());
            }
        }
        return trav;
    }


    /**
     * Add to redis, for Queries containing a payload ie post type requests
     * A path will lead to a Schema or will lead to JSON Response body to match with
     * @param pathList The URI path to the root node
     * @param mockQuery MockQuery
     * @throws JsonProcessingException If Payload data is Invalid, or Schema does not match
     */
    private void addPostTypeMockQuery(ArrayList<Directory> pathList, MockQuery mockQuery) throws JsonProcessingException {

        // before preceding, verify that the payload is a valid json object
        JSONObject payloadBody = new JSONObject(mockQuery.getMockRequest().getJsonBody());
        TreeNode trav = traverseAndAdd(pathList);

        String redisKey;
        synchronized (trav) { // since synchronization order is same always, will never be in deadlock!
            if(trav.getId() == 0) { // add payload
                synchronized (payloadBodies){
                    payloadBodies.add(new PayloadBody(mockQuery.getMockRequest().getCheckMode(),payloadBody));
                    trav.setId(payloadBodies.size());
                }
            }else if(trav.getId()>0){
                synchronized (payloadBodies){ // update payload
                    payloadBodies.get(trav.getId()-1).setCheckMode(mockQuery.getMockRequest().getCheckMode());
                    payloadBodies.get(trav.getId()-1).setPayloadBody(payloadBody);
                }
            }else{
                throw new IllegalArgumentException("A JSON SCHEMA exists at this path, to continue, delete that path and try adding the payload again!");
            }
            redisKey = postTypeRedisKey(trav.getId());
        }

        String value = mapper.writeValueAsString(mockQuery.getMockResponse());
        RedisClient.addVal(redisKey,value);
        logger.info("Added to redis: "+redisKey+" : "+value);
    }


    /**
     * Add to Redis for queries with out payloads
     * @param pathList The URI path on the tree to traverse
     * @param mockQuery The Mock query to be added
     * @throws JsonProcessingException Not expected to be thrown any time
     */
    private void addGetTypeMockQuery(ArrayList<Directory> pathList, MockQuery mockQuery) throws JsonProcessingException {
        TreeNode trav = traverseAndAdd(pathList);

        if(trav.getId() == 0) {
            synchronized (trav) { // will never be null
                if(trav.getId() == 0) {
                    getTypeCounter++;
                    trav.setId(getTypeCounter); // setId(int)
                }
            }
        }

        String key = getTypeRedisKey(trav.getId());
        String value = mapper.writeValueAsString(mockQuery.getMockResponse());
        RedisClient.addVal(key,value);
        logger.info("Added to Redis: "+key+" : "+ value);
    }




    /* ************************************************* DELETE MOCK REQUEST ******************8*************************************/

    /**
     * Common helper function to traverse the URI tree and find the node having the corresponding response
     * The here simple strings of the path are matched, no pattern matching is required
     * @param pathList The list of patters/simple directory names
     * @return the root having the response
     */
    private TreeNode traverseAndFind(ArrayList<Directory> pathList){
        TreeNode trav = root;
        for(Directory dir : pathList) {
            if(dir instanceof DirName && trav.getChildNames().containsKey(dir.getDirName()))
                trav = trav.getChildNames().get(dir.getDirName());
            else if(dir instanceof DirPattern && trav.getChildPatterns().containsKey(dir.getDirName()))
                trav = trav.getChildPatterns().get(dir.getDirName());
            else return  null;
        }
        return null;
    }

    /**
     * Common helper function for verifying the pathList and
     * set the node id to -1, and remove the key from redis.
     * The childNames and childPatters map of this node can still contain valid information!
     * concurrency is handled in the deletePayload method and in the empty method
     * @param deleteMockRequest the request to be deleted, simple path
     */
    public void deleteMockRequest(DeleteMockRequest deleteMockRequest){

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
                deleteMockRequest.getQueryParametersRegex()
        );

        TreeNode trav =  traverseAndFind(pathList);
        if(trav==null || trav.getId()==0 ) throw new IllegalArgumentException("No such path not exists");
        if(deleteMockRequest.getMethod()== Method.POST ||
                deleteMockRequest.getMethod()==Method.PUT ||
                deleteMockRequest.getMethod()==Method.DEL) {
            int key = trav.empty();
            RedisClient.deleteKey(postTypeRedisKey(key));
            logger.info("Deleted Key "+postTypeRedisKey(trav.getId()));
        } else {
            int key=trav.empty();
            RedisClient.deleteKey(getTypeRedisKey(key));
            logger.info("Deleted key "+getTypeRedisKey(key));
        }
    }



    /* ************************************************* FIND RESPONSE FOR MOCK REQUEST ******************8************************************/

    /**
     * Helper function for finding a path(may contain regex) matching a given path string as a List.
     * First Match among the simple directory names first.
     * Then Match among the regex linearly one by one
     * @param trav The current Tree Node
     * @param pathList The path list for which we need to find a match
     * @param id The index in path list
     * @return The first node matching the path
     */

    private TreeNode findMatch( TreeNode trav, ArrayList <String> pathList, int id) {
        TreeNode node =  trav.getChildNames().get(pathList.get(id));
        if(node!=null){
            if(id == pathList.size() - 1) return node;
            TreeNode res = findMatch(node, pathList, id + 1);
            if(res != null) return res;
        }
        for(Map.Entry <String, TreeNode> itr : trav.getChildPatterns().entrySet()) {
            if(itr.getValue().matches(pathList.get(id))) {
                if(id == pathList.size() - 1)  return itr.getValue();
                TreeNode res = findMatch(itr.getValue(), pathList, id + 1);
                if(res != null) return res;
            }
        }
        return null;
    }

    public MockResponse postTypeResponse(ArrayList<String> pathList, JSONObject jsonObject)
            throws JsonProcessingException,IllegalArgumentException,ValidationException {

        TreeNode node =  findMatch(root,pathList,0);
        if(node==null) throw new IllegalArgumentException("Directory with this path and payload does not exists");

        String res;
        synchronized (node){ // will never be null, as no value is deleted ever
            if(node.getId()==0) throw new IllegalArgumentException("Directory with this path and payload does not exists");
            if(node.getId()>0){
                // a payload exists, match it!
                payloadBodies.get(node.getId()-1).equals(jsonObject);
                logger.info("Payload at " + postTypeRedisKey(node.getId()));
                res = RedisClient.getVal(postTypeRedisKey(node.getId()));
            }else{
                // a schema exists! verify it!
                schemas.get(-node.getId()+1).verify(jsonObject);
                logger.info("Payload at " + postTypeRedisKey(node.getId()));
                res = RedisClient.getVal(postTypeRedisKey(node.getId()));
            }
        }

        return mapper.readValue(res, MockResponse.class);
    }


    public MockResponse getTypeResponse(ArrayList<String> pathList) throws JsonProcessingException, IllegalArgumentException {
        TreeNode node = findMatch(root,pathList,0);
        String res;
        synchronized (node){
            if(node.getId()==0) throw new IllegalArgumentException("This path does not exists!");
            res = RedisClient.getVal(postTypeRedisKey(node.getId()));
        }
        return mapper.readValue(res,MockResponse.class);
    }



    /* ************************************************* ADD A MOCK SCHEMA ******************8************************************/

    public void addSchema(MockSchemaQuery mockSchemaQuery){
        if(mockSchemaQuery.getMethod()!=Method.POST
                && mockSchemaQuery.getMethod()!=Method.DEL
                && mockSchemaQuery.getMethod()!=Method.PUT )
            throw new IllegalArgumentException(mockSchemaQuery.getMethod().val+" does not support schema checks");

        if(mockSchemaQuery.getQueryParameters()!=null && mockSchemaQuery.getQueryParametersRegex()!=null)
            throw new IllegalArgumentException("Schema can not have both simple and regex query parameters at the same time");

        ArrayList <Directory> pathList = Verifier.getPathList(
                mockSchemaQuery.getMethod(),
                mockSchemaQuery.getPath(),
                mockSchemaQuery.getQueryParameters(),
                mockSchemaQuery.getQueryParametersRegex()
        );

        TreeNode trav= traverseAndAdd(pathList);
        Schema schema = SchemaLoader.load(new JSONObject(mockSchemaQuery.getSchema()));

        synchronized (trav) {
            if(trav.getId() == 0) { // add schema
                schemas.add(new PayloadSchema(schema));
                trav.setId(-schemas.size());
            }else if(trav.getId()<0)  schemas.get(-trav.getId()+1).setSchema(schema); // update schema
            else throw new IllegalArgumentException("A Payload body is also attached to this path, try deleting that path and then continue");
        }

        logger.info("Schema Added at key val "+postTypeRedisKey(trav.getId()));
    }


    public String getSchema(ArrayList<String> pathList) throws IllegalArgumentException {
        TreeNode node = findMatch(root,pathList,0);
        if(node!=null) throw new IllegalArgumentException("Path does not exists!");
        Schema schema;
        synchronized (node){
            if(node.getId() >=0) throw new IllegalArgumentException("Path does not exists!");
            logger.info("GOT schema at "+node.getId());
            schema = schemas.get(-node.getId()+1).getSchema();
        }
        return schema.toString();
    }

}