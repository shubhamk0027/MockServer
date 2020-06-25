# MockServer
Have your team's development stopped because of the limits or the cost on the number of API calls you can make to a service? Or your team's backend service on which your application depends is not ready yet? Or maybe because the third party service on which your backend depends is not available?

MockServer comes to the rescue by helping you create a fake server that will send fake responses to your application for fastening the development process, and easing the testing process while verifying your http/https requests and sending the response you desire for that particular request.

## Contents
1. [Quick Setup](https://github.com/shubhamk0027/MockServer/#1-quick-setup)
2. [Using via SlackBot](https://github.com/shubhamk0027/MockServer/#2-using-via-slackbot)
3. [Using MockServer Without Slackbot](https://github.com/shubhamk0027/MockServer/#3-using-mockserver-without-slackbot)
     
     - [Create A New Team](https://github.com/shubhamk0027/MockServer/#create-a-new-team)
     - [Delete A Team](https://github.com/shubhamk0027/MockServer/#delete-a-team)
     - [Add Mock Query](https://github.com/shubhamk0027/MockServer/#add-mock-query)
     - [Delete A Payload Response for a Request](https://github.com/shubhamk0027/MockServer/#we-can-also-delete-a-particular-payload-response)
     - [Delete All Responses on Request](https://github.com/shubhamk0027/MockServer/#delete-all-the-payloads-present-at-this-mockrequest)
     - [Add a Schema](https://github.com/shubhamk0027/MockServer/#adding-the-schema-again)
     - [Get Schema](https://github.com/shubhamk0027/MockServer/#find-the-schema-present-at-this-path)
     - [Get Team Key](https://github.com/shubhamk0027/MockServer/#get-the-team-key)
     - [Adding Query Parameters](https://github.com/shubhamk0027/MockServer/#we-can-also-have-query-parameters-as)

4. [Performance](https://github.com/shubhamk0027/MockServer/#4-performance)

## 1. Quick Setup

First, run this mock server. By default it runs on localhost:8080.

After that, you can either send your queries directly or using the Slack bot. To use via Slack bot, run [slackbot](https://github.com/shubhamk0027/SlackBot) after adding bearer token and slack client token and the address where the mock server is running in the environment parameters as

        SLACK_BOT_TOKEN=FULL_TOKEN_HERE 
        SLACK_SIGNING_SECRET=SIGNING_SECRET_HERE 
        MOCK_SERVER=localhost:8080

Now you can send your add/delete mock queries to the mock server via the slackbot dialogs or via sending the json request without the slackbot directly. Whichever way you use, you can get the response set up by you for a particular request url, by sending that request to mockserver as 
        
        http//localhost:8080/TEAM-SECRET-KEY/your/path/here?any=query&parameters
        
with payloads if any. Change localhost:8080 to the address where your mockserver is running.


## 2. Using via SlackBot

See the slackbot in [action](https://drive.google.com/file/d/1sk_VV9kycOtOwtN3ikzgRYGEuvOwv5BT/view?usp=sharing) to understand how to use it. Simply run the following slash commands in the slack. A dialog box will appear asking for the Http request details and the corresponding response parameters to return. More details on what to fill in the input boxes of the dialog can be understood by understading the json the mock server accepts. See the examples in [Using Mockserver Without SlackBot](https://github.com/shubhamk0027/MockServer/#3-using-mockserver-without-slackbot).

#### /addteam

To create a new team. It will provide you with a secret team token. This secret team key will be used to send and add mock queries to this mock server.

#### /delteam

To delete a team. Only the admin, the user who created the team can delete a team.

#### /addschema

To add a schema support to the request body of the mock queries you will be adding up. Its just like a backend service rejecting the queries which are not according to the requested format. This is only for the http methods which support payloads (like PUT, POST and DEL)

#### /getschema

It will return you with the schema JSON at the path

#### /addmock 

To add a mock query which is a mock request with a mock response body. A mock request is the http request you will be sending to the mockserver, and Mock response is the response that the mockserver will give you in return.

#### /delmock

To delete a whole mockquery, ie delete all the responses you set earlier on a particular request url  

#### /delpayload

To delete one particular request url matching the given payload.

#### /getkey

To get the api key of your team. 

## 3. Using MockServer Without Slackbot
Send you queries as the server, with the appropriate JSON. The following examples explains all the operations and the accepted JSON format. 

#### Create a new team

After creating the team, you will be provided with the Team Secret API Key, that you can share with your team. And will be required while adding/deleting the mockqueries. 

        POST http://localhost:8080/_admin/_add/_team
        Content-Type: application/json

        {
          "teamName":"Team1",           // team username
          "password":"Password1"        // team secret password
        }

        ### Expected Response
        ### Your Team Key ca72678b69c3c1ed




#### Create another team with a different name

        POST http://localhost:8080/_admin/_add/_team
        Content-Type: application/json

        {
          "teamName":"Team2",
          "password":"Password2"
        }

        ### Expected Response
        ### Your Team Key 87879c21b7b3c632




#### Delete a team

        POST http://localhost:8080/_admin/_del/_team
        Content-Type: application/json

        {
          "teamName":"Team2",
          "password":"Password2"
        }

        ### Expected Response
        ### Team Deleted Successfully!




#### Add Mock Query 
Here, note that the checkMode is false(Lenient). It uses JSONAssert for matching the json requestBody. You can read more about it [here](https://github.com/skyscreamer/JSONassert). STRICT MODE matches all fields order of arrays and no additional fields allowed, and ONLY_MATCHING_FIELDS/LENIENT MODE which only matches fields provided in the request matcher

        POST http://localhost:8080/_admin/_add/_mock
        Content-Type: application/json

        {
                "mockRequest":{
                        "teamKey":"ca72678b69c3c1ed",
                        "method":"POST",
                        "path":"/items/electronics/[a-zA-Z]+/details",
                "checkMode": "false",
                        "requestBody":"{\"username\":\"alex\",\"userId\":1234567890}"
                },
                "mockResponse":{
                        "statusCode":200,
                        "responseBody":"{\"price\":\"20000\"}",
                        "headers":{
                                "browser":"chrome",
                    "Content-Type": "application/json"
                        }
                }
        }

        ### Expected Response
        ### MockQuery Added Successfully




#### Test Mock Query

        POST http://localhost:8080/ca72678b69c3c1ed/items/electronics/iphone/details
        Content-Type: application/json

        {
          "username":"alex",
          "userId":1234567890
        }

        ### Expected Response
        ### {"price": "20000"}




#### Test Mock Query, Is the checks lenient?

        POST http://localhost:8080/ca72678b69c3c1ed/items/electronics/iphone/details
        Content-Type: application/json

        {
          "userId":1234567890
        }

        ### Expected Response
        ### {
        ###  "price": "20000"
        ### }



#### Add another Mock Query on the same path but with a different payload

        POST http://localhost:8080/_admin/_add/_mock
        Content-Type: application/json

        {
                "mockRequest":{
                        "teamKey":"ca72678b69c3c1ed",
                        "method":"POST",
                        "path":"/items/electronics/[a-zA-Z]+/details",
                "checkMode": "true",
                        "requestBody":"{\"username\":\"missy\",\"userId\":1234567890}"
                },
                "mockResponse":{
                        "statusCode":200,
                        "responseBody":"{\"price\":\"20000\"}",
                        "headers":{
                                "browser":"chrome",
                    "Content-Type": "application/json"
                        }
                }
        }

        ### Expected Response
        ### MockQuery Added Successfully


#### Test MockQuery

        POST http://localhost:8080/ca72678b69c3c1ed/items/electronics/iphone/details
        Content-Type: application/json

        {
          "username":"missy",
          "userId":1234567890
        }

        ### Expected Response
        ### {
        ###  "price": "20000"
        ### }




#### We can also delete a particular payload response

        POST http://localhost:8080/_admin/_del/_payload
        Content-Type: application/json

        {
          "teamKey":"ca72678b69c3c1ed",
          "method":"POST",
          "path":"/items/electronics/[a-zA-Z]+/details",
          "requestBody":"{\"username\":\"missy\",\"userId\":1234567890}"
        }

        ### Expected Response
        ### PayloadResponse And Response Deleted Successfully!




#### Test the Delete MockRequest

        POST http://localhost:8080/ca72678b69c3c1ed/items/electronics/iphone/details
        Content-Type: application/json

        {
          "username":"missy",
          "userId":1234567890
        }

        ### Expected Response
        ### No matching payload found!




#### Replacing multiple payloads by adding a Mock Schema at the same path!

This is not allowed, a request supporting a payload can have either a schema check, or a List of Payloads to be matched with. You have to delete the payloads present at the old path and then add the schema.

        POST http://localhost:8080/_admin/_add/_schema
        Content-Type: application/json

        {
          "mockSchema":{
            "teamKey":"ca72678b69c3c1ed",
            "method":"POST",
            "jsonSchema":"{\"properties\":{\"username\":{\"type\":\"string\"},\"userId\":{\"type\":\"number\"}}}",
            "path":"/items/electronics/[a-zA-Z]+/details"
          },
          "mockResponse":{
            "statusCode":200,
            "responseBody":"{\"price\":\"2000\"}",
            "headers":{
              "browser":"mozilla"
            }
          }
        }

        ### Expected Response
        ### A PayloadResponse List is also attached to this path try deleting the payloadResponse List at his path and then continue!




#### Delete all the payloads present at this mockrequest.

        POST http://localhost:8080/_admin/_del/_mock
        Content-Type: application/json

        {
          "teamKey":"ca72678b69c3c1ed",
          "method":"POST",
          "path":"/items/electronics/[a-zA-Z]+/details"
        }

        ### Expected Response
        ### MockQuery/MockSchema Deleted Successfully!



#### Adding the schema again

        POST http://localhost:8080/_admin/_add/_schema
        Content-Type: application/json

        {
          "mockSchema":{
            "teamKey":"ca72678b69c3c1ed",
            "method":"POST",
            "jsonSchema":"{\"properties\":{\"username\":{\"type\":\"string\"},\"userId\":{\"type\":\"number\"}}}",
            "path":"/items/electronics/[a-zA-Z]+/details"
          },
          "mockResponse":{
            "statusCode":200,
            "responseBody":"{\"price\":\"2000\"}",
            "headers":{
              "browser":"mozilla"
            }
          }
        }

        ### Expected Response
        ### Schema Added Successfully!




#### Test the schema - Validation Error

        POST http://localhost:8080/ca72678b69c3c1ed/items/electronics/iphonePlus/details
        Content-Type: application/json

        {
          "username":"alex",
          "userId":"1234567890"
        }

        ### Expected Response
        ### #/userId: expected type: Number found: String




#### Test the schema - Successfull requestBody validation

        POST http://localhost:8080/ca72678b69c3c1ed/items/electronics/iphonePlus/details
        Content-Type: application/json

        {
          "username":"alex",
          "userId":1234567890
        }

        ### Expected Response
        ### {
        ### "price":"2000"
        ### }




#### Find the schema present at this path

        POST http://localhost:8080/_admin/_get/_schema
        Content-Type: application/json

        {
          "teamKey":"ca72678b69c3c1ed",
          "method":"POST",
          "path":"/items/electronics/iphone11/details"
        }

        ### Expected Response
        ### Path does not exists!
        ### As [a-zA-Z]+ does not expects numbers!




#### Find the schema present at this path

        POST http://localhost:8080/_admin/_get/_schema
        Content-Type: application/json

        {
          "teamKey":"ca72678b69c3c1ed",
          "method":"POST",
          "path":"/items/electronics/iphoneElevenPro/details"
        }

        ### Expected Response
        ### {"properties":{"userId":{"type":"number"},"username":{"type":"string"}}}




#### Get the team key

        POST http://localhost:8080/_admin/_get/_key
        Content-Type: application/json

        {
          "teamName":"Team1",
          "password":"Password1"
        }

        ### Expected Response
        ### Your Team Key ca72678b69c3c1ed




#### We can also have query parameters as

        POST http://localhost:8080/_admin/_add/_mock
        Content-Type: application/json

        {
          "mockRequest":{
            "teamKey":"ca72678b69c3c1ed",
            "method":"GET",
            "path":"/items/grocery/details",
            "queryParameters": "?name=alex"
          },
          "mockResponse":{
            "statusCode":200,
            "responseBody":"{\"price\":\"50000\"}",
            "headers":{
              "browser":"chrome",
              "Content-Type": "application/json"
            }
          }
        }

        ### Expected Response
        ### MockQuery Added Successfully!




#### Test the added Mock Query

        GET http://localhost:8080/ca72678b69c3c1ed/items/grocery/details?name=alex
        ### Expected Response
        ### {
        ###  "price": "50000"
        ### }



#### Using query parameters as a regex

        POST http://localhost:8080/_admin/_add/_mock
        Content-Type: application/json

        {
          "mockRequest":{
            "teamKey":"ca72678b69c3c1ed",
            "method":"GET",
            "path":"/items/grocery/details",
            "queryParametersRegex": "?name=[a-zA-Z]+&id=[0-9]+"
          },
          "mockResponse":{
            "statusCode":200,
            "responseBody":"{\"info\":\"All Set!\"}",
            "headers":{
              "browser":"chrome",
              "Content-Type": "application/json"
            }
          }
        }

        ### Expected Response
        ### MockQuery Added Successfully!




#### Test the added Mock Query

        GET http://localhost:8080/ca72678b69c3c1ed/items/grocery/details?name=missy&id=123
        ### Expected Response
        ### {
        ###  "info": "All Set!"
        ### }




In case a path is having multiple responses (because of use regular expressions)
The directory without any regular expression will be matched first then the one with regular expressions
Ex: simple/path/here will be matched first then simple/[a-zA-Z]+/path
  
          
## 4. Performance

How the tests are generated is shown [here](https://github.com/shubhamk0027/MockServer/blob/localDb/src/main/java/com/mock/server/Application.java). 

        For Adding 1000 GET Requests each with response size of 100 KB
        Time taken to add 1000 GET requests: 2677 ms
        Memory usage for 1000 GET requests: 256 MB
        
        For adding 1000 POST requests each with response size OF 100 KB and
        Payload size of 1 KB
        Time taken to add 1000 POST requests: 2943 ms
        Memory usage for 1000 POST and 1000 GET requests: 370 MB
        
        Persistence Test:
        After restarting the server
        Time taken to load all 2000 Operations:	4913 ms
        
        // Network latencies are not included. All the requests are sent from within the server

## Few more things about this server:

1. You can send your queries via the following paths, to update the mock server as a POST request with the payloads. 

        http//localhost:8080/_admin/_add/_team
        http//localhost:8080/_admin/_add/_schema
        http//localhost:8080/_admin/_add/_mock

        http//localhost:8080/_admin/_get/_schema
        http//localhost:8080/_admin/_get/_key
        
        http//localhost:8080/_admin/_del/_team
        http//localhost:8080/_admin/_del/_mock
        http//localhost:8080/_admin/_del/_payload
        

2. All the paths in the mock queries should be relative not absolute and without query parameters. Query parameters should be added in a separate field

3. The path can have directory names as a regular expression as should in the tutorial above

4. Payloads can have strict checking type or normal checking. For more about how the payloadResponse comparison is happening, see https://github.com/skyscreamer/JSONassert

5. Schema matching is done with the help of skyscreamer library, https://github.com/everit-org/json-schema

6. There is support for HEAD, PUT, DEL, POST, GET, OPTIONS and TRACE methods.

7. Query parameters can be a regular expression too.

8. There is support of persistence in the server. In case server crashers, it can automatically load all the operations from the operations.log file

9. log.log contains all the logs. and the major operations are in operations.log file

10. Don't forget to delete both the logs after the development process is over. 

11. All the data, payloadResponse, and the requests must be in JSON format. Other content types are not supported

12. Add all the opeartions you have performed on the mockserver in a separate request.http file, so that you can keep track of the old and new queries well. As shown [here](https://github.com/shubhamk0027/MockServer/blob/localDb/src/test/java/com/mock/server/httpTest.http)


## Also, as of now

1. Responses added for a request are not validated anytime, so if response is not valid, corresponding http response may not be valid.

2. Regex on methods are not there.

3. Only JSON content type is supported.
