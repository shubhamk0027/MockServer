# MockServer
Have your team's development stopped because of the limits or cost on the number of api calls you can make to a service?
Or the team's backend service on which your app depends is not working? Or because the third party service on which your backend depends is not available? 

This MockServer comes to the rescue by helping you create a fake server that will act as your backend service! 

### How to use it?

You can either set up the slack bot our send queries directly. Run this slack 
bot (https://github.com/shubhamk0027/SlackBot) after adding the bearer token in 
MessagePoster class and adding bearer token and slack client token in the environment 
parameters. 

It also uses redis to store and reply the big responses efficiently. So you can 
have responses with huge response body, without any performance issues. 

Use the following slash command:
#### TLDR;

Go through this quick tutorial below to know it better.
(https://drive.google.com/file/d/1sk_VV9kycOtOwtN3ikzgRYGEuvOwv5BT/view?usp=sharing)

#### Slash Commands Supported    
##### /addteam
to create a new team. It will provide you with a secret team token. 
This secret team key will be used to send and add mock queries to this mock server.

##### /delteam
To delete a team. Only the admin, the user who created the team can 
delete a team.

##### /addschema
To add a schema support to the request body of the mock queries 
you will be adding up. Its just like a backend service rejecting the queries 
which are not according to the requested format. This is only for the http
methods which support payloads (like PUT, POST and GET)

##### /getschema
It will return you with the schema JSON at a path

##### /addmock 
To add a mock query which is a mock request with a mock response body.
A mock request is the http request you will be sending to the mockserver, and Mock response
is the response that the mockserver will give you in return.

##### /delmock
To delete a mockquery  

##### /getkey
To get back the lost key. This command can only be used by the admin. 

#### Few more things about this server:
1. You can also send the above slash commands without slack directly as an Http request. 
Equivalent to the above slash commands, send the JSON with above details to-

        http//localhost:8080/_admin/_add/_team
        http//localhost:8080/_admin/_add/_schema
        http//localhost:8080/_admin/_add/_mock
        http//localhost:8080/_admin/_get/_schema
        http//localhost:8080/_admin/_get/_key
        http//localhost:8080/_admin/_del/_team
        http//localhost:8080/_admin/_del/_mock
        
2. All the paths in the mock queries should be relative not absolute and 
without query parameters. Query parameters should be added in a separate field

3. The path can have directory names as a regular expression as should in the tutorial above

4. Payloads can have strict checking type or normal checking. For more about how the
payload comparison is happening, see https://github.com/skyscreamer/JSONassert

5. Schema matching is done with the help of skyscreamer library, https://github.com/everit-org/json-schema

6. There is support for HEAD, PUT, DEL, POST, GET, OPTIONS and TRACE methods.

7. Query parameters can be a regular expression too.

8. There is support of persistence in the server, you do not need to take snapshots
of the redis server as well.

9. The server responds very fast as all queries, and the whole database is in memory. Persistence
is added via AOF techniques as used by redis.

10. log.log contains all the logs. and the major operations are in operations.log file

11. Don't forget to delete both the logs after the development process is over. 

12. All the data, payload, and the requests must be in JSON format. Other content types are not supported


#### Also as of now

1. There is no check Mock Response is not validated anytime, so if response string is not valid, corresponding http response may also be valid.

2. Regex on methods are not there.

3. Only JSON content type and schema checks are there.
