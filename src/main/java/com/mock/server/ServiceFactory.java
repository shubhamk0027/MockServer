package com.mock.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mock.server.Query.*;
import com.mock.server.Server.DevTeam;
import com.mock.server.Server.MockServer;
import com.mock.server.Server.PayloadsAndSchema;
import com.mock.server.Server.Verifier;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/*
 Data is not deleted from redis in case of team deletion, since the path will simply not exists
 In case the same path and same team name are encountered again, they will be overwritten
 */
@Service
public class ServiceFactory {

    // Persistence via Appender
    private static final Logger appender = LoggerFactory.getLogger("OperationsLogger");
    private static final Logger logger = LoggerFactory.getLogger(ServiceFactory.class);

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int KEY_LEN = 64;

    private final ObjectFactory<PayloadsAndSchema> payloadSchemaFactory ;
    private final ConcurrentHashMap <String, DevTeam> keyTeamMap;
    private final ConcurrentHashMap <String, DevTeam> nameTeamMap;

    private final Verifier verifier;
    private final RedisClient redisClient;
    private boolean isLoading;

    private ServiceFactory(
            Verifier verifier,
            RedisClient redisClient,
            ObjectFactory<PayloadsAndSchema> payloadSchemaFactory) throws IllegalAccessException, InterruptedException, IOException {

        this.verifier = verifier;
        this.redisClient = redisClient;
        this.payloadSchemaFactory=payloadSchemaFactory;

        keyTeamMap = new ConcurrentHashMap <>();
        nameTeamMap = new ConcurrentHashMap <>();
        isLoading = true;

        try {
            logger.info("LOADING OLD DATA...");
            int total = loadOperations();
            logger.info("LOADED "+total+" OPERATIONS SUCCESSFULLY!");
        }catch( IllegalAccessException | IOException e) {
            logger.info("Error in reading the file Operations!");
            throw e;
        }

        isLoading = false;

    }


    // Load operations from the appender/operations.log file
    public int  loadOperations() throws IllegalAccessException, IOException {
        FileInputStream fstream = new FileInputStream("operations.log");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine;
        int total=0;

        while((strLine = br.readLine()) != null) {
            if(strLine.charAt(0) == '+') {
                if(strLine.charAt(1) == 'T') createTeam(strLine.substring(3));
                else if(strLine.charAt(1) == 'M') addMockQuery(strLine.substring(3));
                else if(strLine.charAt(1) == 'S') addSchema(strLine.substring(3));
                else throw new IllegalStateException("Can't read the earlier operations!");
                total++;
            }else{
                if(strLine.charAt(1) == 'T') deleteTeam(strLine.substring(3));
                else deleteMockQuery(strLine.substring(3));
                total++;
            }
        }
        fstream.close();


        return total;
    }


    // since the number of teams will be limited, api key generated will be unique
    public String createKey(String teamName) {
        byte[] bytes = new byte[KEY_LEN / 8];
        new SecureRandom().nextBytes(bytes);
        String key = DatatypeConverter.printHexBinary(bytes).toLowerCase();
        logger.info("Key generated for team: " + teamName + " = " + key);
        return key;
    }


