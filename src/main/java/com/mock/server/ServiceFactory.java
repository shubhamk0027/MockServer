package com.mock.server;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(ServiceFactory.class);
    private static final Logger OperationLogger = LoggerFactory.getLogger(ServiceFactory.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Map <String,DevTeam> keyTeamMap;                  // key-> team
    private final Map <String,DevTeam> nameTeamMap;                 // teamName ->key

    private final Verifier verifier;
    private final RedisClient redisClient;

    private ServiceFactory(
            Verifier verifier,
            RedisClient redisClient){

        this.verifier=verifier;
        this.redisClient=redisClient;

        keyTeamMap = new HashMap <>();
        nameTeamMap = new HashMap <>();
    }

    public String createKey(String teamName){
        String key = "API-KEY-OF-"+teamName;
        logger.info("Key generated for team: "+teamName+" = "+key);
        return key;
    }

    // admin functions
    public synchronized void deleteTeam(String body) throws IllegalAccessException, JsonProcessingException {
        DeleteTeamQuery  deleteTeamQuery = mapper.readValue(body,DeleteTeamQuery.class);
        if(!keyTeamMap.containsKey(deleteTeamQuery.key))
            throw new IllegalArgumentException("No Team exists with this key!");
        if(!keyTeamMap.get(deleteTeamQuery.key).getAdminId().equals(deleteTeamQuery.adminId))
            throw new IllegalAccessException("Only the Admin can delete a team!");
        nameTeamMap.remove(keyTeamMap.get(deleteTeamQuery.key).getTeamName());
        keyTeamMap.remove(deleteTeamQuery.key);
    }

    public synchronized String createTeam(String body) throws JsonProcessingException, IllegalArgumentException {
        CreateTeamQuery  createTeamQuery = mapper.readValue(body,CreateTeamQuery.class);
        if(nameTeamMap.containsKey(createTeamQuery.teamName)) throw new IllegalArgumentException("Team Name exists. Choose a different Team Name!");
        String apiKey = createKey(createTeamQuery.teamName);
        MockServer mockServer = new MockServer(redisClient,verifier,new PayloadsAndSchema());
        DevTeam newDevTeam = new DevTeam(apiKey,createTeamQuery.teamName,createTeamQuery.adminId,mockServer);
        nameTeamMap.put(createTeamQuery.teamName,newDevTeam);
        keyTeamMap.put(apiKey,newDevTeam);
        return apiKey;
    }

    public String getApiKey(String body) throws IllegalAccessException, JsonProcessingException {
        CreateTeamQuery  createTeamQuery = mapper.readValue(body,CreateTeamQuery.class);
        if(!nameTeamMap.containsKey(createTeamQuery.teamName))
            throw new IllegalArgumentException("This Team Name is invalid!");
        if(!nameTeamMap.get(createTeamQuery.teamName).getAdminId().equals(createTeamQuery.adminId))
            throw new IllegalAccessException("Only the Admin can have acess the api key!");
        return nameTeamMap.get(createTeamQuery.teamName).getKey();
    }

    // developer team CRUD functions
    public void addSchema(String body) throws JsonProcessingException, IllegalAccessException  {
        MockSchema mockSchema = mapper.readValue(body,MockSchema.class);
        if(!keyTeamMap.containsKey(mockSchema.getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");
        keyTeamMap.get(mockSchema.getTeamKey()).getMockServer().addSchema(mockSchema);
    }

    public String getSchema(String body) throws JsonProcessingException, IllegalAccessException {
        GetSchemaQuery getSchemaQuery = mapper.readValue(body,GetSchemaQuery.class);
        getSchemaQuery.log();
        if(!keyTeamMap.containsKey(getSchemaQuery.getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");
        return keyTeamMap.get(getSchemaQuery.getTeamKey()).getMockServer().getSchema(
                verifier.getSimplePathList(getSchemaQuery.getPath(),Method.POST.val)
        );
    }

    public void addMockQuery(String body) throws JsonProcessingException, IllegalAccessException {
        MockQuery mockQuery = mapper.readValue(body,MockQuery.class);
        mockQuery.log();
        if(!keyTeamMap.containsKey(mockQuery.getMockRequest().getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");
        keyTeamMap.get(mockQuery.getMockRequest().getTeamKey()).getMockServer().updateMockQuery(mockQuery,false);
    }

    public void deleteMockQuery(String body) throws JsonProcessingException, IllegalAccessException {
        MockQuery mockQuery = mapper.readValue(body,MockQuery.class);
        mockQuery.log();
        if(!keyTeamMap.containsKey(mockQuery.getMockRequest().getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");
        keyTeamMap.get(mockQuery.getMockRequest().getTeamKey()).getMockServer().updateMockQuery(mockQuery,true);
    }

    // Fake Server functions
    public MockResponse postTypeResponse(String key, ArrayList<String> pathList,String body ) throws IllegalAccessException, JsonProcessingException {
        logger.info("Received a Post request for key: "+key);
        if(!keyTeamMap.containsKey(key)) throw new IllegalAccessException("You seems to have a wrong API key!");
        return keyTeamMap.get(key).getMockServer().postTypeResponse(pathList,new JSONObject(body));
    }

    public MockResponse getTypeResponse(String key, ArrayList<String> pathList) throws IllegalAccessException, JsonProcessingException {
        logger.info("Received a Post request for key: "+key);
        if(!keyTeamMap.containsKey(key)) throw new IllegalAccessException("You seems to have a wrong API key!");
        return keyTeamMap.get(key).getMockServer().getTypeResponse(pathList);
    }

}
