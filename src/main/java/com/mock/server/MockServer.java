package com.mock.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.*;


@Service
public class MockServer {

    private static final Logger logger = LoggerFactory.getLogger(MockServer.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final RedisClient redisClient;
    private final TreeNode root;
    private final Verifier verifier;
    private final PayloadsAndSchema payloadsAndSchema;

    // Index of the payload list where the response can be
    private Integer counter;

    private MockServer(
            RedisClient redisClient,
            Verifier verifier,
            PayloadsAndSchema payloadsAndSchema) {

        root = new TreeNode(new DirName("ROOT"));
        this.redisClient = redisClient;
        this.verifier=verifier;
        this.payloadsAndSchema=payloadsAndSchema;

        counter = 0;
        // load values from some db/logger
        // loading the counter from redis
        // counter= redisClient.getCounter();
    }


    public void addMockQuery(MockQuery mockQuery) throws JsonProcessingException {
        logger.info("...............................");
        logger.info("Adding to the URI tree");

        verifier.verifyMethodAndQuery(
                mockQuery.getMockRequest().getMethod(),
                mockQuery.getMockRequest().getJsonBody(),
                mockQuery.getMockRequest().getQueryParameters(),
                mockQuery.getMockRequest().getQueryParametersRegex()
        );

        ArrayList <Directory> pathList = verifier.getPathList(
                mockQuery.getMockRequest().getMethod(),
                mockQuery.getMockRequest().getPath(),
                mockQuery.getMockRequest().getQueryParameters(),
                mockQuery.getMockRequest().getQueryParametersRegex()
        );

        TreeNode trav = root;

        // Make this operation atomic
        for(Directory dir : pathList) {
            if(!trav.getChildren().containsKey(dir.getDirName()))
                trav.addChild(dir.getDirName(), new TreeNode(dir));
            trav = trav.getChildren().get(dir.getDirName());
        }

        if(trav.getId() == -1) {
            synchronized (trav) {
                if(trav.getId() == -1) {
                    counter++;
                    trav.setId(counter); // setId(int)
                }
            }
        }

        // count of the number of payloads
        int redisKey = payloadsAndSchema.addPayload(
                trav.getId(),
                mockQuery.getMockRequest().getJsonBody()
        );

        String key = Integer.toString(redisKey);
        // Add the value to redis after serialization
        RedisValue redisValue = new RedisValue(mockQuery.getMockResponse());
        String value = mapper.writeValueAsString(redisValue);

        logger.info("Payload List At: "+trav.getId());
        logger.info("Key: "+key);
        logger.info("Val: "+value);

        redisClient.addVal(key,value);
        value = redisClient.getVal(key);
        logger.info("Added to Redis: "+key+" : "+ value);
    }

/*
    // only * is considered a special character matching any!
    boolean matches(String text, String patt) {
        int n = text.length(), m = patt.length();
        boolean[][] dp = new boolean[n + 1][m + 1];

        for(int i = 0; i <= n; i++) for(int j = 0; j <= m; j++) dp[i][j] = false;
        dp[0][0] = true;
        for(int i = 1; i <= m; i++)
            if(patt.charAt(i - 1) == '*') {
                dp[0][i] = dp[0][i - 1];
            }

        for(int i = 1; i <= n; i++) {
            for(int j = 1; j <= m; j++) {
                if(patt.charAt(j - 1) == '*') dp[i][j] = dp[i][j - 1] || dp[i - 1][j];
                else if(patt.charAt(j - 1) == text.charAt(i - 1)) dp[i][j] = dp[i - 1][j - 1];
            }
        }
        return dp[n][m];
    }
*/

    private TreeNode find(TreeNode trav, ArrayList <String> pathList, int id) {
        for(Map.Entry <String, TreeNode> itr : trav.getChildren().entrySet()) {
            if(itr.getValue().matches(pathList.get(id))) {
                if(id == pathList.size() - 1) {
                    logger.info(itr.getKey()+" matched "+pathList.get(id));
                    return itr.getValue();
                }
                TreeNode res = find(itr.getValue(), pathList, id + 1);
                if(res != null) return res;
            }else{
                logger.info(itr.getValue().getName()+" does not matched "+pathList.get(id));
            }
        }
        return null;
    }

    public RedisValue getResponse(ArrayList<String> pathList, JSONObject jsonObject) throws Exception {
        logger.info("........................");
        logger.info("Matching Process Started");

        TreeNode sol = find(root, pathList, 0);
        if(sol == null) {
            throw new Exception("Directory matching the path does not exists!");
        }

        if(sol.getId() == -1) throw new Exception("Path does not exists!");

        // check if there exists such a payload
        int at = payloadsAndSchema.checkPayload(sol.getId(),jsonObject);
        // no exception => success
        logger.info("Payload array at "+sol.getId());
        logger.info("Payload at"+at);

        // deserialize and return
        String res = redisClient.getVal(Integer.toString(at));
        return mapper.readValue(res,RedisValue.class);
    }

    @PreDestroy
    public void destroy() {
        redisClient.setCounter(counter);
    }
}
