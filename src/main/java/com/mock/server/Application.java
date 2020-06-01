package com.mock.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.io.File;

@SpringBootApplication
public class Application {

    MockServer mockServer;
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    Application(MockServer mockServer) {
        this.mockServer = mockServer;

    }


    @Bean
    public ServletRegistrationBean <AdminServlet> AdminservletRegistrationBean() {
        return new ServletRegistrationBean <>(
                new AdminServlet(mockServer), "/_admin/*");
    }

    @Bean
    public ServletRegistrationBean <Servlet> servletRegistrationBean() {
        return new ServletRegistrationBean <>(
                new Servlet(mockServer), "/*");
    }


    public static void main(String[] args) {
        logger.info("Greetings from MockServer!");
        SpringApplication.run(Application.class, args);
    }


    public void addQueries() throws JsonProcessingException {
                /*
            {
                "properties":{
                    "city": {
                        "type":"string"
                        },
                    "name":{
                        "type":"string"
                        },
                    "age":{
                        "type":"number"
                        }
                }
           }
           */

        // https://github.com/everit-org/json-schema
        // strict schema check allowed
        MockSchema mockSchema = new MockSchema()
                .setMethod(Method.POST)                                     // Methods that do not accept any payloads will give error!
                .setSchema("{\n" +
                        "                \"properties\":{\n" +
                        "                    \"city\": {\n" +
                        "                        \"type\":\"string\"\n" +
                        "                        },\n" +
                        "                    \"name\":{\n" +
                        "                        \"type\":\"string\"\n" +
                        "                        },\n" +
                        "                    \"age\":{\n" +
                        "                        \"type\":\"number\"\n" +
                        "                        }\n" +
                        "                }\n" +
                        "           }")
                .strictCheckMode(true)
                .setPath("/products/cardboard");

        mockServer.addSchema(mockSchema);

        MockQuery mockQuery;
        try {
            // will give error, age should be a number according to schema!
            mockQuery = new MockQuery().inCase(
                    new MockRequest()
                            .hasPath("/products/cardboard/")
                            .hasMethod(Method.POST)
                            .hasBody("{\"city\":\"chicago\",\"name\":\"jon doe\",\"age\":\"22\"}"))
                    .respondWith(
                            new MockResponse()
                                    .withBody("{\"Info\":\"Simple Post Request\"}")
                                    .withStatus(200)
                                    .withHeader("browser", "mozilla")
                    );
            mockQuery.log();
            mockServer.addMockQuery(mockQuery);
        }catch(Exception e){
            e.getStackTrace();
            logger.info(e.getMessage());
        }

        mockQuery = new MockQuery().inCase(
                new MockRequest()
                        .hasPath("/products/cardboard/")
                        .hasMethod(Method.POST)
                        .hasBody("{\"city\":\"chicago\",\"name\":\"jon doe\",\"age\":22}"))
                .respondWith(
                        new MockResponse()
                                .withBody("{\"Info\":\"Simple Post Request\"}")
                                .withStatus(200)
                                .withHeader("browser", "mozilla")
                );
        mockQuery.log();
        mockServer.addMockQuery(mockQuery);

        // Same post query but with a different payload
        mockQuery = new MockQuery().inCase(
                new MockRequest()
                        .hasPath("/products/cardboard/")
                        .hasMethod(Method.POST)
                        .hasBody("{\"city\":\"kolkata\",\"name\":\"bapu\",\"age\":95}"))
                .respondWith(
                        new MockResponse()
                                .withBody("{\"Info\":\"Simple Post Request\"}")
                                .withStatus(200)
                                .withHeader("browser", "firefox")
                );
        mockQuery.log();
        mockServer.addMockQuery(mockQuery);

        mockQuery = new MockQuery().inCase(
                new MockRequest()
                        .hasPath("/products/[a-zA-Z]+/")
                        .hasMethod(Method.POST)
                        .hasBody("{\"city\":\"chicago\",\"name\":\"jon doe\",\"age\":\"22\"}"))
                .respondWith(
                        new MockResponse()
                                .withBody("{\"Info\":\"Post request with regex returning two headers\"}")
                                .withStatus(200)
                                .withHeader("browser", "chrome")
                                .withHeader("at", "night")
                );
        mockQuery.log();
        mockServer.addMockQuery(mockQuery);

        mockQuery = new MockQuery().inCase(
                new MockRequest()
                        .hasPath("/products/pro[a-zA-Z]+/price")
                        .hasMethod(Method.GET))
                .respondWith(
                        new MockResponse()
                                .withBody("{\"Info\":\"Simple GET request with headers and regex\"}")
                                .withStatus(200)
                                .withHeader("browser", "mozilla")
                );
        mockQuery.log();
        mockServer.addMockQuery(mockQuery);

        mockQuery = new MockQuery().inCase(
                new MockRequest()
                        .hasPath("/products/pro[a-zA-Z]+/price")
                        .hasMethod(Method.GET)
                        .hasQueryParameters("?a=1&b=2"))
                .respondWith(
                        new MockResponse()
                                .withBody("{\"Info\":\"Simple GET request with multiple headers and query parameters\"}")
                                .withStatus(200)
                                .withHeader("browser", "mozilla")
                                .withHeader("time", "12:00 AM")
                );
        mockQuery.log();
        mockServer.addMockQuery(mockQuery);

        mockQuery = new MockQuery().inCase(
                new MockRequest()
                        .hasPath("/products/pro[a-zA-Z]+/price")
                        .hasMethod(Method.GET)
                        .hasQueryParametersRegex("?a=[0-9]&b=[0-9]"))
                .respondWith(
                        new MockResponse()
                                .withBody("{\"Info\":\"Simple GET request with multiple headers and regex in query parameters\"}")
                                .withStatus(200)
                                .withHeader("browser", "mozilla")
                                .withHeader("time", "12:00 AM")
                );
        mockQuery.log();
        mockServer.addMockQuery(mockQuery);

    }



    @Bean
    CommandLineRunner commandLineRunner() {
        return (args) -> {
            addQueries();
        };
    }

}
/*
 * https://github.com/everit-org/json-schema
 * body should be of Json Content Type only
 * path is relative
 */

/*
 * Adding a MockQuery
 * Dev
 *  ->MockQueryBuild (MockReq(+verification),MockResponse)
 *      ->add to URITreeAdd
 *          ->generateRedisKey and value(JsonBody+Response)
 *              -> to Redis
 *
 * FakeServe
 * HttpServletRequest
 *      ->simplePathList
 *          ->URITree match
 *              -> Generate Redis Key
 *                  -> get Val from redis
 *
 *
 *
 * https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpRequest.html
 * https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.html
 *
 * https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/http/HttpServletRequest.html
 * https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/http/HttpServletResponse.html
 */