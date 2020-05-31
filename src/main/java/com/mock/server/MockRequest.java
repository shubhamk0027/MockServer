package com.mock.server;


import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Form authentication types are ignored */
// https://docs.oracle.com/javaee/7/api/javax/json/JsonObject.html

public class MockRequest {

    public static final Logger logger = LoggerFactory.getLogger(MockRequest.class);

    private Method method;
    private String path;
    private JSONObject jsonBody;
    private String queryParameters;
    private String queryParametersRegex;

    public Method getMethod() {
        return method;
    }

    public JSONObject getJsonBody() {
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

    // verified in the MockQuery
    public MockRequest hasMethod(Method method) {
        this.method = method;
        return this;
    }

    public MockRequest hasPath(String path){
        this.path=path;
        return this;
    }

    public MockRequest hasQueryParameters(String queryParameters) {
        if(queryParameters==null || queryParameters.length()<2 || queryParameters.charAt(0)!='?')
            throw new IllegalStateException("Invalid Query Parameter Assignment!");
        this.queryParameters=queryParameters;
        return this;
    }

    public MockRequest hasQueryParametersRegex(String queryParametersRegex){
        if(queryParametersRegex==null || queryParametersRegex.length()<2 || queryParametersRegex.charAt(0)!='?')
            throw new IllegalStateException("Invalid Query Parameter Assignment!");
        this.queryParametersRegex=queryParametersRegex;
        return this;
    }

    public MockRequest hasBody(JSONObject jsonBody) {
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
