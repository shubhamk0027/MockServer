package com.mock.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Scope("prototype")
public class MockServer {

    private static final Logger logger = LoggerFactory.getLogger(MockServer.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private final RedisClient redisClient;
    private final TreeNode root;
    private final Verifier verifier;
    private final PayloadsAndSchema payloadsAndSchema ;

    private Integer getTypeCounter;
    private Integer postTypeCounter;

    MockServer(
            RedisClient redisClient,
            Verifier verifier,
            PayloadsAndSchema payloadsAndSchema) {

        logger.info("A new MockServer Created!");

        root = new TreeNode(new DirName("ROOT"));
        this.redisClient = redisClient;
        this.verifier=verifier;
        this.payloadsAndSchema= payloadsAndSchema;

        getTypeCounter = 0;
        postTypeCounter = 0;

    }

    // Make this operation atomic
    public void updateMockQuery(MockQuery mockQuery,boolean delete) throws JsonProcessingException {
        logger.info("...............................");
        logger.info("Adding to the URI tree");

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

        if(mockQuery.getMockRequest().getMethod()==Method.POST ||
                mockQuery.getMockRequest().getMethod()==Method.PUT ||
                    mockQuery.getMockRequest().getMethod()==Method.DEL) {
            postTypeMockQuery(pathList,mockQuery,delete);
        } else {
            getTypeMockQuery(pathList,mockQuery,delete);
        }

    }


    private TreeNode traverse(ArrayList<Directory> pathList){
        TreeNode trav = root;
        for(Directory dir : pathList) {
            if(!trav.getChildren().containsKey(dir.getDirName()))
                trav.addChild(dir.getDirName(), new TreeNode(dir));
            trav = trav.getChildren().get(dir.getDirName());
        }
        return trav;
    }

    // Queries with a request body
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
            payloadsAndSchema.deletePayload(
                    trav.getId(),
                    payloadBody
            );
            logger.info("Deleted Payload at "+trav.getId());
            return ;
        }

        int redisKey = payloadsAndSchema.addPayload(
                trav.getId(),
                mockQuery.getMockRequest().getCheckMode(),
                payloadBody
        );

        String key = "P"+redisKey;
        String value = mapper.writeValueAsString(mockQuery.getMockResponse());

        logger.info("Payload List At: "+trav.getId());
        logger.info("Key: "+key);
        logger.info("Val: "+value);

        redisClient.addVal(key,value);
        value = redisClient.getVal(key);
        logger.info("Added to Redis: "+key+" : "+ value);
    }


    // Queries with a request body
    private void getTypeMockQuery(ArrayList<Directory> pathList, MockQuery mockQuery, boolean delete) throws JsonProcessingException {

        TreeNode trav = traverse(pathList);

        if(delete){ // A delete operation
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


        String key = "G"+trav.getId();
        String value = mapper.writeValueAsString(mockQuery.getMockResponse());
        logger.info("Key: "+key);
        logger.info("Val: "+value);

        redisClient.addVal(key,value);
        value = redisClient.getVal(key);
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
        logger.info("........................");
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
        logger.info("Payload at P"+at);

        // deserialize and return
        String res = redisClient.getVal("P"+at);
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

        logger.info("Found at G"+ sol.getId());
        String res = redisClient.getVal("G"+sol.getId());
        return mapper.readValue(res,MockResponse.class);
    }


    public void addSchema(MockSchema mockSchema){
        logger.info(".....................................");
        logger.info("Adding Schema.......................");

        if(mockSchema.getMethod()!=Method.POST
                && mockSchema.getMethod()!=Method.DEL
                && mockSchema.getMethod()!=Method.PUT )
            throw new IllegalArgumentException(mockSchema.getMethod().val+" does not support schema checks");

        if(mockSchema.getQueryParameters()!=null && mockSchema.getQueryParametersRegex()!=null)
            throw new IllegalArgumentException("Schema can not have both simple and regex query parameters at the same time");

        ArrayList <Directory> pathList = verifier.getPathList(
                mockSchema.getMethod(),
                mockSchema.getPath(),
                mockSchema.getQueryParameters(),
                mockSchema.getQueryParametersRegex()
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
                SchemaLoader.load(new JSONObject(mockSchema.getSchema()))
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