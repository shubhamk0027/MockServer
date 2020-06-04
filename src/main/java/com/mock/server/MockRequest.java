package com.mock.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Form authentication types are ignored */
// https://docs.oracle.com/javaee/7/api/javax/json/JsonObject.html

public class MockRequest {

    private static final Logger logger = LoggerFactory.getLogger(MockRequest.class);

    private String teamKey;
    private Method method;
    private String path;
    private String jsonBody;
    private String queryParameters;
    private String queryParametersRegex;
    private boolean checkMode=false;
    // check mode will be ignored if method does not support payload


    public String getTeamKey(){ return teamKey; }
    public Method getMethod() {
        return method;
    }
    public String getJsonBody() {
        return jsonBody;
    }
    public String getQueryParameters() {
        return queryParameters;
    }
    public String getQueryParametersRegex() {
        return queryParametersRegex;
    }
    public String getPath() {
        return path;
    }
    public boolean getCheckMode(){ return  checkMode; }


    public MockRequest fromTeam(String teamKey){
        this.teamKey=teamKey;
        return this;
    }

    public MockRequest inCheckMode(boolean checkMode){
        this.checkMode= checkMode;
        return this;
    }

    public MockRequest hasMethod(Method method) {
        this.method = method;
        return this;
    }

    public MockRequest hasPath(String path){
        this.path=path;
        return this;
    }

    public MockRequest hasQueryParameters(String queryParameters) {
        this.queryParameters=queryParameters;
        return this;
    }

    public MockRequest hasQueryParametersRegex(String queryParametersRegex){
        this.queryParametersRegex=queryParametersRegex;
        return this;
    }

    public MockRequest hasBody(String jsonBody) {
        this.jsonBody = jsonBody;
        return this;
    }

}


/*
 * Data can be sent as in url as queryParameters, headers, and cookies
 * Data sent through the POST method will not be visible in the URL, as parameters are not sent along with the URI.
 * https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpRequest.html
 * data sent by GET method can be accessed using query parameters
 * POST method transfers information via HTTP headers
 * Information is encoded as described in case of GET method and put into a header
 * POST method does not have any restriction on data size to be sent
 *
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods
 *
 * POST         no restrictions
 * PUT          no restrictions
 * DELETE       no restrictions
 * GET          Request do not have body
 * TRACE        Request do not have body
 * OPTIONS      Request do not have body
 * HEAD         Request do not have body
 *
 * General Builder patterns are not therad safe!
 */
