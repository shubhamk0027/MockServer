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


    private void getTypeResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            String method) throws Exception {

        ArrayList<String> pathList = getPathList(request.getRequestURI(),method);
        String queryParameters=  request.getQueryString();
        if(queryParameters!=null && queryParameters.length()>1) {
            pathList.add(queryParameters.substring(1)); // ignoring ?
        }

        logger.info("Path: "+pathList);
        RedisValue redisValue = mockServer.getTypeResponse(pathList);
        logger.info("Returned details: ");
        logger.info(""+redisValue.status);

        if(redisValue.resHeaders.size()>0) logger.info("Headers Returned: ");
        for(Map.Entry<String,String> itr: redisValue.resHeaders.entrySet()){
            response.addHeader(itr.getKey(),itr.getValue());
            logger.info(itr.getKey()+":"+itr.getValue());
        }

        logger.info("Body: "+redisValue.resBody);
        PrintWriter out = response.getWriter();
        out.println(redisValue.resBody);
    }

    private void postTypeResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            String method) throws Exception {

        ArrayList<String> pathList = getPathList(request.getRequestURI(),method);
        String queryParameters=  request.getQueryString();
        if(queryParameters!=null && queryParameters.length()>1)
            pathList.add(queryParameters.substring(1)); // ignoring ?

        String body = getBody(request);
        logger.info("Path: "+pathList);
        logger.info("Body: "+body);

        RedisValue redisValue = mockServer.postTypeResponse(pathList, new JSONObject(body));
        logger.info("Returned details: ");
        logger.info(""+redisValue.status);

        if(redisValue.resHeaders.size()>0) logger.info("Headers Returned: ");
        for(Map.Entry<String,String> itr: redisValue.resHeaders.entrySet()){
            response.addHeader(itr.getKey(),itr.getValue());
            logger.info(itr.getKey()+":"+itr.getValue());
        }

        logger.info("Body: "+redisValue.resBody);
        PrintWriter out = response.getWriter();
        out.println(redisValue.resBody);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            logger.info("........................Servlet in GET Invoked!");
            getTypeResponse(req,resp,Method.GET.val);
        } catch( Exception e){
            e.getStackTrace();
            logger.info(e.getMessage());
            resp.sendError(400,e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            logger.info("........................Servlet in POST Invoked!");
            postTypeResponse(req,resp,Method.POST.val);
        } catch( Exception e){
            e.getStackTrace();
            logger.info(e.getMessage());
            resp.sendError(400,e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            logger.info("........................Servlet in PUT Invoked!");
            postTypeResponse(req,resp,Method.PUT.val);
        } catch( Exception e){
            e.getStackTrace();
            logger.info(e.getMessage());
            resp.sendError(400,e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            logger.info("........................Servlet in DEL Invoked!");
            postTypeResponse(req,resp,Method.DEL.val);
        } catch( Exception e){
            e.getStackTrace();
            logger.info(e.getMessage());
            resp.sendError(400,e.getMessage());
        }
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            logger.info("........................Servlet in HEAD Invoked!");
            getTypeResponse(req,resp,Method.HEAD.val);
        } catch( Exception e){
            e.getStackTrace();
            logger.info(e.getMessage());
            resp.sendError(400,e.getMessage());
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            logger.info("........................Servlet in OPTIONS Invoked!");
            getTypeResponse(req,resp,Method.OPTIONS.val);
        } catch( Exception e){
            e.getStackTrace();
            logger.info(e.getMessage());
            resp.sendError(400,e.getMessage());
        }
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            logger.info("........................Servlet in TRACE Invoked!");
            getTypeResponse(req,resp,Method.TRACE.val);
        } catch( Exception e){
            e.getStackTrace();
            logger.info(e.getMessage());
            resp.sendError(400,e.getMessage());
        }
    }

}

/*
 * https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/http/HttpServlet.html
 */