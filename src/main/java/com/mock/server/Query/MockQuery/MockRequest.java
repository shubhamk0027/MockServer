package com.mock.server.Query.MockQuery;

import com.mock.server.Query.Method;

// https://github.com/skyscreamer/JSONassert
// STRICT MODE which matches all fields order of arrays and no additional fields allowed, and
// ONLY_MATCHING_FIELDS/LENIENT which only matches fields provided in the request matcher

public class MockRequest {

    private String teamKey;                 // Team Secret API Key
    private Method method;                  // POST PUT DEL GET HEAD TRACE OPTIONS
    private String path;                    // some/simple/or/[a-zA-Z0-9]+/path/with/regex
    private String requestBody;             // The PayloadResponse with the request, will be for if GET HEAD TRACE OPTIONS
    private String queryParameters;         // ?simple=query&parameter=(s)
    private String queryParametersRegex;    // ?complex=[a-zA-Z]+&query=parameters[0-9]+
    private boolean checkMode = false;      // PayLoad matching to be STRICT(true) or LENIENT(false)

    public MockRequest() {

    }

    public String getTeamKey() {
        return teamKey;
    }

    public Method getMethod() {
        return method;
    }

    public String getRequestBody() {
        return requestBody;
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

    public boolean getCheckMode() {
        return checkMode;
    }

    public MockRequest fromTeam(String teamKey) {
        this.teamKey = teamKey;
        return this;
    }

    public MockRequest inCheckMode(boolean checkMode) {
        this.checkMode = checkMode;
        return this;
    }

    public MockRequest hasMethod(Method method) {
        this.method = method;
        return this;
    }

    public MockRequest hasPath(String path) {
        this.path = path;
        return this;
    }

    public MockRequest hasQueryParameters(String queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public MockRequest hasQueryParametersRegex(String queryParametersRegex) {
        this.queryParametersRegex = queryParametersRegex;
        return this;
    }

    public MockRequest hasRequestBody(String jsonBody) {
        this.requestBody = jsonBody;
        return this;
    }

}


/*
 * Few general things about the different type of http requests
 *
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
 *
 */
