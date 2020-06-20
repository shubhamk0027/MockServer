package com.mock.server.Servlet;

import com.mock.server.Query.Method;
import com.mock.server.Query.MockResponse;
import com.mock.server.ServiceFactory;
import com.mock.server.Server.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

/**
 * Servlets run on multiple threads therefore should be able to handle concurrent
 * requests and should support synchronization over the shared objects
 */

@Service
public class FakeServerServlet extends HttpServlet {

    public static final Logger logger = LoggerFactory.getLogger(FakeServerServlet.class);
    private final ServiceFactory serviceFactory;

    FakeServerServlet(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    // Extract the requestBody from the request
    private String getBody(HttpServletRequest request) throws IOException {
        Scanner s = new Scanner(request.getInputStream(), StandardCharsets.UTF_8).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    // Handle the GET type Requests (GET HEAD OPTIONS TRACE), ie requests without payloads
    private void getTypeResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            String method) throws IOException, IllegalAccessException, IllegalArgumentException {

        String uri = request.getRequestURI();
        int i = 1;
        for(; i < uri.length(); i++) if(uri.charAt(i) == '/') break;
        String key = uri.substring(1, i);
        uri = uri.substring(i);

        ArrayList <String> pathList = Verifier.getSimplePathList(uri, method);

        String queryParameters = request.getQueryString();
        if(queryParameters != null && queryParameters.length() > 0) {
            pathList.add(queryParameters);
        }

        logger.info("Path: " + pathList);
        MockResponse mockResponse = serviceFactory.getTypeResponse(key, pathList);
        logger.info("Returned details:" + mockResponse.getStatusCode());

        if(mockResponse.getHeaders().size() > 0) {
            logger.info("Headers Returned: ");
        }

        for(Map.Entry <String, String> itr : mockResponse.getHeaders().entrySet()) {
            response.addHeader(itr.getKey(), itr.getValue());
            logger.info(itr.getKey() + ":" + itr.getValue());
        }

        logger.info("ResponseBody: " + mockResponse.getResponseBody());
        PrintWriter out = response.getWriter();
        out.println(mockResponse.getResponseBody());
    }

    // Handle the POST type Requests (POST PUT DEL), ie requests with payloads
    private void postTypeResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            String method) throws IOException, IllegalAccessException, IllegalArgumentException {

        String uri = request.getRequestURI();
        int i = 1;
        for(; i < uri.length(); i++) if(uri.charAt(i) == '/') break;
        String key = uri.substring(1, i);
        uri = uri.substring(i);

        ArrayList <String> pathList = Verifier.getSimplePathList(uri, method);

        String queryParameters = request.getQueryString();
        if(queryParameters != null && queryParameters.length() > 0)
            pathList.add(queryParameters);

        String body = getBody(request);
        logger.info("Path: " + pathList +"\nBody: " + body);
        MockResponse mockResponse = serviceFactory.postTypeResponse(key, pathList, body);

        logger.info("Returned details: "+ mockResponse.getStatusCode());
        if(mockResponse.getHeaders().size() > 0) logger.info("Headers Returned: ");
        for(Map.Entry <String, String> itr : mockResponse.getHeaders().entrySet()) {
            response.addHeader(itr.getKey(), itr.getValue());
            logger.info(itr.getKey() + ":" + itr.getValue());
        }

        logger.info("ResponseBody: " + mockResponse.getResponseBody());
        PrintWriter out = response.getWriter();
        out.println(mockResponse.getResponseBody());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            getTypeResponse(req, resp, Method.GET.val);
        }catch(Exception e) {
            e.getStackTrace();
            logger.info(e.getMessage());
            PrintWriter out = resp.getWriter();
            out.println(e.getMessage());
            resp.setStatus(400);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            postTypeResponse(req, resp, Method.POST.val);
        }catch(Exception e) {
            e.getStackTrace();
            logger.info(e.getMessage());
            PrintWriter out = resp.getWriter();
            out.println(e.getMessage());
            resp.setStatus(400);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            postTypeResponse(req, resp, Method.PUT.val);
        }catch(Exception e) {
            e.getStackTrace();
            logger.info(e.getMessage());
            PrintWriter out = resp.getWriter();
            out.println(e.getMessage());
            resp.setStatus(400);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            postTypeResponse(req, resp, Method.DEL.val);
        }catch(Exception e) {
            e.getStackTrace();
            logger.info(e.getMessage());
            PrintWriter out = resp.getWriter();
            out.println(e.getMessage());
            resp.setStatus(400);
        }
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            getTypeResponse(req, resp, Method.HEAD.val);
        }catch(Exception e) {
            e.getStackTrace();
            logger.info(e.getMessage());
            PrintWriter out = resp.getWriter();
            out.println(e.getMessage());
            resp.setStatus(400);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            getTypeResponse(req, resp, Method.OPTIONS.val);
        }catch(Exception e) {
            e.getStackTrace();
            logger.info(e.getMessage());
            PrintWriter out = resp.getWriter();
            out.println(e.getMessage());
            resp.setStatus(400);
        }
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            getTypeResponse(req, resp, Method.TRACE.val);
        }catch(Exception e) {
            e.getStackTrace();
            logger.info(e.getMessage());
            PrintWriter out = resp.getWriter();
            out.println(e.getMessage());
            resp.setStatus(400);
        }
    }

}
