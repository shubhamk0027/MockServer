package com.mock.server.URITree;

/*
A tree node may have a leaf
 The leaf can be-
      In case of GET type requests
          A simple MockResponse
      In case of POST type requests
          A Schema Validator ( jsonSchema and MockResponse ) - SchemaResponse
          An array of PayloadResponse ( requestBody and MockResponse ) - PayloadList
*/

public interface Leaf {

}
