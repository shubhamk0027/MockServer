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
import java.util.ArrayList;
import java.util.Scanner;


@Service
class AdminServlet extends HttpServlet {

    public static final Logger logger= LoggerFactory.getLogger(Servlet.class);
    private final MockServer mockServer;

    AdminServlet(MockServer mockServer){
        this.mockServer=mockServer;
    }

    private String getBody(HttpServletRequest request) throws IOException {
        Scanner s = new Scanner(request.getInputStream(), StandardCharsets.UTF_8).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private ArrayList <String> getSimplePathList(String uri, String method){
        ArrayList<String> res = new ArrayList <>();
        res.add(method);
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=1;i<uri.length();i++){
            if(uri.charAt(i)=='/'){
                res.add(stringBuilder.toString());
                stringBuilder.setLength(0);
            }else if(Character.isLetterOrDigit(uri.charAt(i))){
                stringBuilder.append(uri.charAt(i));
            }else{
                throw new IllegalStateException("Path invalid!");
            }
        }
        if(stringBuilder.length()!=0){
            res.add(stringBuilder.toString());
        }
        return res;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        try{
            String body = getBody(req);
            logger.info("Body Received:");
            logger.info(body);

            if(uri.equals("/_admin/_add/_schema")) {
                logger.info("Received Schema addition Request "+uri);
                mockServer.addSchema(body);
                resp.setStatus(200);
                PrintWriter out =  resp.getWriter();
                out.write("Schema Updated Successfully");
            }else if(uri.equals("/_admin/_add/_mock")){
                logger.info("Received MockQuery addition Request "+uri);
                mockServer.addMockQuery(body);
                PrintWriter out =  resp.getWriter();
                out.write("MockQuery Added Successfully");
            }else if(uri.equals("/_admin/_get/_schema")){
                logger.info("Received Schema get Request "+uri);
                String response = mockServer.getSchema(getSimplePathList(body,Method.POST.val));
                PrintWriter out =  resp.getWriter();
                out.write(response);
            }else throw new IllegalPathStateException("Path not understood");
        }catch(Exception e){
            e.getStackTrace();
            logger.info(e.getMessage());
            PrintWriter out = resp.getWriter();
            out.println(e.getMessage());
            resp.setStatus(400);
        }
    }

}
