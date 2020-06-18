package com.mock.server.Server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mock.server.Query.Method;
import com.mock.server.Query.MockQuery;
import com.mock.server.Query.MockResponse;
import com.mock.server.Query.MockSchemaQuery;
import com.mock.server.RedisClient;
import com.mock.server.URITree.DirName;
import com.mock.server.URITree.Directory;
import com.mock.server.URITree.TreeNode;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

// Most of the verifications and the exceptions can be eliminated if we consider all the requests are comming from the slackBot
@Service
@Scope("prototype")
public class MockServer {

    private static final Logger logger = LoggerFactory.getLogger(MockServer.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final RedisClient redisClient;
    private final TreeNode root;
    private final Verifier verifier;
    private final PayloadsAndSchema payloadsAndSchema ;

    private String teamName;
    private Integer getTypeCounter;
    private Integer postTypeCounter;


    public MockServer(
            RedisClient redisClient,
            Verifier verifier,
            PayloadsAndSchema payloadsAndSchema) {

        root = new TreeNode(new DirName("ROOT"));
        this.redisClient = redisClient;
        this.verifier=verifier;
        this.payloadsAndSchema= payloadsAndSchema;

        getTypeCounter = 0;
        postTypeCounter = 0;
    }


    public void setTeamName(String teamName){
        this.teamName=teamName;
    }


    private String getTypeRedisKey(int counter){
        return "G "+teamName+" "+counter;
    }


    private String postTypeRedisKey(int counter){
        return "P "+teamName+" "+counter;
    }


    /**
     * Common Helper function for Verification for Adding/deleting the Mock Query
     * @param mockQuery a MockQuery
     * @param delete to delete a mockQuery or to Add a mockQuery or to Update a Mock Query
     * @throws JsonProcessingException if Payload is An Invalid JSON
     */
    public void updateMockQuery(MockQuery mockQuery, boolean delete) throws JsonProcessingException {

        verifier.verifyMethodAndQuery(
                mockQuery.getMockRequest().getMethod(),
                mockQuery.getMockRequest().getJsonBody(),
                mockQuery.getMockRequest().getQueryParameters(),
                mockQuery.getMockRequest().getQueryParametersRegex()
        );

        ArrayList <Directory> pathList = verifier.getPathList(
                mockQuery.getMockRequest().getMethod(),
                mockQuery.getMockRequest().getPath(),
                mockQuery.getMockRequest().getQueryParameters(),
                mockQuery.getMockRequest().getQueryParametersRegex()
        );

        if(mockQuery.getMockRequest().getMethod()== Method.POST ||
                mockQuery.getMockRequest().getMethod()==Method.PUT ||
                    mockQuery.getMockRequest().getMethod()==Method.DEL) {
            postTypeMockQuery(pathList,mockQuery,delete);
        } else {
            getTypeMockQuery(pathList,mockQuery,delete);
        }

    }



    /**
     * Common Helper Function for traversing the tree
     * @param pathList Path to find
     * @return The root node at which the redis Key exists
     */
    private TreeNode traverse(ArrayList<Directory> pathList){
        TreeNode trav = root;
        for(Directory dir : pathList) {
            if(!trav.getChildren().containsKey(dir.getDirName()))
                trav.addChild(dir.getDirName(), new TreeNode(dir));
            trav = trav.getChildren().get(dir.getDirName());
        }
        return trav;
    }



    /**
     * For Queries containing a payload
     * @param pathList The URI path to the root node
     * @param mockQuery MockQuery
     * @param delete is a delete OR add/update operation
     * @throws JsonProcessingException If Payload data is Invalid, or Schema does not match
     */
    private void postTypeMockQuery(ArrayList<Directory> pathList, MockQuery mockQuery,boolean delete) throws JsonProcessingException {

        JSONObject payloadBody = new JSONObject(mockQuery.getMockRequest().getJsonBody());
        TreeNode trav = traverse(pathList);

        if(trav.getId() == -1) {
            synchronized (trav) {
                if(trav.getId() == -1) {
                    postTypeCounter++;
                    trav.setId(postTypeCounter); // setId(int)
                }
            }
        }

        if(delete){
            payloadsAndSchema.deletePayload( trav.getId(), payloadBody );
            logger.info("Deleted Payload at "+trav.getId());
            return ;
        }

        int at = payloadsAndSchema.addPayload( trav.getId(), mockQuery.getMockRequest().getCheckMode(), payloadBody );
        String redisKey = postTypeRedisKey(at);

        String value = mapper.writeValueAsString(mockQuery.getMockResponse());
        redisClient.addVal(redisKey,value);
        logger.info("Payload List At: "+trav.getId()+"\nKey: "+redisKey+" Val: "+value);
    }


    // Queries without Payload
    private void getTypeMockQuery(ArrayList<Directory> pathList, MockQuery mockQuery, boolean delete) throws JsonProcessingException {
        TreeNode trav = traverse(pathList);

        if(delete){
            int key;
            synchronized (trav){
                if(trav.getId()==-1) return;
                key = trav.getId();
                trav.setId(-1);
            }
            redisClient.deleteKey("G"+key);
            logger.info("Deleted key "+"G"+key);
            return ;
        }

        if(trav.getId() == -1) {
            synchronized (trav) {
                if(trav.getId() == -1) {
                    getTypeCounter++;
                    trav.setId(getTypeCounter); // setId(int)
                }
            }
        }

        String key = getTypeRedisKey(trav.getId());
        String value = mapper.writeValueAsString(mockQuery.getMockResponse());
        redisClient.addVal(key,value);
        logger.info("Added to Redis: "+key+" : "+ value);
    }


    private TreeNode find(TreeNode trav, ArrayList <String> pathList, int id) {
        for(Map.Entry <String, TreeNode> itr : trav.getChildren().entrySet()) {
            if(itr.getValue().matches(pathList.get(id))) {
                if(id == pathList.size() - 1) {
                    return itr.getValue();
                }
                logger.info(itr.getKey()+" matched "+pathList.get(id));
                TreeNode res = find(itr.getValue(), pathList, id + 1);
                if(res != null) return res;
            }else{
                logger.info(itr.getValue().getName()+" does not matched "+pathList.get(id));
            }
        }
        return null;
    }

    // payloads and schema check
    public MockResponse postTypeResponse(ArrayList<String> pathList, JSONObject jsonObject) throws IllegalArgumentException, JsonProcessingException {
        logger.info("Post Matching Process Started");

        TreeNode sol = find(root, pathList, 0);
        if(sol == null) {
            throw new IllegalArgumentException("Directory matching the path does not exists!");
        }

        if(sol.getId() == -1) throw new IllegalArgumentException("Path does not exists!");

        // check if there exists such a payload
        int at = payloadsAndSchema.checkPayload(sol.getId(),jsonObject);
        // no exception => success
        logger.info("Payload array at "+sol.getId());
        logger.info("Payload at "+postTypeRedisKey(sol.getId()));

        // deserialize and return
        String res = redisClient.getVal(postTypeRedisKey(at));
        return mapper.readValue(res,MockResponse.class);
    }

    // no extra checks required
    public MockResponse getTypeResponse(ArrayList<String> pathList) throws IllegalArgumentException, JsonProcessingException {
        logger.info("........................");
        logger.info("Get Matching Process Started");

        TreeNode sol = find(root, pathList, 0);
        if(sol == null) {
            throw new IllegalArgumentException("Directory matching the path does not exists!");
        }

        if(sol.getId() == -1) throw new IllegalArgumentException("Path does not exists!");

        logger.info("Found at "+getTypeRedisKey(sol.getId()));
        String res = redisClient.getVal(getTypeRedisKey(sol.getId()));
        return mapper.readValue(res,MockResponse.class);
    }


    public void addSchema(MockSchemaQuery mockSchemaQuery){
        logger.info(".....................................");
        logger.info("Adding Schema.......................");

        if(mockSchemaQuery.getMethod()!=Method.POST
                && mockSchemaQuery.getMethod()!=Method.DEL
                && mockSchemaQuery.getMethod()!=Method.PUT )
            throw new IllegalArgumentException(mockSchemaQuery.getMethod().val+" does not support schema checks");

        if(mockSchemaQuery.getQueryParameters()!=null && mockSchemaQuery.getQueryParametersRegex()!=null)
            throw new IllegalArgumentException("Schema can not have both simple and regex query parameters at the same time");

        ArrayList <Directory> pathList = verifier.getPathList(
                mockSchemaQuery.getMethod(),
                mockSchemaQuery.getPath(),
                mockSchemaQuery.getQueryParameters(),
                mockSchemaQuery.getQueryParametersRegex()
        );

        TreeNode trav= traverse(pathList);

        if(trav.getId() == -1) {
            synchronized (trav) {
                if(trav.getId() == -1) {
                    postTypeCounter++;
                    trav.setId(postTypeCounter); // setId(int)
                }
            }
        }

        payloadsAndSchema.addSchema(
                trav.getId(),
                SchemaLoader.load(new JSONObject(mockSchemaQuery.getSchema()))
        );
        logger.info("Schema Added at key val P"+trav.getId());
    }


    public String getSchema(ArrayList<String> pathList) throws IllegalArgumentException {
        logger.info("........................");
        logger.info("Getting Schema");

        TreeNode sol = find(root, pathList, 0);
        if(sol == null) {
            throw new IllegalArgumentException("Path does not exists!");
        }
        if(sol.getId() == -1) throw new IllegalArgumentException("Path does not exists!");
        logger.info("GOT schema at "+sol.getId());
        return payloadsAndSchema.getSchema(sol.getId());
    }

}