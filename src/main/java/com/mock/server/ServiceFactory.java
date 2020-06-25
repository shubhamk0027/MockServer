package com.mock.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mock.server.Query.*;
import com.mock.server.Query.MockQuery.MockQuery;
import com.mock.server.Query.MockResponse;
import com.mock.server.Query.MockSchema.MockSchemaQuery;
import com.mock.server.Server.Team;
import com.mock.server.Server.MockServer;
import com.mock.server.Server.Verifier;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

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

    private final ObjectFactory <MockServer> mockServerFactory;
    private final ConcurrentHashMap <String, Team> keyTeamMap;
    private final ConcurrentHashMap <String, Team> nameTeamMap;

    private boolean isLoading;

    ServiceFactory(ObjectFactory <MockServer> mockServerFactory) throws IllegalAccessException {

        this.mockServerFactory = mockServerFactory;
        keyTeamMap = new ConcurrentHashMap <>();
        nameTeamMap = new ConcurrentHashMap <>();
        isLoading = true;

        try {
            logger.info("LOADING OLD DATA...");
            StopWatch watch = new StopWatch();
            watch.start();
            int total = loadOperations();
            watch.stop();
            logger.info("LOADED " + total + " OPERATIONS SUCCESSFULLY!");
            logger.info("TOTAL TIME TAKEN: " + watch.getTotalTimeMillis() + " ms");
        }catch(IllegalAccessException e) {
            logger.info("Error in reading the file Operations!");
            throw e;
        }catch(IOException e) {
            logger.info("No Log file found! No operations loaded!");
        }

        isLoading = false;
    }

    // Load operations from the appender/operations.log file
    public int loadOperations() throws IllegalAccessException, IOException {
        FileInputStream fstream = new FileInputStream("operations.log");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        int total = 0;

        String strLine;
        while((strLine = br.readLine()) != null) {
            if(strLine.charAt(0) == '+') {
                if(strLine.charAt(1) == 'T') createTeam(strLine.substring(3));
                else if(strLine.charAt(1) == 'M') addMockQuery(strLine.substring(3));
                else if(strLine.charAt(1) == 'S') addSchema(strLine.substring(3));
                else throw new IllegalStateException("Can't read the earlier operations!");
                total++;
            }else if(strLine.charAt(0) == '-') {
                if(strLine.charAt(1) == 'T') deleteTeam(strLine.substring(3));
                else if(strLine.charAt(1) == 'M') deleteMockQuery(strLine.substring(3));
                else deleteAPayload(strLine.substring(3));
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
     * @param body String of Type "ApiKey TeamName password" for a loading operation OR a JSON string of Create Team Query if not a loading operation
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
            String password = body.substring(i, body.length() - 1);
            MockServer mockServer = mockServerFactory.getObject();
            Team newTeam = new Team(apiKey, teamName, password, mockServer);
            nameTeamMap.put(teamName, newTeam);
            keyTeamMap.put(apiKey, newTeam);
            return apiKey;
        }

        TeamQuery teamQuery = mapper.readValue(body, TeamQuery.class);
        // teamName verification
        for(int i = 0; i < teamQuery.getTeamName().length(); i++) {
            if(i == 0 && !Character.isAlphabetic(teamQuery.getTeamName().charAt(i))) {
                throw new IllegalArgumentException("Team name should start with an alphabet!");
            }else if(!Character.isLetterOrDigit(teamQuery.getTeamName().charAt(i))) {
                throw new IllegalArgumentException("Team Name can not contain special characters!");
            }
        }

        if(nameTeamMap.containsKey(teamQuery.getTeamName()))
            throw new IllegalArgumentException("Team Name exists. Choose a different Team Name!");
        String apiKey = createKey(teamQuery.getTeamName());

        MockServer mockServer = mockServerFactory.getObject();
        Team newTeam = new Team(apiKey, teamQuery.getTeamName(), teamQuery.getPassword(), mockServer);
        nameTeamMap.put(teamQuery.getTeamName(), newTeam);
        keyTeamMap.put(apiKey, newTeam);
        appender.info("+T " + apiKey + " " + teamQuery.getTeamName() + " " + teamQuery.getPassword());
        return apiKey;
    }

    /**
     * Admin Operation 2-> Delete a team
     *
     * @param body JSON String of type DeleteQuery
     * @throws IllegalAccessException  Wrong Password
     * @throws JsonProcessingException Wrong Query
     */
    public void deleteTeam(String body) throws IllegalAccessException, JsonProcessingException {

        TeamQuery teamQuery = mapper.readValue(body, TeamQuery.class);

        if(!nameTeamMap.containsKey(teamQuery.getTeamName()))
            throw new IllegalArgumentException("Invalid Team Name!");

        if(!nameTeamMap.get(teamQuery.getTeamName()).getPassword().equals(teamQuery.getPassword())) {
            throw new IllegalAccessException("Wrong Password!");
        }

        keyTeamMap.remove(nameTeamMap.get(teamQuery.getTeamName()).getTeamKey());
        nameTeamMap.remove(teamQuery.getTeamName());

        if(!isLoading) appender.info("-T " + mapper.writeValueAsString(teamQuery));
    }

    /**
     * Admin Operation 3 ->  Get the Api Key
     *
     * @param body JSON string of Create Team Query
     * @return the Api key
     * @throws IllegalAccessException  Wrong Password
     * @throws JsonProcessingException Wrong Query Format
     */
    public String getApiKey(String body) throws IllegalAccessException, JsonProcessingException {

        TeamQuery teamQuery = mapper.readValue(body, TeamQuery.class);

        if(!nameTeamMap.containsKey(teamQuery.getTeamName()))
            throw new IllegalArgumentException("Invalid Team Name!");

        if(!nameTeamMap.get(teamQuery.getTeamName()).getPassword().equals(teamQuery.getPassword())) {
            throw new IllegalAccessException("Wrong Password!");
        }

        return nameTeamMap.get(teamQuery.getTeamName()).getTeamKey();
    }

    /**
     * Admin Operation 4-> Add a schema
     *
     * @param body JSON string of MockSchema
     * @throws JsonProcessingException Wrong Query
     * @throws IllegalAccessException  Wrong API key
     */
    public void addSchema(String body) throws JsonProcessingException, IllegalAccessException {

        MockSchemaQuery mockSchemaQuery = mapper.readValue(body, MockSchemaQuery.class);

        if(!keyTeamMap.containsKey(mockSchemaQuery.getMockSchema().getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");
        keyTeamMap.get(mockSchemaQuery.getMockSchema().getTeamKey()).getMockServer().addSchema(mockSchemaQuery);

        if(!isLoading) appender.info("+S " + mapper.writeValueAsString(mockSchemaQuery));
    }

    /**
     * Admin Operation 5-> Add a Mock Query
     *
     * @param body JSON string of Mock Query
     * @throws JsonProcessingException Wrong Query String
     * @throws IllegalAccessException  Wrong API key
     */
    public void addMockQuery(String body) throws JsonProcessingException, IllegalAccessException {

        MockQuery mockQuery = mapper.readValue(body, MockQuery.class);

        if(!keyTeamMap.containsKey(mockQuery.getMockRequest().getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");
        keyTeamMap.get(mockQuery.getMockRequest().getTeamKey()).getMockServer().addMockQuery(mockQuery);

        if(!isLoading) appender.info("+M " + mapper.writeValueAsString(mockQuery));
    }

    /**
     * Admin Operation 6-> Get the schema corresponding to a path
     *
     * @param body JSON String of GetSchemaQuery
     * @return Schema JSON as string
     * @throws JsonProcessingException Wrong Query String
     * @throws IllegalAccessException  Wrong API key
     */
    public String getSchema(String body) throws JsonProcessingException, IllegalAccessException {

        GetSchemaQuery getSchemaQuery = mapper.readValue(body, GetSchemaQuery.class);

        if(!keyTeamMap.containsKey(getSchemaQuery.getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");

        if(getSchemaQuery.getMethod() != Method.POST && getSchemaQuery.getMethod() != Method.PUT && getSchemaQuery.getMethod() != Method.DEL)
            throw new IllegalArgumentException("Schema is only associated with POST, PUT AND DEL query!");

        String path = getSchemaQuery.getPath();
        int i = 0;
        for(; i < path.length(); i++) if(path.charAt(i) == '?') break;
        ArrayList <String> pathList =
                Verifier.getSimplePathList(getSchemaQuery.getPath().substring(0, i), getSchemaQuery.getMethod().val);
        if(i < path.length() - 1) pathList.add(path.substring(i + 1)); // ignore '?'

        for(String s : pathList) logger.info("->" + s);
        return keyTeamMap.get(getSchemaQuery.getTeamKey()).getMockServer().getSchema(pathList);
    }

    /**
     * Admin Operation 7-> Delete a Mock Query
     *
     * @param body JSON string of type DeleteMockQuery
     * @throws JsonProcessingException Wrong Query String
     * @throws IllegalAccessException  Wrong API key
     */
    public void deleteMockQuery(String body) throws JsonProcessingException, IllegalAccessException {

        DeleteMockRequest deleteMockRequest = mapper.readValue(body, DeleteMockRequest.class);

        if(!keyTeamMap.containsKey(deleteMockRequest.getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");
        keyTeamMap.get(deleteMockRequest.getTeamKey()).getMockServer().deleteMockRequest(deleteMockRequest);

        if(!isLoading) appender.info("-M " + mapper.writeValueAsString(deleteMockRequest)); // compress and write
    }

    /**
     * Admin Operation 8-> Delete a Mock Query with corresponding payload match
     *
     * @param body JSON string of type DeleteMockQuery
     * @throws JsonProcessingException Wrong Query String
     * @throws IllegalAccessException  Wrong API key
     */
    public void deleteAPayload(String body) throws JsonProcessingException, IllegalAccessException {

        DeleteMockRequest deleteMockRequest = mapper.readValue(body, DeleteMockRequest.class);

        if(!keyTeamMap.containsKey(deleteMockRequest.getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");
        keyTeamMap.get(deleteMockRequest.getTeamKey()).getMockServer().deleteAPayloadResponse(deleteMockRequest);

        if(!isLoading) appender.info("-P " + mapper.writeValueAsString(deleteMockRequest)); // compress and write
    }

    /**
     * Fake Server Request Handler for POST Type Request
     *
     * @param key      API key
     * @param pathList Absolute path
     * @param body     payload JSON String
     * @return MockResponse at the path
     * @throws IllegalAccessException MockResponse does not exists or Wrong API Key
     */
    public MockResponse postTypeResponse(String key, ArrayList <String> pathList, String body) throws IllegalAccessException {

        logger.info("Received a Post request for key: " + key);

        if(!keyTeamMap.containsKey(key)) throw new IllegalAccessException("You seems to have a wrong API key!");
        return keyTeamMap.get(key).getMockServer().postTypeResponse(pathList, body);
    }

    /**
     * Fake Server Request Handler for GET Type Request
     *
     * @param key      API key
     * @param pathList Absolute path
     * @return MockResponse at the path
     * @throws IllegalAccessException MockResponse does not exists or Wrong API Key
     */
    public MockResponse getTypeResponse(String key, ArrayList <String> pathList) throws IllegalAccessException {

        logger.info("Received a GET request for key: " + key);

        if(!keyTeamMap.containsKey(key)) throw new IllegalAccessException("You seems to have a wrong API key!");
        return keyTeamMap.get(key).getMockServer().getTypeResponse(pathList);
    }


}
