package com.mock.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mock.server.Query.CreateTeamQuery;
import com.mock.server.Query.Method;
import com.mock.server.Query.MockQuery.MockQuery;
import com.mock.server.Query.MockQuery.MockRequest;
import com.mock.server.Query.MockResponse;
import com.mock.server.Servlet.OperationsServlet;
import com.mock.server.Servlet.FakeServerServlet;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@SpringBootApplication
public class Application {

    private final OperationsServlet operationsServlet;
    private final FakeServerServlet fakeServerServlet;

    private final ServiceFactory serviceFactory;
    ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(Application.class);


    Application(
            ServiceFactory serviceFactory,
            OperationsServlet operationsServlet,
            FakeServerServlet fakeServerServlet) {
        this.serviceFactory=serviceFactory;
        this.operationsServlet = operationsServlet;
        this.fakeServerServlet = fakeServerServlet;
    }

    @Bean
    public ServletRegistrationBean <OperationsServlet> OperationsServletRegistrationBean() {
        return new ServletRegistrationBean <>(operationsServlet, "/_admin/*");
    }

    @Bean
    public ServletRegistrationBean <FakeServerServlet> FakeServerServletRegistrationBean() {
        return new ServletRegistrationBean <>(fakeServerServlet, "/*");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        logger.info("Mock Server Ready!");
    }

    public String memoryUsageTestGET(String teamKey, int numberOfRequests) throws JsonProcessingException, IllegalAccessException {

        int responseSize= 10240; // "0123456789"(len=10), byte Size = Approx (responseSize*10) Bytes
        int uniqueDirNames = 20;
        int dirInPaths = 5;

        Random random= new Random();
        ArrayList <String> dirNames = new ArrayList <>();
        for(int i=0;i<uniqueDirNames;i++) dirNames.add(Integer.toString(random.nextInt(100000000)));
        String responseBody = "0123456789".repeat(responseSize);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for(int i=0;i<numberOfRequests;i++){
            StringBuilder path = new StringBuilder();
            for(int j=0;j<dirInPaths;j++) {
                path.append('/');
                path.append(dirNames.get(random.nextInt(uniqueDirNames)));
            }
            MockQuery mockQuery = new MockQuery()
                    .inCase(new MockRequest()
                            .fromTeam(teamKey)
                            .hasMethod(Method.GET)
                            .hasPath(path.toString()))
                    .respondWith(new MockResponse()
                            .withResponseBody(responseBody)
                            .withStatusCode(200));
            serviceFactory.addMockQuery(mapper.writeValueAsString(mockQuery));
        }

        stopWatch.stop();
        return  "\nTEST FOR "+numberOfRequests+" GET REQUESTS EACH WITH  RESPONSE BODY OF "+(responseBody.getBytes().length/1024)+" KB"+
                "\nTEST RUN TIME: "+stopWatch.getTotalTimeMillis()+" ms"+
                "\nTOTAL MEMORY USAGE (GET TEST): "+(Runtime.getRuntime().totalMemory()/1024)/2014+" MB \n";
    }

    public String memoryUsageTestPOST(String teamKey, int numberOfRequests) throws JsonProcessingException, IllegalAccessException {

        int responseSize= 10240; // "0123456789"(len=10), Byte Size = Approx (responseSize*10) Bytes
        int uniqueDirNames = 20;
        int dirInPaths = 5;
        int numberOfPayloadKeyValPairs = 50;
        int numberOfUniquePayloadKeys = 200;

        Random random= new Random();
        ArrayList <String> dirNames = new ArrayList <>();
        for(int i=0;i<uniqueDirNames;i++) dirNames.add(Integer.toString(random.nextInt(100000000)));
        String responseBody = "0123456789".repeat(responseSize);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for(int i=0;i<numberOfRequests;i++){
            StringBuilder path = new StringBuilder();
            for(int j=0;j<dirInPaths;j++) {
                path.append('/');
                path.append(dirNames.get(random.nextInt(uniqueDirNames)));
            }

            Map <String,String> map = new HashMap <>();
            for(int j=0;j<numberOfPayloadKeyValPairs;j++)
                map.put("Payload "+random.nextInt(numberOfUniquePayloadKeys),"Payload ");

            MockQuery mockQuery = new MockQuery()
                    .inCase(new MockRequest()
                            .fromTeam(teamKey)
                            .hasMethod(Method.POST)
                            .hasRequestBody(mapper.writeValueAsString(map))
                            .hasPath(path.toString()))
                    .respondWith(new MockResponse()
                            .withResponseBody(responseBody)
                            .withStatusCode(200));
            serviceFactory.addMockQuery(mapper.writeValueAsString(mockQuery));
        }
        stopWatch.stop();

        Map <String,String> samplePayload = new HashMap <>();
        for(int j=0;j<numberOfPayloadKeyValPairs;j++)
            samplePayload.put("Payload "+random.nextInt(numberOfUniquePayloadKeys),"Payload ");
        String payload =  mapper.writeValueAsString(samplePayload);

        return  "\nTEST FOR "+numberOfRequests+" POST REQUESTS EACH WITH RESPONSE BODY OF "+responseBody.getBytes().length/1024+" KB AND PAYLOAD SIZE OF "+payload.getBytes().length/1024+" KB"+
                "\nTOTAL RUN TIME: "+stopWatch.getTotalTimeMillis()+" ms" +
                "\nTOTAL MEMORY USAGE (GET+POST TEST): "+(Runtime.getRuntime().totalMemory()/1024)/2014+" MB\n";
    }


    void performanceTest() throws JsonProcessingException, IllegalAccessException {
            CreateTeamQuery createTeamQuery = new CreateTeamQuery();
            createTeamQuery.setAdminId("TestAdmin");
            createTeamQuery.setTeamName("TestTeam");
            String teamKey = serviceFactory.createTeam(mapper.writeValueAsString(createTeamQuery));
            int numberOfRequests = 1000;
            String statsGET = memoryUsageTestGET(teamKey,numberOfRequests);
            String statsPOST = memoryUsageTestPOST(teamKey,numberOfRequests);
            logger.info("RESULTS SUMMARY:\n"+statsGET+"\nINCLUDING THE MEMORY USAGE OF GET REQUESTS\n"+statsPOST+"\n\n");
    }


    @Bean
    public CommandLineRunner commandLineRunner(){
        return args -> {
//            performanceTest();
//            logger.info("After the test current Operations.log size = 206.6 MB");
        };
    }
/*
            RESULTS SUMMARY:

            TEST FOR 1000 GET REQUESTS EACH WITH  RESPONSE BODY OF 100 KB
            TEST RUN TIME: 3316 ms
            TOTAL MEMORY USAGE (GET TEST): 152 MB

            INCLUDING THE MEMORY USAGE OF GET REQUESTS

            TEST FOR 1000 POST REQUESTS EACH WITH RESPONSE BODY OF 100 KB AND PAYLOAD SIZE OF 1 KB
            TOTAL RUN TIME: 3306 ms
            TOTAL MEMORY USAGE (GET+POST TEST): 208 MB

            Operations.log SIZE 181 MB              // The persistence file
            LOADED 2001 OPERATIONS SUCCESSFULLY!    // After Restarting the Application
            TOTAL TIME TAKEN: 3591 ms
 */

}