    /**
     * Admin Function Operation 1 Create Team Query
     *
     * @param body String of Type "ApiKey TeamName adminId" for a loading operation OR a JSON string of Create Team Query if not a loading operation
     * @return the apiKey
     * @throws JsonProcessingException  Wrong Json Query
     * @throws IllegalArgumentException Team Name is Exists
     */
    public String createTeam(String body) throws JsonProcessingException, IllegalArgumentException {
        if(isLoading) {
            int i = 0;
            for(; i < body.length(); i++)
                if(body.charAt(i) == ' ') {
                    i++;
                    break;
                }
            String apiKey = body.substring(0, i - 1);
            int j = i;
            for(; i < body.length(); i++)
                if(body.charAt(i) == ' ') {
                    i++;
                    break;
                }
            String teamName = body.substring(j, i - 1);
            String adminId = body.substring(i, body.length() - 1);
            MockServer mockServer = new MockServer(redisClient, verifier, payloadSchemaFactory.getObject());
            mockServer.setTeamName(teamName);
            DevTeam newDevTeam = new DevTeam(apiKey, teamName, adminId, mockServer);
            nameTeamMap.put(teamName, newDevTeam);
            keyTeamMap.put(apiKey, newDevTeam);
            return apiKey;
        }

        CreateTeamQuery createTeamQuery = mapper.readValue(body, CreateTeamQuery.class);
        if(nameTeamMap.containsKey(createTeamQuery.getTeamName()))
            throw new IllegalArgumentException("Team Name exists. Choose a different Team Name!");
        String apiKey = createKey(createTeamQuery.getTeamName());

        MockServer mockServer = new MockServer(redisClient, verifier, payloadSchemaFactory.getObject());
        mockServer.setTeamName(createTeamQuery.getTeamName());
        
        DevTeam newDevTeam = new DevTeam(apiKey, createTeamQuery.getTeamName(), createTeamQuery.getAdminId(), mockServer);
        nameTeamMap.put(createTeamQuery.getTeamName(), newDevTeam);
        keyTeamMap.put(apiKey, newDevTeam);
        appender.info("+T " + apiKey + " " + createTeamQuery.getTeamName() + " " + createTeamQuery.getAdminId());
        return apiKey;
    }


    /**
     * Admin Operation 2-> Delete a team
     * @param body JSON String of type DeleteQuery
     * @throws IllegalAccessException  Not an admin
     * @throws JsonProcessingException Wrong Query
     */
    public void deleteTeam(String body) throws IllegalAccessException, JsonProcessingException {

        DeleteTeamQuery deleteTeamQuery = mapper.readValue(body, DeleteTeamQuery.class);

        if(!keyTeamMap.containsKey(deleteTeamQuery.getTeamKey()))
            throw new IllegalArgumentException("No Team exists with this key!");

        if(!keyTeamMap.get(deleteTeamQuery.getTeamKey()).getAdminId().equals(deleteTeamQuery.getAdminId()))
            throw new IllegalAccessException("Only the Admin can delete a team!");

        nameTeamMap.remove(keyTeamMap.get(deleteTeamQuery.getTeamKey()).getTeamName());
        keyTeamMap.remove(deleteTeamQuery.getTeamKey());

        if(!isLoading) appender.info("-T " + mapper.writeValueAsString(deleteTeamQuery));
    }


    /**
     * Admin Operation 3 ->  Get the Api Key
     * @param body JSON string of Create Team Query
     * @return the Api key
     * @throws IllegalAccessException  Not an admin
     * @throws JsonProcessingException Wrong Query Format
     */
    public String getApiKey(String body) throws IllegalAccessException, JsonProcessingException {
        CreateTeamQuery createTeamQuery = mapper.readValue(body, CreateTeamQuery.class);

        if(!nameTeamMap.containsKey(createTeamQuery.getTeamName()))
            throw new IllegalArgumentException("This Team Name is invalid!");

        if(!nameTeamMap.get(createTeamQuery.getTeamName()).getAdminId().equals(createTeamQuery.getAdminId())) {
            logger.info("Actual " + nameTeamMap.get(createTeamQuery.getTeamName()).getAdminId() + " but " + createTeamQuery.getAdminId());
            throw new IllegalAccessException("Only the Admin can have acess the api key!");
        }

        return nameTeamMap.get(createTeamQuery.getTeamName()).getKey();
    }


