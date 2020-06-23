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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;

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
        this.serviceFactory = serviceFactory;
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

        int responseSize = 10240;    // byte size = Approx responseSize*"0123456789".len bytes
        int uniqueDirNames = 1234567890;
        int dirInPaths = 5;
        String responseBody = "0123456789".repeat(responseSize);

        StopWatch stopWatch = new StopWatch();
        Random random = new Random();
        stopWatch.start();

        for(int i = 0; i < numberOfRequests; i++) {
            StringBuilder path = new StringBuilder();
            for(int j = 0; j < dirInPaths; j++) {
                path.append('/');
                path.append(random.nextInt(uniqueDirNames));
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

        return "\nFOR ADDING " + numberOfRequests + " GET REQUESTS EACH WITH RESPONSE BODY OF " + (responseBody.getBytes().length / 1024) + " KB" +
                "\nTOTAL RUN TIME: " + stopWatch.getTotalTimeMillis() + " ms" +
                "\nTOTAL MEMORY USAGE: " + (Runtime.getRuntime().totalMemory() / 1024) / 1024 + " MB \n";
    }

    public String memoryUsageTestPOST(String teamKey, int numberOfRequests) throws JsonProcessingException, IllegalAccessException {

        int responseSize = 10240;
        int uniqueDirNames = 1234567890;
        int dirInPaths = 5;
        int numberOfPayloadKeyValPairs = 50;
        String responseBody = "0123456789".repeat(responseSize);

        Map <String, String> samplePayload = new HashMap <>();
        for(int j = 0; j < numberOfPayloadKeyValPairs; j++)
            samplePayload.put("Payload" + j, "Payload");
        String payload = mapper.writeValueAsString(samplePayload);

        StopWatch stopWatch = new StopWatch();
        Random random = new Random();
        stopWatch.start();

        for(int i = 0; i < numberOfRequests; i++) {
            StringBuilder path = new StringBuilder();
            for(int j = 0; j < dirInPaths; j++) {
                path.append('/');
                path.append(random.nextInt(uniqueDirNames));
            }
            MockQuery mockQuery = new MockQuery()
                    .inCase(new MockRequest()
                            .fromTeam(teamKey)
                            .hasMethod(Method.POST)
                            .hasRequestBody(payload)
                            .hasPath(path.toString()))
                    .respondWith(new MockResponse()
                            .withResponseBody(responseBody)
                            .withStatusCode(200));
            serviceFactory.addMockQuery(mapper.writeValueAsString(mockQuery));
        }

        stopWatch.stop();

        return "\nFOR ADDING " + numberOfRequests + " POST REQUESTS EACH WITH RESPONSE BODY OF " +
                responseBody.getBytes().length / 1024 + " KB AND PAYLOAD SIZE OF " + payload.getBytes().length / 1024 + " KB" +
                "\nTOTAL RUN TIME: " + stopWatch.getTotalTimeMillis() + " ms" +
                "\nTOTAL MEMORY USAGE : " + (Runtime.getRuntime().totalMemory() / 1024) / 1024 + " MB\n";
    }


    String performanceTest() throws JsonProcessingException, IllegalAccessException {
        CreateTeamQuery createTeamQuery = new CreateTeamQuery();
        createTeamQuery.setAdminId("Test1" + "Admin");
        createTeamQuery.setTeamName("Test1");
        String teamKey = serviceFactory.createTeam(mapper.writeValueAsString(createTeamQuery));
        int numberOfRequests = 1000;
        String statsGET = memoryUsageTestGET(teamKey, numberOfRequests);
        String statsPOST = memoryUsageTestPOST(teamKey, numberOfRequests);
        return "RESULTS SUMMARY:\n" + statsGET + "\n" + statsPOST + "\n\n";
    }


    /*
    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            logger.info(performanceTest());
            logger.info("After the test current Operations.log size = 206.6 MB");
        };
    }
            RESULTS SUMMARY:

            FOR ADDING 1000 GET REQUESTS EACH WITH RESPONSE BODY OF 100 KB
            TOTAL RUN TIME: 2677 ms
            TOTAL MEMORY USAGE: 256 MB


            FOR ADDING 1000 POST REQUESTS EACH WITH RESPONSE BODY OF 100 KB AND PAYLOAD SIZE OF 1 KB
            TOTAL RUN TIME: 2943 ms
            TOTAL MEMORY USAGE : 370 MB

            Operations.log SIZE 206 MB              // The persistence file
            LOADED 2001 OPERATIONS SUCCESSFULLY!    // After Restarting the Application
            TOTAL TIME TAKEN: 4193 ms
    */

}
