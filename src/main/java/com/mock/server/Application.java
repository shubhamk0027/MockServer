package com.mock.server;

import com.mock.server.Servlet.OperationsServlet;
import com.mock.server.Servlet.FakeServerServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    private final OperationsServlet operationsServlet;
    private final FakeServerServlet fakeServerServlet;

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    Application( OperationsServlet operationsServlet, FakeServerServlet fakeServerServlet) {
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