package com.mock.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.geom.IllegalPathStateException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


@Service
class AdminServlet extends HttpServlet {

    public static final Logger logger= LoggerFactory.getLogger(Servlet.class);
    private final ServiceFactory serviceFactory;

    AdminServlet(ServiceFactory serviceFactory){
        this.serviceFactory=serviceFactory;
    }

    private String getBody(HttpServletRequest request) throws IOException {
        Scanner s = new Scanner(request.getInputStream(), StandardCharsets.UTF_8).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI().substring(8);
        try{
            String body = getBody(req);
            logger.info("Body Received:");
            logger.info(body);
            switch (uri) {
                case "_add/_schema": {
                    logger.info("Received Schema addition Request " + uri);
                    serviceFactory.addSchema(body);
                    resp.setStatus(200);
                    PrintWriter out = resp.getWriter();
                    out.write("Schema Updated Successfully");
                    break;
                }
                case "_add/_mock": {
                    logger.info("Received MockQuery addition Request " + uri);
                    serviceFactory.addMockQuery(body);
                    PrintWriter out = resp.getWriter();
                    out.write("MockQuery Added Successfully");
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
                    out.write(response);
                    break;
                }
                case "_get/_key":{
                    logger.info("Received Get Key Request!");
                    String response = serviceFactory.getApiKey(body);
                    PrintWriter out = resp.getWriter();
                    out.write(response);
                    break;
                }
                case "_del/_team":{
                    logger.info("Received Delete Team Request!");
                    serviceFactory.deleteTeam(body);
                    PrintWriter out = resp.getWriter();
                    out.write("Team Deleted Successfully!");
                    break;
                }
                case "_del/_mock":{
                    logger.info("Received Delete Mock Request!");
                    serviceFactory.deleteMockQuery(body);
                    PrintWriter out = resp.getWriter();
                    out.write("MockQuery Deleted Successfully!");
                    break;
                }
                default:
                    throw new IllegalPathStateException("Path not understood");
            }

        }catch(Exception e){
            e.getStackTrace();
            logger.info(e.getMessage());
            PrintWriter out = resp.getWriter();
            out.println(e.getMessage());
            resp.setStatus(400);
        }
    }

}
