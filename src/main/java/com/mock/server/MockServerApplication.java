package com.mock.server;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MockServerApplication {

    MockServer mockServer;
    private static final Logger logger = LoggerFactory.getLogger(MockServerApplication.class);

    MockServerApplication(MockServer mockServer){
        this.mockServer=mockServer;
    }

    @Bean
    public ServletRegistrationBean <Servlet> servletRegistrationBean() {
        return new ServletRegistrationBean <>(
                new Servlet(mockServer), "/");
    }


    public static void main(String[] args) {
        SpringApplication.run(MockServerApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(){
        return (args)-> {
            MockQuery mockQuery;

            mockQuery = new MockQuery().inCase(
                    new MockRequest()
                            .hasPath("/products/cardboard/")
                            .hasMethod(Method.POST)
                            .hasBody( new JSONObject("{\"city\":\"chicago\",\"name\":\"jon doe\",\"age\":\"22\"}")))
                    .respondWith(
                            new MockResponse()
                                    .withBody(
                                            new JSONObject("{\"name\":\"John\",\"Home\":\"London\",\"Marital Status\":\"Developer\"}"))
                                    .withStatus(200)
                                    .withHeader("browser","mozilla")
            );
            mockQuery.log();
            mockServer.addMockQuery(mockQuery);


            mockQuery = new MockQuery().inCase(
                    new MockRequest()
                            .hasPath("/products/[a-b]/")
                            .hasMethod(Method.POST)
                            .hasBody( new JSONObject("{\"city\":\"chicago\",\"name\":\"jon doe\",\"age\":\"22\"}")))
                    .respondWith(
                            new MockResponse()
                                    .withBody(
                                            new JSONObject("{\"Info\":\"this one is on a regex path!\"}"))
                                    .withStatus(200)
                                    .withHeader("browser","chrome")
                    );
            mockQuery.log();
            mockServer.addMockQuery(mockQuery);

        };
    }

}

// this Response jsonBody will be converted to the string before inserting it to the redis
// before insertion it will go through a schema check
// these operations are not atomic,
// it is possible that path is inserted but the json schema does not match!

// jsonObject Comparator
// Json schema and check http://jsonassert.skyscreamer.org/

/**
 * body should be of Json Content Type only
 * path is relative
 * <p>
 * Need to be implemented-> Json Schema support allowed
 * Json verifier
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