package com.mock.server.Query;

/**
 * Supports only methods supported by servlet request
 * CONNECT method is not supported
 * PATCH method is not supported
 */
public enum Method {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DEL("DEL"),
        HEAD("HEAD"),
        OPTIONS("OPTIONS"),
        TRACE("TRACE");

        public String val;
        Method (String val) { this.val= val; }
}

