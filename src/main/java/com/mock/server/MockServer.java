package com.mock.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.*;

@Service
public class MockServer {

    private static final Logger logger= LoggerFactory.getLogger(MockServer.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final RedisClient redisClient;
    private final TreeNode root ;

    private Integer counter;

    private MockServer(RedisClient redisClient){
        root = new TreeNode("");
        this.redisClient= redisClient;
        // load values from some db/logger
        // loading the counter from redis
        // counter= redisClient.getCounter();
        counter=0;
        logger.info("Loading from counter Val-> "+ counter);
    }


    // duplicates will not be handled unless both the redis and this tree are in synchronization
    public void add(String s) throws JsonProcessingException {
        MockQuery mockQuery = MockQuery.getMockRequest(s);
        addMockQuery(mockQuery);
    }

    public void addMockQuery(MockQuery mockQuery) throws JsonProcessingException {
        ArrayList<String> pathList = getPathList(mockQuery.method.val,mockQuery.path);

        TreeNode trav= root;
        for(String dir: pathList){
            if(!trav.getChildren().containsKey(dir)) trav.addChild(dir, new TreeNode(dir));
            trav= trav.getChildren().get(dir);
        }

        if(trav.getId()==-1) {
            synchronized (trav){
                if(trav.getId()==-1) {
                    counter++;
                    trav.setId(counter); // setId(int)
                }
            }
        }

        String key = Integer.toString(trav.getId());
        String val = mapper.writeValueAsString(mockQuery.response);
        logger.info("ADDING TO REDIS: "+"["+key+"]->"+val);
        redisClient.addVal(key,val);
    }

    // only * is considered a special character matching any!
    boolean matches(String text, String patt){
        int n=text.length(), m=patt.length();
        boolean[][] dp = new boolean[n+1][m+1];

        for(int i=0;i<=n;i++) for(int j=0;j<=m;j++) dp[i][j]=false;
        dp[0][0]=true;
        for(int i=1;i<=m;i++) if(patt.charAt(i-1)=='*'){
            dp[0][i]=dp[0][i-1];
        }

        for(int i=1;i<=n;i++){
            for(int j=1;j<=m;j++){
                if (patt.charAt(j-1) == '*') dp[i][j] = dp[i][j-1] || dp[i-1][j];
                else if(patt.charAt(j-1)==text.charAt(i-1)) dp[i][j]=dp[i-1][j-1];
            }
        }
        return dp[n][m];
    }

    private TreeNode find(TreeNode trav, ArrayList<String> pathList, int id){
        for(Map.Entry<String,TreeNode> itr : trav.getChildren().entrySet()){
            if(matches(pathList.get(id),itr.getKey())) {
                if(id==pathList.size()-1) return itr.getValue();
                TreeNode res = find(itr.getValue(),pathList,id+1);
                if(res!=null) return res;
            }
        }
        return null;
    }

    public MockResponse getResponse(String method, String pathPattern) throws Exception {
        ArrayList<String> pathList= getPathList(method,pathPattern);
        TreeNode sol = find(root,pathList,0);
        if(sol==null) {
            throw new Exception("Directory  matching the path does not exists!");
        }
        if(sol.getId()==-1) throw new Exception("Path does not exists!");
        String key = Integer.toString(sol.getId());
        String val = redisClient.getVal(key);
        return mapper.readValue(val,MockResponse.class);
    }



    // acts on validated path
    private ArrayList<String> getPathList(String method, String path ){
        ArrayList<String> pathList= new ArrayList <>();
        pathList.add(method);
        String[] ar = path.split("/");
        pathList.addAll(Arrays.asList(ar).subList(1, ar.length));
        logger.info("Path As List: "+pathList);
        return pathList;
    }



    @PreDestroy
    public void destroy(){
        redisClient.setCounter(counter);
    }
}
