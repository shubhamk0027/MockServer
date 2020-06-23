# MockServer
Have your team's development stopped because of the limits or cost on the number of api calls you can make to a service?
Or the team's backend service on which your app depends is not working? Or because the third party service on which your backend depends is not available? 

This MockServer comes to the rescue by helping you create a fake server that will act as your backend service! 

## How to use it?

You can either set up the slack bot or send queries directly. Run this with [slack bot](https://github.com/shubhamk0027/SlackBot) after adding bearer token and slack client token in the environment parameters as
"SLACK_BOT_TOKEN=FULL_TOKEN_HERE; SLACK_SIGNING_SECRET=SIGNING_SECRET_HERE" without the quotes. 

## Slack slash commands supported 
See the slackbot in [action](https://drive.google.com/file/d/1sk_VV9kycOtOwtN3ikzgRYGEuvOwv5BT/view?usp=sharing) to understand how to use it. Simply run the following slash commands in the slack after running the mock server and the slack bot server. A dialog box will appear asking for the Http request details and the corresponding response details to return. More details on what to fill in the input boxes of the dialog can be understood by understading how the mock server works. The examples [below](https://github.com/shubhamk0027/MockServer#using-mockserver-without-the-slackbot) explains the queries.

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
To delete a mockquery  

#### /getkey
To get back the lost key. This command can only be used by the admin. 

## How to send your request to the mock server from your application?
Send your request as 
        
        http//localhost:8080/TEAM-SECRET-KEY/your/path/here?any=query&parameters
        
with payloads if any

## Using MockServer without the SlackBot
To run it without use of slack bot, setup the mock server and send your queries directly to the server. The following examples explains how to send the queries. 

#### Create a new team
Slack bot will automatically consider the adminId parameter. If you are using it without the slack bot, you need to send your adminId it it manually as in the request. For a team there will be one adminId, and only he can delete the team and perform the Get Key operation. So make sure this adminId remains a secret to you. Otherwise, any person sending the adminId within the delete team query, can delete your team! 

        POST http://localhost:8080/_admin/_add/_team
        Content-Type: application/json

        {
          "teamName":"Dev1",
          "adminId":"admin1"
        }

        ### Expected Response
        ### Your Team Key ca72678b69c3c1ed



#### Multiple teams can not have same name

        POST http://localhost:8080/_admin/_add/_team
        Content-Type: application/json

        {
          "teamName":"Dev1",
          "adminId":"admin2"
        }


        ### Expected Response
        ### Team Name exists. Choose a different Team Name!




#### Create another team with a different name
        POST http://localhost:8080/_admin/_add/_team
        Content-Type: application/json

        {
          "teamName":"Dev2",
          "adminId":"admin2"
        }

        ### Expected Response
        ### Your Team Key 87879c21b7b3c632




#### Teams can only be deleted by the admin
        POST http://localhost:8080/_admin/_del/_team
        Content-Type: application/json

        {
          "teamKey":"c29219479096998a",
          "adminId":"admin1"
        }

        ### Expected Response
        ### Only the Admin can delete a team!




#### Delete second team
        POST http://localhost:8080/_admin/_del/_team
        Content-Type: application/json

        {
          "teamKey":"87879c21b7b3c632",
          "adminId":"admin2"
        }

        ### Expected Response
        ### Team Deleted Successfully!




#### Add Mock Query 
Here you can note that the checkMode is false(Lenient). It uses JSONAssert for matching the json requestBody. You can read more about it [here](https://github.com/skyscreamer/JSONassert). STRICT MODE matches all fields order of arrays and no additional fields allowed, and ONLY_MATCHING_FIELDS/LENIENT MODE which only matches fields provided in the request matcher

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
          "teamName":"Dev1",
          "adminId":"admin1"
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
  
          


## Few more things about this server:
1. You can send your queries via the following paths, to update the mock server as a POST request with the payloads. 

        http//localhost:8080/_admin/_add/_team
        http//localhost:8080/_admin/_add/_schema
        http//localhost:8080/_admin/_add/_mock
        http//localhost:8080/_admin/_add/_payload
        http//localhost:8080/_admin/_get/_schema
        http//localhost:8080/_admin/_get/_key
        http//localhost:8080/_admin/_del/_team
        http//localhost:8080/_admin/_del/_mock
        

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


## Also, as of now

1. Response Body is not validated anytime, so if response string is not valid, corresponding http response may not be valid.

2. Regex on methods are not there.

3. Only JSON content type is supported.
