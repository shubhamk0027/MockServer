package com.mock.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.swing.plaf.basic.BasicButtonUI;

@SpringBootApplication
public class MockServerApplication {

    MockServer mockServer;
    private static final Logger logger = LoggerFactory.getLogger(MockServerApplication.class);

    @Bean
    public ServletRegistrationBean <Servlet> servletRegistrationBean() {
        return new ServletRegistrationBean <>(
                new Servlet(mockServer), "/");
    }

    MockServerApplication(MockServer mockServer){
        this.mockServer=mockServer;
    }

    public static void main(String[] args) {
        SpringApplication.run(MockServerApplication.class, args);
    }

    // Add (specificString/pattern) -> Specific Http Response
    // Request -> Specific Http Response

    @Bean
    CommandLineRunner commandLineRunner(){
        return (args)-> {
            // GET MockReq after parsing string
            String json1 = "            {\n" +
                    "                    \"path\" :\"/products/cardboard\",\n" +
                    "                    \"method\":\"GET\",\n" +
                    "                    \"response\":{\n" +
                    "                        \"status\":200,\n" +
                    "                        \"body\":\"{\\\"price\\\":100}\"\n" +
                    "                    }\n" +
                    "            }\n";
            mockServer.add(json1);

            // directly build from a mockQuery
            mockServer.addMockQuery(
                    new MockQuery.Builder()
                            .setMethod(MockQuery.Method.GET)
                            .setPath("/products/smartphones/")
                            .setResponseBody("{\"price\":10000}")
                            .setResponseCode(200).build()
            );

            mockServer.addMockQuery(
                    new MockQuery.Builder()
                            .setMethod(MockQuery.Method.POST)
                            .setPath("/products/smartphones/")
                            .setResponseBody("{\"error\":\"price not it range!\"}")
                            .setResponseCode(200).build()
            );

            mockServer.addMockQuery(
                    new MockQuery.Builder()
                            .setMethod(MockQuery.Method.POST)
                            .setPath("/products/smartphones?price=100000&color=red")
                            .setResponseBody("{\"result\":\"success!\"}")
                            .setResponseCode(200).build()
            );

            mockServer.addMockQuery(
                    new MockQuery.Builder()
                            .setMethod(MockQuery.Method.GET)
                            .setPath("/products/smartphones/buy/iphone11pro")
                            .setResponseBody("{\"note\":\"stay in your limits!\"}")
                            .setResponseCode(200).build()
            );

            mockServer.addMockQuery(
                    new MockQuery.Builder()
                            .setMethod(MockQuery.Method.GET)
                            .setPath("/products/smartphones/*")
                            .setResponseBody("{\"price\":100000}")
                            .setResponseCode(200).build()
            );

            mockServer.addMockQuery(
                    new MockQuery.Builder()
                            .setMethod(MockQuery.Method.GET)
                            .setPath("/*/robots.txt")
                            .setResponseBody("{\"note\":\"Robots are not allowed on out site!\"}")
                            .setResponseCode(200).build()
            );
        };
    }

}
