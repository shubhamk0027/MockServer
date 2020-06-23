package com.mock.server.Servlet;

import com.mock.server.ServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.geom.IllegalPathStateException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


@Service
public class OperationsServlet extends HttpServlet {

    public static final Logger logger = LoggerFactory.getLogger(OperationsServlet.class);
    private final ServiceFactory serviceFactory;

    OperationsServlet(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }


    // .useDelimiter("\\A")
    // Now the scanner set as delimiter the [Regexp for \A][1]
    // \A stands for :start of a string!
    private String getBody(HttpServletRequest request) throws IOException {
        Scanner s = new Scanner(request.getInputStream(), StandardCharsets.UTF_8).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI().substring(8);
        try {
            String body = getBody(req);
            logger.info("Body Received:");
            logger.info(body);
            switch (uri) {
                case "_add/_schema": {
                    logger.info("Received Schema addition Request " + uri);
                    serviceFactory.addSchema(body);
                    resp.setStatus(200);
                    PrintWriter out = resp.getWriter();
                    out.write("Schema Added Successfully!");
                    break;
                }
                case "_add/_mock": {
                    logger.info("Received MockQuery addition Request " + uri);
                    serviceFactory.addMockQuery(body);
                    PrintWriter out = resp.getWriter();
                    out.write("MockQuery Added Successfully!");
                    break;
                }
                case "_get/_schema": {
                    logger.info("Received Schema get Request " + uri);
                    String response = serviceFactory.getSchema(body);
                    PrintWriter out = resp.getWriter();
                    out.write(response);
                    break;
                }
                case "_add/_team": {
                    logger.info("Received Create Team Request!");
                    String response = serviceFactory.createTeam(body);
                    PrintWriter out = resp.getWriter();
                    out.write("Your Team Key " + response);
                    break;
                }
                case "_get/_key": {
                    logger.info("Received Get Key Request!");
                    String response = serviceFactory.getApiKey(body);
                    PrintWriter out = resp.getWriter();
                    out.write("Your Team Key " + response);
                    break;
                }
                case "_del/_team": {
                    logger.info("Received Delete Team Request!");
                    serviceFactory.deleteTeam(body);
                    PrintWriter out = resp.getWriter();
                    out.write("Team Deleted Successfully!");
                    break;
                }
                case "_del/_mock": {
                    logger.info("Received Delete Mock Request!");
                    serviceFactory.deleteMockQuery(body);
                    PrintWriter out = resp.getWriter();
                    out.write("MockQuery/MockSchema Deleted Successfully!");
                    break;
                }
                case "_del/_payload": {
                    logger.info("Received Delete A PayloadResponse!");
                    serviceFactory.deleteAPayload(body);
                    PrintWriter out = resp.getWriter();
                    out.write("Payload And Response Deleted Successfully!");
                    break;
                }
                default:
                    throw new IllegalPathStateException("Path Is Invalid");
            }

        }catch(Exception e) {
            e.getStackTrace();
            logger.info(e.getMessage());
            PrintWriter out = resp.getWriter();
            out.println(e.getMessage());
            resp.setStatus(400);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(req.getMethod().equals("post") || req.getMethod().equals("POST")) super.service(req, resp);
        else {
            logger.info("This method is not supported");
            PrintWriter out = resp.getWriter();
            out.println("Invalid Request!");
            resp.setStatus(400);
        }
    }
}

//    Servlets are API but RESTful is not.
//    RESTful web service can use Servlets as there implementation but vice versa is not true.

/*
 * This servlet is quite reliable,
 * Default destroy method will be
 * Called by the servlet container to indicate to a servlet that the servlet is being taken out of service.
 * This method is only called once all threads within the servlet's service method have exited or after a
 * timeout period has passed. After the servlet container calls this method, it will not call the service
 * method again on this servlet.
 */