    /**
     * Admin Operation 4-> Add a schema
     * @param body JSON string of MockSchema
     * @throws JsonProcessingException Wrong Query
     * @throws IllegalAccessException  Wrong API key
     */
    public void addSchema(String body) throws JsonProcessingException, IllegalAccessException {
        MockSchemaQuery mockSchemaQuery = mapper.readValue(body, MockSchemaQuery.class);
        if(!keyTeamMap.containsKey(mockSchemaQuery.getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");
        keyTeamMap.get(mockSchemaQuery.getTeamKey()).getMockServer().addSchema(mockSchemaQuery);
        if(!isLoading) appender.info("+S " + mapper.writeValueAsString(mockSchemaQuery));
    }


    /**
     * Admin Operation 5-> Add a Mock Query
     * @param body JSON string of Mock Query
     * @throws JsonProcessingException Wrong Query String
     * @throws IllegalAccessException  Wrong API key
     */
    public void addMockQuery(String body) throws JsonProcessingException, IllegalAccessException {
        MockQuery mockQuery = mapper.readValue(body, MockQuery.class);
        mockQuery.log();
        if(!keyTeamMap.containsKey(mockQuery.getMockRequest().getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");
        keyTeamMap.get(mockQuery.getMockRequest().getTeamKey()).getMockServer().updateMockQuery(mockQuery, false);
        if(!isLoading) appender.info("+M " + mapper.writeValueAsString(mockQuery));
    }


    /**
     * Admin Operation 6-> Get the schema corresponding to a path
     * @param body JSON String of GetSchemaQuery
     * @return Schema JSON as string
     * @throws JsonProcessingException Wrong Query String
     * @throws IllegalAccessException  Wrong API key
     */
    public String getSchema(String body) throws JsonProcessingException, IllegalAccessException {
        GetSchemaQuery getSchemaQuery = mapper.readValue(body, GetSchemaQuery.class);
        getSchemaQuery.log();

        if(!keyTeamMap.containsKey(getSchemaQuery.getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");

        if(getSchemaQuery.getMethod() != Method.POST && getSchemaQuery.getMethod() != Method.PUT && getSchemaQuery.getMethod() != Method.DEL)
            throw new IllegalArgumentException("Schema is only associated with POST, PUT AND DEL query!");

        return keyTeamMap.get(getSchemaQuery.getTeamKey()).getMockServer().getSchema(
                verifier.getSimplePathList(getSchemaQuery.getPath(), getSchemaQuery.getMethod().val)
        );
    }


    /**
     * Admin Operation 7-> Delete a Mock Query
     * @param body JSON string of type DeleteMockQuery
     * @throws JsonProcessingException Wrong Query String
     * @throws IllegalAccessException  Wrong API key
     */
    public void deleteMockQuery(String body) throws JsonProcessingException, IllegalAccessException {
        MockQuery mockQuery = mapper.readValue(body, MockQuery.class);
        mockQuery.log();
        if(!keyTeamMap.containsKey(mockQuery.getMockRequest().getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");
        keyTeamMap.get(mockQuery.getMockRequest().getTeamKey()).getMockServer().updateMockQuery(mockQuery, true);
        if(!isLoading) appender.info("-M " + mapper.writeValueAsString(mockQuery)); // compress and write
    }


    /**
     * Fake Server Request Handler for POST Type Request
     * @param key      API key
     * @param pathList Absoulte path
     * @param body     payload JSON String
     * @return MockResponse at the path
     * @throws IllegalAccessException  MockResponse does not exists or Wrong API Key
     * @throws JsonProcessingException Wrong JSON payload or path does not exists. Response body is returned as a string as it is.
     */
    public MockResponse postTypeResponse(String key, ArrayList <String> pathList, String body) throws IllegalAccessException, JsonProcessingException {
        logger.info("Received a Post request for key: " + key);
        if(!keyTeamMap.containsKey(key)) throw new IllegalAccessException("You seems to have a wrong API key!");
        return keyTeamMap.get(key).getMockServer().postTypeResponse(pathList, new JSONObject(body));
    }


    /**
     * Fake Server Request Handler for GET Type Request
     * @param key      API key
     * @param pathList Absolute path
     * @return MockResponse at the path
     * @throws IllegalAccessException  MockResponse does not exists or Wrong API Key
     * @throws JsonProcessingException Path does not exists. Response body is returned as a string as it is.
     */
    public MockResponse getTypeResponse(String key, ArrayList <String> pathList) throws IllegalAccessException, JsonProcessingException {
        logger.info("Received a Post request for key: " + key);
        if(!keyTeamMap.containsKey(key)) throw new IllegalAccessException("You seems to have a wrong API key!");
        return keyTeamMap.get(key).getMockServer().getTypeResponse(pathList);
    }

}
