package com.mock.server;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

/**
 * Servlets run on multiple threads
 * therefore should be able to handle concurrent requests
 * and should support synchronization over the shared objects
 */

class Servlet extends HttpServlet {

    public static final Logger logger= LoggerFactory.getLogger(Servlet.class);
    private final MockServer mockServer;

    Servlet(MockServer mockServer){
        this.mockServer=mockServer;
    }

    private String getBody(HttpServletRequest request) throws IOException {
        Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private ArrayList<String> getPathList(String uri,String method){
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
                throw new IllegalStateException("Bad Request");
            }
        }
        if(stringBuilder.length()!=0){
            res.add(stringBuilder.toString());
        }
        return res;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            ArrayList<String> pathList = getPathList(req.getRequestURI(),Method.GET.val);

            String queryParameters=  req.getQueryString();
            if(queryParameters!=null && queryParameters.length()>1)
                pathList.add(queryParameters.substring(1)); // ignoring ?

            String body = getBody(req);
            logger.info("........................Servlet in Get Invoked!");
            logger.info("Path: "+pathList);
            RedisValue redisValue = mockServer.getResponse(pathList, new JSONObject(body));
            resp.setStatus(redisValue.status);
            for(Map.Entry<String,String> itr: redisValue.resHeaders.entrySet()){
                resp.addHeader(itr.getKey(),itr.getValue());
            }
            PrintWriter out = resp.getWriter();
            out.println(redisValue.resBody);

        } catch( Exception e){
            e.getStackTrace();
            resp.sendError(400,e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            ArrayList<String> pathList = getPathList(req.getRequestURI(),Method.POST.val);

            String queryParameters=  req.getQueryString();
            if(queryParameters!=null && queryParameters.length()>1)
                pathList.add(queryParameters.substring(1)); // ignoring ?

            String body = getBody(req);
            logger.info("........................Servlet in POST Invoked!");
            logger.info("Path: "+pathList);
            logger.info("Body: "+body);

            RedisValue redisValue = mockServer.getResponse(pathList, new JSONObject(body));
            logger.info("Retuned details: ");
            logger.info(""+redisValue.status);

            if(redisValue.resHeaders.size()>0) logger.info("Headers Returned: ");
            for(Map.Entry<String,String> itr: redisValue.resHeaders.entrySet()){
                resp.addHeader(itr.getKey(),itr.getValue());
                logger.info(itr.getKey()+":"+itr.getValue());
            }

            logger.info("Body: "+redisValue.resBody);
            PrintWriter out = resp.getWriter();
            out.println(redisValue.resBody);

        } catch( Exception e){
            e.getStackTrace();
            logger.info(e.getMessage());
            resp.sendError(400,e.getMessage());
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doHead(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doOptions(req, resp);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doTrace(req, resp);
    }

}

/**
 * https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/http/HttpServlet.html
 */