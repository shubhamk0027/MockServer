package com.mock.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

// not public!
class Servlet extends HttpServlet {
    public static final Logger logger= LoggerFactory.getLogger(Servlet.class);
    private final MockServer mockServer;
    private final ObjectMapper mapper = new ObjectMapper();

    Servlet(MockServer mockServer){
        this.mockServer=mockServer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getRequestURI();
        logger.info("........................Servlet in Get Invoked! "+MockQuery.Method.GET.val+" "+path);
        MockResponse response;
        try {
            response = mockServer.getResponse(MockQuery.Method.GET.val,path);
        } catch(Exception e) {
            logger.info(e.getMessage());
            e.getStackTrace();
            logger.info("key do not exists!");
            resp.setStatus(200);
            resp.setContentType("text/plain");
            PrintWriter out = resp.getWriter();
            out.println("Key does not exists in redis!");
            return;
        }

        logger.info("Result From REDIS:"+response.body);
        resp.setStatus(response.status);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.println(response.body);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info(req.getQueryString());
        String path = req.getRequestURI();
        String queryString = req.getQueryString();
        if (queryString != null)  path = path+"?"+queryString;

        logger.info("........................Servlet in Post Invoked! "+MockQuery.Method.POST.val+" "+path);
        MockResponse response;
        try {
            response = mockServer.getResponse(MockQuery.Method.POST.val,path);
        } catch(Exception e) {
            logger.info(e.getMessage());
            e.getStackTrace();
            logger.info("key do not exists!");
            resp.setStatus(200);
            resp.setContentType("text/plain");
            PrintWriter out = resp.getWriter();
            out.println("Key does not exists in redis!");
            return;
        }

        logger.info("Result From REDIS:"+response.body);
        resp.setStatus(response.status);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.println(response.body);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }
}