package com.mock.server;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.rmi.MarshalException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(ServiceFactory.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Map <String,DevTeam> keyTeamMap;                  // key-> team
    private final Map <String,DevTeam> nameTeamMap;                 // teamName ->key

    private final Verifier verifier;
    private final RedisClient redisClient;
    private final Persistence persistence;
    private final int maxTries;
    private boolean isloading;
    private final int KEYLEN =64;

    private ServiceFactory(
            Verifier verifier,
            RedisClient redisClient,
            Persistence persistence,
            @Value("${maxTries}") int maxTries){

        this.verifier=verifier;
        this.redisClient=redisClient;
        this.persistence=persistence;
        this.maxTries=maxTries;

        keyTeamMap = new HashMap <>();
        nameTeamMap = new HashMap <>();
        isloading=true;

        logger.info("............................................................");
        for(int i=0;i<5;i++) logger.info("LOADING OLD DATA...");
        logger.info("............................................................");

        loadOperations();
        isloading=false;

        logger.info("............................................................");
        logger.info("OLD DATA LOADED!");
        logger.info("............................................................");

        Thread persistThread = new Thread(()->{
            try {
                logger.info("Persistence Requested");
                persistence.run();
            } catch(InterruptedException e) {
                e.printStackTrace();
                logger.info("Persistence Stopped!");
            }
        });
        persistThread.start();
    }

    // Load earlier operations
    public void loadOperations(){
        try{
            FileInputStream fstream = new FileInputStream("operations.log");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                if(strLine.charAt(0)=='I'); // and INFORMATION!
                else if(strLine.charAt(0)=='+'){
                    if(strLine.charAt(1)=='T') createTeam(strLine.substring(3));
                    else if(strLine.charAt(1)=='M') addMockQuery(strLine.substring(3));
                    else if(strLine.charAt(1)=='S') addSchema(strLine.substring(3));
                    else throw new IllegalStateException("Can't read the earlier operations!");
                }else {
                    if(strLine.charAt(1)=='T') deleteTeam(strLine.substring(3));
                    else if(strLine.charAt(2)=='M') deleteMockQuery(strLine.substring(3));
                }
            }
            fstream.close();
        } catch (Exception e) {
            logger.info("Error in reading the file! Cant load all of the old operations!");
        }
    }


    public String createKey(String teamName){
        byte[] bytes = new byte[KEYLEN/8];
        new SecureRandom().nextBytes(bytes);
        String key = DatatypeConverter.printHexBinary(bytes).toLowerCase();
        logger.info("Key generated for team: "+teamName+" = "+key);
        return key;
    }

    // admin functions
    public synchronized String createTeam(String body) throws JsonProcessingException, IllegalArgumentException {

        if(isloading){
            int i=0;
            for(;i<body.length();i++) if(body.charAt(i)==' ') {
                i++;
                break;
            }
            String apiKey=body.substring(0,i-1);
            int j=i;
            for(;i<body.length();i++) if(body.charAt(i)==' '){
                i++;
                break;
            }
            String teamName =body.substring(j,i-1);
            String adminId = body.substring(i,body.length()-1);
            MockServer mockServer = new MockServer(redisClient,verifier,new PayloadsAndSchema());
            DevTeam newDevTeam = new DevTeam(apiKey,teamName,adminId,mockServer);
            nameTeamMap.put(teamName,newDevTeam);
            keyTeamMap.put(apiKey,newDevTeam);
            return apiKey;
        }

        CreateTeamQuery  createTeamQuery = mapper.readValue(body,CreateTeamQuery.class);
        if(nameTeamMap.containsKey(createTeamQuery.teamName)) throw new IllegalArgumentException("Team Name exists. Choose a different Team Name!");
        String apiKey = createKey(createTeamQuery.teamName);

        MockServer mockServer = new MockServer(redisClient,verifier,new PayloadsAndSchema());
        DevTeam newDevTeam = new DevTeam(apiKey,createTeamQuery.teamName,createTeamQuery.adminId,mockServer);
        nameTeamMap.put(createTeamQuery.teamName,newDevTeam);
        keyTeamMap.put(apiKey,newDevTeam);

        for(int i=0;i<maxTries;i++){
            try{
                persistence.add("+T "+apiKey+" "+createTeamQuery.teamName+" "+createTeamQuery.adminId);
                break;
            }catch(InterruptedException e){
                e.getStackTrace();
            }
        }
        return apiKey;
    }

    public synchronized void deleteTeam(String body) throws IllegalAccessException, JsonProcessingException{
        DeleteTeamQuery  deleteTeamQuery = mapper.readValue(body,DeleteTeamQuery.class);

        if(!keyTeamMap.containsKey(deleteTeamQuery.key))
            throw new IllegalArgumentException("No Team exists with this key!");

        if(!keyTeamMap.get(deleteTeamQuery.key).getAdminId().equals(deleteTeamQuery.adminId))
            throw new IllegalAccessException("Only the Admin can delete a team!");

        nameTeamMap.remove(keyTeamMap.get(deleteTeamQuery.key).getTeamName());
        keyTeamMap.remove(deleteTeamQuery.key);

        if(!isloading) for(int i=0;i<maxTries;i++){
            try{
                persistence.add("-T "+mapper.writeValueAsString(deleteTeamQuery));
                break;
            }catch(InterruptedException e){
                e.getStackTrace();
            }
        }
    }

    public String getApiKey(String body) throws IllegalAccessException, JsonProcessingException {
        CreateTeamQuery  createTeamQuery = mapper.readValue(body,CreateTeamQuery.class);

        if(!nameTeamMap.containsKey(createTeamQuery.teamName))
            throw new IllegalArgumentException("This Team Name is invalid!");

        if(!nameTeamMap.get(createTeamQuery.teamName).getAdminId().equals(createTeamQuery.adminId)){
            logger.info("Actual "+nameTeamMap.get(createTeamQuery.teamName).getAdminId()+" but "+createTeamQuery.adminId);
            throw new IllegalAccessException("Only the Admin can have acess the api key!");
        }

        return nameTeamMap.get(createTeamQuery.teamName).getKey();
    }

    // developer team CRUD functions
    public void addSchema(String body) throws JsonProcessingException, IllegalAccessException  {
        MockSchema mockSchema = mapper.readValue(body,MockSchema.class);

        if(!keyTeamMap.containsKey(mockSchema.getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");

        keyTeamMap.get(mockSchema.getTeamKey()).getMockServer().addSchema(mockSchema);
        if(!isloading) for(int i=0;i<maxTries;i++){
            try{
                persistence.add("+S "+mapper.writeValueAsString(mockSchema));
                break;
            }catch(InterruptedException e){
                e.getStackTrace();
            }
        }
    }

    public String getSchema(String body) throws JsonProcessingException, IllegalAccessException {
        GetSchemaQuery getSchemaQuery = mapper.readValue(body,GetSchemaQuery.class);
        getSchemaQuery.log();

        if(!keyTeamMap.containsKey(getSchemaQuery.getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");

        if(getSchemaQuery.getMethod()!= Method.POST && getSchemaQuery.getMethod()!= Method.PUT && getSchemaQuery.getMethod()!= Method.DEL)
            throw new IllegalArgumentException("Schema is only associated with POST, PUT AND DEL query!");

        return keyTeamMap.get(getSchemaQuery.getTeamKey()).getMockServer().getSchema(
                verifier.getSimplePathList(getSchemaQuery.getPath(),getSchemaQuery.getMethod().val)
        );
    }

    public void addMockQuery(String body) throws JsonProcessingException, IllegalAccessException, InterruptedException {
        MockQuery mockQuery = mapper.readValue(body,MockQuery.class);
        mockQuery.log();

        if(!keyTeamMap.containsKey(mockQuery.getMockRequest().getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");

        keyTeamMap.get(mockQuery.getMockRequest().getTeamKey()).getMockServer().updateMockQuery(mockQuery,false);
        if(!isloading) for(int i=0;i<maxTries;i++){
            try{
                persistence.add("+M "+mapper.writeValueAsString(mockQuery));
                break;
            }catch(InterruptedException e){
                e.getStackTrace();
                throw e;
            }
        }
    }

    public void deleteMockQuery(String body) throws JsonProcessingException, IllegalAccessException {
        MockQuery mockQuery = mapper.readValue(body,MockQuery.class);
        mockQuery.log();

        if(!keyTeamMap.containsKey(mockQuery.getMockRequest().getTeamKey()))
            throw new IllegalAccessException("You seems to have a wrong API key!");

        keyTeamMap.get(mockQuery.getMockRequest().getTeamKey()).getMockServer().updateMockQuery(mockQuery,true);
        if(!isloading) for(int i=0;i<maxTries;i++){
            try{
                persistence.add("-M "+mapper.writeValueAsString(mockQuery)); // compress and write
                break;
            }catch(InterruptedException e){
                e.getStackTrace();
            }
        }
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
